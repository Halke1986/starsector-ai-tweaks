package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand.USE_SYSTEM
import com.genir.aitweaks.core.combat.combatState
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.*
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.sign

class Movement(override val ai: AI) : Coordinable {
    // Used for communication with attack coordinator.
    override var proposedHeadingPoint: Vector2f? = null
    override var reviewedHeadingPoint: Vector2f? = null

    private val ship = ai.ship
    private val engineController = EngineController(ship)

    // Make strafe rotation direction random, but consistent for a given ship.
    private val strafeRotation = Rotation(if (ship.id.hashCode() % 2 == 0) 10f else -10f)
    private var averageAimOffset = RollingAverageFloat(Preset.aimOffsetSamples)

    fun advance(dt: Float) {
        ai.burnDriveAI?.advance(dt)
        setFacing()
        setHeading(dt, ai.maneuverTarget, ai.moveOrderLocation)
        manageMobilitySystems()
    }

    private fun setFacing() {
        val (aimPoint: Vector2f, velocity: Vector2f) = when {
            // Position ship to start burn.
            ai.burnDriveAI?.shouldBurn == true -> {
                // Compiler should require !! on headingPoint. Is this a Kotlin bug?
                Pair(ai.burnDriveAI.destination, Vector2f())
            }

            // Face the attack target.
            ai.attackTarget != null -> {
                val target = ai.attackTarget!!

                // Average aim offset to avoid ship wobbling.
                val aimPointThisFrame = calculateOffsetAimPoint(target)
                val aimOffsetThisFrame = getShortestRotation(target.location, ship.location, aimPointThisFrame)
                val aimOffset = averageAimOffset.update(aimOffsetThisFrame)

                Pair(target.location.rotatedAroundPivot(Rotation(aimOffset - ai.broadsideFacing), ship.location), target.velocity)
            }

            // Face threat direction when no target.
            !ai.threatVector.isZeroVector() -> {
                Pair(ship.location + ai.threatVector, Vector2f())
            }

            // Face movement target location.
            ai.moveOrderLocation != null -> {
                Pair(ai.moveOrderLocation, Vector2f())
            }

            // Nothing to do. Stop rotation.
            else -> Pair(ship.location, Vector2f())
        }

        ai.aimPoint = aimPoint
        engineController.facing(aimPoint, velocity)
    }

    private fun setHeading(dt: Float, maneuverTarget: ShipAPI?, moveOrderLocation: Vector2f?) {
        val (headingPoint: Vector2f, velocity: Vector2f) = when {
            // Position ship to start burn.
            ai.burnDriveAI?.shouldBurn == true -> {
                Pair(ai.burnDriveAI.destination, Vector2f())
            }

            // Move directly to ordered location for player ships.
            ship.owner == 0 && !ship.isAlly && moveOrderLocation != null -> {
                Pair(moveOrderLocation, Vector2f())
            }

            // Move opposite to threat direction when backing off.
            // If there's no threat, the ship will continue to coast.
            ai.isBackingOff -> {
                if (ai.threatVector.isZeroVector()) Pair(ship.location, ship.velocity.resized(ship.maxSpeed))
                else Pair(ship.location - ai.threatVector.resized(1000f), Vector2f())
            }

            // Orbit target at effective weapon range.
            // Rotate away from threat if there are multiple enemy ships around.
            // Chase the target if there are no other enemy ships around.
            // Strafe target randomly if in range and no other threat.
            maneuverTarget != null -> {
                // TODO will need syncing when interval tracking is introduced.
                val vectorToTarget = maneuverTarget.location - ship.location
                val vectorToThreat = if (!ai.threatVector.isZeroVector()) ai.threatVector else vectorToTarget

                // Strafe the target randomly, when it's the only threat.
                val shouldStrafe = ai.is1v1 && ai.range(maneuverTarget) <= ai.effectiveRange
                val attackPositionOffset = if (shouldStrafe) vectorToThreat.rotated(strafeRotation)
                else vectorToThreat

                // Let the attack coordinator review the calculated heading point.
                proposedHeadingPoint = maneuverTarget.location - attackPositionOffset.resized(ai.minRange)
                val headingPoint = (reviewedHeadingPoint ?: proposedHeadingPoint)!!
                reviewedHeadingPoint = null

                val velocity = (headingPoint - (ai.headingPoint ?: headingPoint)) / dt
                Pair(headingPoint, velocity)
            }

            // Move directly to ordered location for non-player ships.
            moveOrderLocation != null -> {
                Pair(moveOrderLocation, Vector2f())
            }

            // Nothing to do, stop the ship.
            else -> Pair(ship.location, Vector2f())
        }

        ai.headingPoint = headingPoint
        engineController.heading(headingPoint, velocity, gatherSpeedLimits(dt))
    }

