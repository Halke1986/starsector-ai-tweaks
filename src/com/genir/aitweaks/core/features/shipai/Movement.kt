package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand.USE_SYSTEM
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.combat.combatState
import com.genir.aitweaks.core.features.shipai.autofire.AutofireAI
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.*
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
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

    private fun setHeading(dt: Float, maneuverTarget: ShipAPI?, assignmentLocation: Vector2f?) {
        val systemOverride: Vector2f? = ai.systemAI?.overrideHeading()

        val newHeadingPoint: Vector2f? = when {
            // Let movement system determine ship heading.
            systemOverride != null -> {
                systemOverride
            }

            // For player ships the heading to assignment location takes priority.
            ship.owner == 0 && !ship.isAlly && shouldHeadToAssigment(assignmentLocation) -> {
                assignmentLocation
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
                proposedHeadingPoint = maneuverTarget.location - attackPositionOffset.resized(ai.calculateAttackRange())
                val headingPoint = (reviewedHeadingPoint ?: proposedHeadingPoint)!!
                reviewedHeadingPoint = null

                headingPoint
            }

            // Move directly to assignment location.
            assignmentLocation != null -> {
                assignmentLocation
            }

            // Nothing to do, stop the ship.
            else -> null
        }

        if (newHeadingPoint != null) {
            headingPoint = newHeadingPoint.copy
            expectedVelocity = engineController.heading(headingPoint, gatherSpeedLimits(dt))
        } else {
            headingPoint = ship.location
            expectedVelocity = engineController.stop()
        }
    }

    private fun setFacing() {
        val systemOverride: Float? = ai.systemAI?.overrideFacing()
        val currentAttackTarget: ShipAPI? = ai.attackTarget ?: ai.finishBurstTarget
        val broadside = if (currentAttackTarget == ai.attackTarget) ai.broadside
        else ai.finishBurstBroadside!!

        expectedFacing = when {
            // Let movement system determine ship facing.
            systemOverride != null -> {
                systemOverride
            }

            // Face the attack target.
            currentAttackTarget != null && (currentAttackTarget.location - ship.location).length <= ai.stats.threatSearchRange -> {
                // Average aim offset to avoid ship wobbling.
                val aimPointThisFrame = unitVector(aimShip(currentAttackTarget, broadside)) * 100f + ship.location
                val aimOffsetThisFrame = getShortestRotation(currentAttackTarget.location, ship.location, aimPointThisFrame)
                val aimOffset = averageAimOffset.update(aimOffsetThisFrame)

                (currentAttackTarget.location - ship.location).facing + aimOffset
            }

            // Face threat direction when no target.
            !ai.threatVector.isZeroVector() -> {
                ai.threatVector.facing
            }

            // Face expected velocity.
            expectedVelocity.length > 5f -> {
                expectedVelocity.facing
            }

            // Nothing to do. Stop rotation.
            else -> expectedFacing
        }

        engineController.facing(expectedFacing)
    }

    private fun shouldHeadToAssigment(location: Vector2f?): Boolean {
        return location != null && (location - ship.location).length > Preset.arrivedAtLocationRadius
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

    /** Aim weapons with entire ship, if possible. */
    private fun aimShip(attackTarget: ShipAPI, broadside: Broadside): Float {
        val makePlot = fun(w: WeaponAPI): Pair<AutofireAI, Vector2f>? {
            val ai = w.customAI ?: return null
            val intercept = ai.plotIntercept(attackTarget) ?: return null
            return Pair(ai, intercept)
        }

        val solutions: Map<AutofireAI, Vector2f> = broadside.weapons.mapNotNull { makePlot(it) }.toMap()
        val averageFacing: Float = solutions.values.sumOf { (it - ship.location).facing.toDouble() }.toFloat() / solutions.size

        // Aim directly at target if no weapon firing solution is available.
        if (solutions.isEmpty()) (attackTarget.location - ship.location).facing

        var offsetNegative: Float = 0f
        var offsetPositive: Float = 0f
        solutions.forEach {
            val weapon = it.key.weapon
            val intercept = it.value

            // Assume ship is already facing along the average weapon firing solution.
            val arcFacing = weapon.arcFacing + averageFacing
            val interceptFacing = (intercept - ship.location).facing

            // Check if firing solution is inside weapon arc.
            val halfArc = weapon.arc / 2f
            val angleToArc = MathUtils.getShortestRotation(arcFacing, interceptFacing)
            if (abs(angleToArc) < halfArc) return@forEach

            // Angle to nearest arc boundary.
            val angleArc1 = MathUtils.getShortestRotation(arcFacing + halfArc, interceptFacing)
            val angleArc2 = MathUtils.getShortestRotation(arcFacing - halfArc, interceptFacing)
            val angleToIntercept = if (abs(angleArc1) > abs(angleArc2)) angleArc2
            else angleArc1

            // Calculate offset if firing solution is outside weapon firing arc.
            when {
                angleToIntercept < 0f -> offsetNegative = min(offsetNegative, angleToIntercept)
                angleToIntercept > 0f -> offsetPositive = max(offsetPositive, angleToIntercept)
            }
        }

        // Calculate ship facing offset from average firing solution facing.
        val offset = when {
            offsetPositive == 0f -> offsetNegative

            offsetNegative == 0f -> offsetPositive

//            abs(offsetNegative) > abs(offsetPositive) -> offsetPositive

            // Return average facing when there are no offsets
            // or there are conflicting offsets.
            else -> offsetNegative
        }

//        debugPrint["avg"] = "avg ${averageFacing}"
//        debugPrint["pos"] = "pos ${offsetPositive}"
//        debugPrint["neg"] = "neg ${offsetNegative}"

        return averageFacing + offset + 1f * offset.sign
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

        val limits: MutableList<EngineController.Limit?> = mutableListOf()

        limits.add(avoidBlockingLineOfFire(dt, friendlies))
        limits.add(avoidBorder())
        limits.addAll(avoidCollisions(dt, friendlies))

        return limits.filterNotNull()
    }

    private fun avoidCollisions(dt: Float, friendlies: List<ShipAPI>): List<EngineController.Limit?> {
        return friendlies.map { obstacle -> vMaxToObstacle(dt, obstacle) }
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

    private fun vMaxToObstacle(dt: Float, obstacle: ShipAPI): EngineController.Limit? {
        val direction = obstacle.location - ship.location
        val dirFacing = direction.facing
        val distance = direction.length - (ai.stats.totalCollisionRadius + obstacle.totalCollisionRadius + Preset.collisionBuffer)

        // Already colliding.
        if (distance <= 0f) return EngineController.Limit(dirFacing, 0f)

        val vObstacle = vectorProjectionLength(obstacle.velocity, direction)
        val aObstacle = vectorProjectionLength(combatState().accelerationTracker[obstacle], direction)
        val decelShip = ship.collisionDeceleration(dirFacing)

        val vMax: Float = when {
            // Take obstacle acceleration into account when the obstacle is doing a brake check.
            // The acceleration is approximated as total velocity loss. Including actual
            // acceleration (shipAcc + aAbs) in calculations leads to erratic behavior.
            vObstacle > 0f && aObstacle < 0f -> {
                vMax(dt, distance, decelShip)
            }

            // Obstacle is moving towards the ship. If the obstacle is a friendly,
            // non flamed out ship, assume both ship will try to avoid the collision.
            vObstacle < 0f && obstacle.owner == ship.owner && !obstacle.engineController.isFlamedOut -> {
                val decelObstacle = obstacle.collisionDeceleration(-dirFacing)
                val decelDistObstacle = decelerationDist(dt, -vObstacle, decelObstacle)

                vMax(dt, distance - decelDistObstacle, decelShip)
            }

            // Maximum velocity that will not lead to collision with inert obstacle.
            else -> vMax(dt, distance, decelShip) + vObstacle
        }

        return if (vMax > ship.maxSpeed) null
        else EngineController.Limit(dirFacing, vMax)
    }

    /** Ship deceleration for collision avoidance purposes. */
    private fun ShipAPI.collisionDeceleration(collisionFacing: Float): Float {
        val angleFromBow = abs(MathUtils.getShortestRotation(facing, collisionFacing))
        return when {
            angleFromBow < 30f -> deceleration
            angleFromBow < 150f -> strafeAcceleration
            else -> acceleration
        }
    }
}
