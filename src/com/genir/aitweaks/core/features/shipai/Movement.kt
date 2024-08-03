package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand.USE_SYSTEM
import com.genir.aitweaks.core.combat.combatState
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.*
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.sign

@Suppress("MemberVisibilityCanBePrivate")
class Movement(override val ai: AI) : Coordinable {
    private val ship: ShipAPI = ai.ship
    private val engineController: EngineController = EngineController(ship)

    var headingPoint: Vector2f = Vector2f()
    var expectedVelocity: Vector2f = Vector2f()
    var expectedFacing: Float = ship.facing

    // Used for communication with attack coordinator.
    override var proposedHeadingPoint: Vector2f? = null
    override var reviewedHeadingPoint: Vector2f? = null

    // Make strafe rotation direction random, but consistent for a given ship.
    private val strafeRotation = Rotation(if (ship.id.hashCode() % 2 == 0) 10f else -10f)
    private var averageAimOffset = RollingAverageFloat(Preset.aimOffsetSamples)

    fun advance(dt: Float) {
        setHeading(dt, ai.maneuverTarget, ai.assignmentLocation)
        setFacing()
        manageMobilitySystems()
    }

    private fun setHeading(dt: Float, maneuverTarget: ShipAPI?, moveOrderLocation: Vector2f?) {
        val systemOverride: Vector2f? = ai.systemAI?.overrideHeading()

        headingPoint = when {
            // Let movement system determine ship heading.
            systemOverride != null -> {
                systemOverride
            }

            // Move directly to ordered location for player ships.
            ship.owner == 0 && !ship.isAlly && moveOrderLocation != null -> {
                moveOrderLocation
            }

            // Move opposite to threat direction when backing off.
            // If there's no threat, the ship will continue to coast.
            ai.isBackingOff -> {
                val farAway = 2048f
                if (ai.threatVector.isZeroVector()) ship.location + ship.velocity.resized(farAway)
                else ship.location - ai.threatVector.resized(farAway)
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
                val shouldStrafe = ai.is1v1 && ai.range(maneuverTarget) <= ai.broadside.effectiveRange
                val attackPositionOffset = if (shouldStrafe) vectorToThreat.rotated(strafeRotation)
                else vectorToThreat

                // Let the attack coordinator review the calculated heading point.
                proposedHeadingPoint = maneuverTarget.location - attackPositionOffset.resized(ai.broadside.minRange)
                val headingPoint = (reviewedHeadingPoint ?: proposedHeadingPoint)!!
                reviewedHeadingPoint = null

                headingPoint
            }

            // Move directly to ordered location for non-player ships.
            moveOrderLocation != null -> {
                moveOrderLocation
            }

            // Nothing to do, stop the ship.
            else -> {
                if ((ship.location - headingPoint).length() > ship.collisionRadius) ship.location
                else headingPoint
            }
        }

        expectedVelocity = engineController.heading(headingPoint, gatherSpeedLimits(dt))
    }

    private fun setFacing() {
        val systemOverride: Float? = ai.systemAI?.overrideFacing()

        expectedFacing = when {
            // Let movement system determine ship facing.
            systemOverride != null -> {
                systemOverride
            }

            // Face the attack target.
            ai.attackTarget != null -> {
                val target = ai.attackTarget!!

                // Average aim offset to avoid ship wobbling.
                val aimPointThisFrame = calculateOffsetAimPoint(target)
                val aimOffsetThisFrame = getShortestRotation(target.location, ship.location, aimPointThisFrame)
                val aimOffset = averageAimOffset.update(aimOffsetThisFrame)

                (target.location - ship.location).facing + aimOffset - ai.broadside.facing
            }

            // Face threat direction when no target.
            !ai.threatVector.isZeroVector() -> {
                ai.threatVector.facing
            }

            // Face movement target location.
            ai.assignmentLocation != null -> {
                (ai.assignmentLocation!! - ship.location).facing
            }

            // Nothing to do. Stop rotation.
            else -> expectedFacing
        }

        engineController.facing(expectedFacing)
    }

    private fun manageMobilitySystems() {
        when (ship.system?.specAPI?.AIType) {

            ShipSystemAiType.BURN_DRIVE -> {
                // Prevent vanilla AI from jumping closer to target with
                // BURN_DRIVE, if the target is already within weapons range.
                if (ai.attackTarget != null && ai.range(ai.attackTarget!!) < ai.broadside.effectiveRange) {
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
        val hardpoints = ship.allWeapons.filter { it.slot.isHardpoint }.mapNotNull { it.customAI }
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
                blocked.range(target) > blocked.broadside.maxRange * 1.2f -> return@forEach
            }

            val arcLength = distToTarget * abs(angleToOtherLine) * DEGREES_TO_RADIANS
            val minDist = (ai.stats.totalCollisionRadius + obstacle.stats.totalCollisionRadius) * 0.75f
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
        if (tgtLoc != null && !ai.isBackingOff && abs(getShortestRotation(tgtLoc - ship.location, borderIntrusion)) < 90f) {
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
        val distance = dirAbs - (ai.stats.totalCollisionRadius + obstacle.totalCollisionRadius + Preset.collisionBuffer)

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