    private fun manageMobilitySystems() {
        when (ship.system?.specAPI?.AIType) {

            ShipSystemAiType.MANEUVERING_JETS -> when {
                !ship.canUseSystemThisFrame() -> Unit

                // Use MANEUVERING_JETS to back off. Vanilla AI does
                // this already, but is not determined enough.
                ai.isBackingOff -> ship.command(USE_SYSTEM)

                // Use MANEUVERING_JETS to chase target during 1v1 duel.
                ai.is1v1 && ai.range(ai.attackTarget!!) > ai.effectiveRange -> ship.command(USE_SYSTEM)
            }

            ShipSystemAiType.BURN_DRIVE -> {
                // Prevent vanilla AI from jumping closer to target with
                // BURN_DRIVE, if the target is already within weapons range.
                if (ai.attackTarget != null && ai.range(ai.attackTarget!!) < ai.effectiveRange) {
                    ship.blockCommandForOneFrame(USE_SYSTEM)
                }

                if (ai.isBackingOff) ship.blockCommandForOneFrame(USE_SYSTEM)
            }

            else -> Unit
        }
    }

    /** Aim hardpoint weapons with entire ship, if possible. */
    private fun calculateOffsetAimPoint(attackTarget: ShipAPI): Vector2f {
        // Find intercept points of all hardpoints attacking the current target.
        val hardpoints = ship.allWeapons.filter { it.slot.isHardpoint }.mapNotNull { it.autofireAI }
        val aimedHardpoints = hardpoints.filter { it.targetShip != null && it.targetShip == attackTarget }
        val interceptPoints = aimedHardpoints.mapNotNull { it.intercept }

        if (interceptPoints.isEmpty()) return attackTarget.location

        // Average the intercept points. This may cause poor aim if different hardpoints
        // have weapons with significantly different projectile velocities.
        val interceptSum = interceptPoints.fold(Vector2f()) { sum, intercept -> sum + intercept }
        val aimPoint = interceptSum / interceptPoints.size.toFloat()

        return aimPoint
    }

    private fun gatherSpeedLimits(dt: Float): List<EngineController.Limit> {
        val friendlies = Global.getCombatEngine().ships.filter {
            when {
                it == ship -> false
                it.owner != ship.owner -> false
                it.collisionClass != CollisionClass.SHIP -> false

                // Modules and drones count towards
                // their parent collision radius.
                it.isModule -> false
                it.isDrone -> false
                else -> true
            }
        }

        val limits: MutableList<EngineController.Limit?> = mutableListOf(avoidBlockingLineOfFire(dt, friendlies), avoidBorder())

        limits.addAll(avoidCollisions(dt, friendlies))

        return limits.filterNotNull()
    }

    private fun avoidCollisions(dt: Float, friendlies: List<ShipAPI>): List<EngineController.Limit?> {
        return friendlies.map { obstacle ->
            val lim = vMaxToObstacle(dt, obstacle)
            if (lim.speed < ship.maxSpeed) lim
            else null
        }
    }

    private fun avoidBlockingLineOfFire(dt: Float, allies: List<ShipAPI>): EngineController.Limit? {
        val target = ai.attackTarget ?: return null
        val ais = allies.mapNotNull { it.customAI }

        // Blocking line of fire occurs mostly among ships attacking the same target.
        // For simplicity, the AI will try to avoid only those cases of blocking.
        val squad = ais.filter { it.attackTarget == ai.attackTarget }
        if (squad.isEmpty()) return null

        // Calculations are done in target frame of reference.
        val lineOfFire = ship.location - target.location
        val facing = lineOfFire.getFacing()
        val distToTarget = lineOfFire.length()

        val velocityFacing = (ship.location + ship.velocity - target.location).getFacing()
        val angleToVelocity = MathUtils.getShortestRotation(facing, velocityFacing)

        var maxLimit: EngineController.Limit? = null

        squad.forEach { obstacle ->
            val obstacleLineOfFire = obstacle.ship.location - target.location
            val obstacleFacing = obstacleLineOfFire.getFacing()
            val angleToOtherLine = MathUtils.getShortestRotation(facing, obstacleFacing)

            val blocked = if (obstacleLineOfFire.lengthSquared() < lineOfFire.lengthSquared()) ai
            else obstacle

            when {
                blocked.isBackingOff -> return@forEach

                // Too far from obstacle line of fire to consider blocking.
                abs(MathUtils.getShortestRotation(facing, obstacleFacing)) >= 90f -> return@forEach

                // Ship is moving away from the obstacle.
                angleToVelocity.sign != angleToOtherLine.sign -> return@forEach

                // Do not consider line of fire blocking if target is out of range, with 1.2 tolerance factor.
                blocked.range(target) > blocked.maxRange * 1.2f -> return@forEach
            }

            val arcLength = distToTarget * abs(angleToOtherLine) * DEGREES_TO_RADIANS
            val minDist = (ai.totalCollisionRadius + obstacle.totalCollisionRadius) * 0.75f
            val distance = arcLength - minDist

            // Line of fire blocking occurs when ships orbiting the same target
            // strafe in front of each one. To prevent this, speed limit is imposed
            // only on strafing velocity. This allows to return only the most severe
            // speed limit, as all found limits are parallel.
            val limitFacing = facing + 90f * angleToOtherLine.sign

            val limit = if (distance < 0f) {
                // Already blocking.
                EngineController.Limit(limitFacing, 0f)
            } else {
                val obstacleV = obstacle.ship.velocity
                val obstacleAngularV = obstacleV - vectorProjection(obstacleV, obstacleLineOfFire)

                val t = timeToOrigin(obstacle.ship.location - ship.location, obstacleAngularV)
                val obstacleVComponent = obstacleAngularV.length() * t.sign

                val vMax = vMax(dt, distance, ship.strafeAcceleration) + obstacleVComponent
                EngineController.Limit(limitFacing, vMax)
            }

            if (maxLimit == null || maxLimit!!.speed > limit.speed) {
                maxLimit = limit
            }
        }

        return maxLimit
    }

    private fun avoidBorder(): EngineController.Limit? {
        ai.isAvoidingBorder = false

        val mapH = Global.getCombatEngine().mapHeight / 2f
        val mapW = Global.getCombatEngine().mapWidth / 2f
        val borderZone = Preset.borderNoGoZone + Preset.borderCornerRadius

        // Translate ship coordinates so that the ship appears to be
        // always near a map corner. That way we can use a circle
        // calculations to approximate a rectangle with rounded corners.
        val l = ship.location
        val borderIntrusion = Vector2f()
        borderIntrusion.x = if (l.x > 0) (l.x - mapW + borderZone).coerceAtLeast(0f)
        else (l.x + mapW - borderZone).coerceAtMost(0f)
        borderIntrusion.y = if (l.y > 0) (l.y - mapH + borderZone).coerceAtLeast(0f)
        else (l.y + mapH - borderZone).coerceAtMost(0f)

        // Distance into the border zone.
        val d = (borderIntrusion.length() - Preset.borderCornerRadius).coerceAtLeast(0f)

        // Ship is far from border, no avoidance required.
        if (d == 0f) return null

        // Allow chasing targets into the border zone.
        val tgtLoc = ai.maneuverTarget?.location
        if (tgtLoc != null && !ai.isBackingOff && getShortestRotation(tgtLoc - ship.location, borderIntrusion) < 90f) {
            return null
        }

        ai.isAvoidingBorder = true

        // The closer the ship is to map edge, the stronger
        // the heading transformation away from the border.
        val avoidForce = (d / (Preset.borderNoGoZone - Preset.borderHardNoGoZone)).coerceAtMost(1f)
        return EngineController.Limit(borderIntrusion.getFacing(), ship.maxSpeed * (1f - avoidForce))
    }

    private fun vMaxToObstacle(dt: Float, obstacle: ShipAPI): EngineController.Limit {
        val direction = obstacle.location - ship.location
        val dirAbs = direction.length()
        val dirFacing = direction.getFacing()
        val distance = dirAbs - (ai.totalCollisionRadius + obstacle.totalCollisionRadius + Preset.collisionBuffer)

        // Already colliding.
        if (distance <= 0f) return EngineController.Limit(dirFacing, 0f)

        var vObstacle = vectorProjectionLength(obstacle.velocity, direction)
        val aObstacle = vectorProjectionLength(combatState().accelerationTracker[obstacle], direction)

        // Take obstacle acceleration into account when the obstacle is doing a brake check.
        // The acceleration is approximated as total velocity loss. Including actual
        // acceleration (shipAcc + aAbs) in calculations leads to erratic behavior.
        if (vObstacle > 0f && aObstacle < 0f) vObstacle = 0f

        val angleFromBow = abs(MathUtils.getShortestRotation(ship.facing, dirFacing))
        val shipAcc = when {
            angleFromBow < 30f -> ship.deceleration
            angleFromBow < 150f -> ship.strafeAcceleration
            else -> ship.acceleration
        }

        val vMax = vMax(dt, distance, shipAcc) + vObstacle
        return EngineController.Limit(dirFacing, vMax)
    }
}
