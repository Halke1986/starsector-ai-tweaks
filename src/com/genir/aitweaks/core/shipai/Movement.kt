package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand.USE_SYSTEM
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.Preset.Companion.hulkSizeFactor
import com.genir.aitweaks.core.shipai.autofire.BallisticTarget
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.Rotation.Companion.rotated
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*

@Suppress("MemberVisibilityCanBePrivate")
class Movement(override val ai: CustomShipAI) : AttackCoordinator.Coordinable {
    private val ship: ShipAPI = ai.ship
    private val engineController: EngineController = EngineController(ship)

    var headingPoint: Vector2f = Vector2f()
    var expectedVelocity: Vector2f = Vector2f()
    var expectedFacing: Float = ship.facing

    // Used for communication with attack coordinator.
    override var proposedHeadingPoint: Vector2f? = null
    override var reviewedHeadingPoint: Vector2f? = null

    private val interpolateHeading = InterpolateValue()
    private val interpolateFacing = InterpolateValue()

    // Make strafe rotation direction random, but consistent for a given ship.
    private val strafeRotation = Rotation(if (ship.id.hashCode() % 2 == 0) 10f else -10f)

    fun advance(dt: Float) {
        setHeading(dt, ai.maneuverTarget)
        setFacing(dt)
        manageMobilitySystems()
    }

    private fun setHeading(dt: Float, maneuverTarget: ShipAPI?) {
        val systemOverride: Vector2f? = ai.systemAI?.overrideHeading()
        val backoffOverride: Vector2f? = ai.backoff.overrideHeading(maneuverTarget)
        val navigateTo: Vector2f? = ai.assignment.navigateTo

        headingPoint = interpolateHeading.advance(dt) {
            when {
                // Let movement system determine ship heading.
                systemOverride != null -> {
                    systemOverride
                }

                // For player ships the heading to assignment location takes priority.
                ship.owner == 0 && !ship.isAlly && navigateTo != null && (ai.threatVector.isZero || !ai.assignment.arrivedAt) -> {
                    navigateTo
                }

                // Hand over control to backoff module when the ship is backing off.
                backoffOverride != null -> {
                    backoffOverride
                }

                // Orbit target at effective weapon range.
                // Rotate away from threat if there are multiple enemy ships around.
                // Chase the target if there are no other enemy ships around.
                // Strafe target randomly if in range and no other threat.
                maneuverTarget != null -> {
                    // Let the attack coordinator review the calculated heading point.
                    val attackVector = calculateAttackVector(maneuverTarget)
                    proposedHeadingPoint = maneuverTarget.location + attackVector.resized(ai.attackRange)

                    val headingPoint = reviewedHeadingPoint ?: proposedHeadingPoint!!

                    avoidHulks(maneuverTarget, headingPoint) ?: headingPoint
                }

                // Move directly to assignment location.
                navigateTo != null -> {
                    navigateTo
                }

                // Nothing to do, stop the ship.
                else -> EngineController.allStop

            }.copy // Copy to avoid relying on changing value.
        }

        expectedVelocity = engineController.heading(dt, headingPoint, gatherSpeedLimits(dt))
    }

    private fun setFacing(dt: Float) {
        val systemOverride: Float? = ai.systemAI?.overrideFacing()
        val currentAttackTarget: CombatEntityAPI? = ai.finishBurstTarget ?: ai.attackTarget
        val weaponGroup = if (currentAttackTarget == ai.attackTarget) ai.attackingGroup
        else ai.finishBurstWeaponGroup!!

        expectedFacing = interpolateFacing.advance(dt) {
            val newFacing: Float = when {
                // Let movement system determine the ship facing.
                systemOverride != null -> {
                    systemOverride
                }

                // Face movement target location.
                ai.threatVector.isZero && ai.assignment.navigateTo != null -> {
                    // If already at the assignment location, face the center of the map.
                    val lookAt = if (ai.assignment.arrivedAt) Vector2f()
                    else ai.assignment.navigateTo!!

                    (lookAt - ship.location).facing
                }

                // Face the attack target.
                currentAttackTarget != null -> {
                    val ballisticTarget = BallisticTarget.entity(currentAttackTarget)
                    weaponGroup.attackFacing(ballisticTarget)
                }

                // Face threat vector when no target.
                ai.threatVector.isNonZero -> {
                    (-ai.threatVector).facing
                }

                // Nothing to do. Stop rotation.
                else -> EngineController.rotationStop
            }

            Vector2f(newFacing, 0f)
        }.x

        engineController.facing(dt, expectedFacing)
    }

    private fun calculateAttackVector(maneuverTarget: ShipAPI): Vector2f {
        // If the ship is near the navigation objective, it should
        // position itself between the objective and the target.
        ai.assignment.navigateTo?.let { return maneuverTarget.location - it }

        // Strafe away from map border, prefer the map center.
        val engine = Global.getCombatEngine()
        val borderDistX = (ship.location.x * ship.location.x) / (engine.mapWidth * engine.mapWidth * 0.25f)
        val borderDistY = (ship.location.y * ship.location.y) / (engine.mapHeight * engine.mapHeight * 0.25f)
        val borderWeight = max(borderDistX, borderDistY) * 2f

        // Try to move away from the threat.
        val attackVector = -(ai.threatVector + ship.location.resized(borderWeight))

        // Strafe the target randomly, when it's the only threat.
        val shouldStrafe = ai.is1v1 && ai.range(maneuverTarget) <= ai.attackingGroup.effectiveRange
        return if (!shouldStrafe) attackVector.rotated(strafeRotation)
        else attackVector
    }

    private fun avoidHulks(maneuverTarget: CombatEntityAPI, proposedHeading: Vector2f): Vector2f? {
        val toHeadingVector = proposedHeading - maneuverTarget.location
        val toHeadingAngle = toHeadingVector.facing
        val dist = toHeadingVector.length

        val hulks: Sequence<ShipAPI> = Grid.ships(maneuverTarget.location, dist).filter {
            when {
                !it.isHulk -> false

                it.isFighter -> false

                abs(getShortestRotation(it.location, maneuverTarget.location, proposedHeading)) > 90f -> false

                (maneuverTarget.location - it.location).length > dist -> false

                else -> true
            }
        }

        val arcs: List<Arc> = hulks.map { hulk ->
            val toHulk: Vector2f = hulk.location - maneuverTarget.location
            val shipSize = angularSize(toHulk.lengthSquared, ship.collisionRadius)
            val arc: Float = angularSize(toHulk.lengthSquared, hulk.collisionRadius + shipSize / 2f)

            val facing: Float = toHulk.facing
            Arc(min(arc, 90f), facing)
        }.toList()

        val mergedArcs = Arc.mergeOverlapping(arcs)
        val obstacle = mergedArcs.firstOrNull { it.contains(toHeadingAngle) } ?: return null

        val angle1 = obstacle.facing - (obstacle.angle / 2f)
        val angle2 = obstacle.facing + (obstacle.angle / 2f)

        val toShipAngle = (ship.location - maneuverTarget.location).facing
        val offset1 = absShortestRotation(toShipAngle, angle1)
        val offset2 = absShortestRotation(toShipAngle, angle2)

        val newAngle = if (offset1 < offset2) angle1 else angle2

        val newHeadingPoint = unitVector(newAngle).resized(dist) + maneuverTarget.location
        return newHeadingPoint
    }

    private fun manageMobilitySystems() {
        when (ship.system?.specAPI?.AIType) {

            ShipSystemAIType.BURN_DRIVE -> {
                // Prevent vanilla AI from jumping closer to target with
                // BURN_DRIVE, if the target is already within weapons range.
                if (ai.attackTarget != null && ai.range(ai.attackTarget!!) < ai.attackingGroup.effectiveRange) {
                    ship.blockCommandForOneFrame(USE_SYSTEM)
                }

                if (ai.backoff.isBackingOff) ship.blockCommandForOneFrame(USE_SYSTEM)
            }

            else -> Unit
        }
    }

    private fun gatherSpeedLimits(dt: Float): List<EngineController.Limit> {
        val allObstacles = Global.getCombatEngine().ships.filter {
            when {
                it.root == ship.root -> false
                it.collisionClass != CollisionClass.SHIP -> false

                // Modules and drones count towards
                // their parent collision radius.
                it.isModule -> false
                it.isDrone -> false

                else -> true
            }
        }

        val friendlies = allObstacles.filter { it.owner == ship.owner }
        val hulks = allObstacles.filter { it.owner == 100 && it.mass / ship.mass > hulkSizeFactor }

        val limits: MutableList<EngineController.Limit?> = mutableListOf()

        limits.add(avoidBlockingLineOfFire(dt, friendlies))
        limits.add(avoidBorder())
        limits.add(avoidTargetCollision(dt))
        limits.addAll(avoidCollisions(dt, friendlies + hulks))

        return limits.filterNotNull()
    }

    /** Do not ram friendly ships. */
    private fun avoidCollisions(dt: Float, friendlies: List<ShipAPI>): List<EngineController.Limit?> {
        return friendlies.map { obstacle ->
            val distance = ai.stats.totalCollisionRadius + obstacle.totalCollisionRadius + Preset.collisionBuffer
            vMaxToObstacle(dt, obstacle, distance)
        }
    }

    /** Do not ram the maneuver target. */
    private fun avoidTargetCollision(dt: Float): EngineController.Limit? {
        val target = ai.maneuverTarget ?: return null

        // Do not be paralyzed by frigates when trying to back off.
        if (ai.backoff.isBackingOff && target.root.isFrigate) return null

        val distance = max(ai.stats.totalCollisionRadius + target.totalCollisionRadius + Preset.collisionBuffer, ai.attackRange * 0.8f)
        return vMaxToObstacle(dt, target, distance)
    }

    private fun avoidBlockingLineOfFire(dt: Float, allies: List<ShipAPI>): EngineController.Limit? {
        val target = ai.attackTarget ?: return null
        val ais = allies.mapNotNull { it.customShipAI }

        // Blocking line of fire occurs mostly among ships attacking the same target.
        // For simplicity, the AI will try to avoid only those cases of blocking.
        val squad = ais.filter { it.attackTarget == ai.attackTarget }
        if (squad.isEmpty()) return null

        // Calculations are done in target frame of reference.
        val lineOfFire = ship.location - target.location
        val facing = lineOfFire.facing
        val distToTarget = lineOfFire.length

        val velocityFacing = (ship.location + ship.velocity - target.location).facing
        val angleToVelocity = shortestRotation(facing, velocityFacing)

        var maxLimit: EngineController.Limit? = null

        squad.forEach { obstacle ->
            val obstacleLineOfFire = obstacle.ship.location - target.location
            val obstacleFacing = obstacleLineOfFire.facing
            val angleToOtherLine = shortestRotation(facing, obstacleFacing)

            val blocked = if (obstacleLineOfFire.lengthSquared < lineOfFire.lengthSquared) ai
            else obstacle

            when {
                blocked.backoff.isBackingOff -> return@forEach

                // Too far from obstacle line of fire to consider blocking.
                absShortestRotation(facing, obstacleFacing) >= 90f -> return@forEach

                // Ship is moving away from the obstacle.
                angleToVelocity.sign != angleToOtherLine.sign -> return@forEach

                // Do not consider line of fire blocking if target is out of range, with 1.2 tolerance factor.
                blocked.range(target) > blocked.attackingGroup.maxRange * 1.2f -> return@forEach
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
                val obstacleV = obstacle.ship.timeAdjustedVelocity
                val obstacleAngularV = vectorRejection(obstacleV, obstacleLineOfFire)

                val t = timeToOrigin(obstacle.ship.location - ship.location, obstacleAngularV)
                val obstacleVComponent = obstacleAngularV.length * t.sign

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
        val d = (borderIntrusion.length - Preset.borderCornerRadius).coerceAtLeast(0f)

        // Ship is far from border, no avoidance required.
        if (d == 0f) return null

        // Allow chasing targets into the border zone.
        val tgtLoc = ai.maneuverTarget?.location
        if (tgtLoc != null && !ai.backoff.isBackingOff && abs(getShortestRotation(tgtLoc - ship.location, borderIntrusion)) < 90f) {
            return null
        }

        ai.isAvoidingBorder = true

        // The closer the ship is to map edge, the stronger
        // the heading transformation away from the border.
        val avoidForce = (d / (Preset.borderNoGoZone - Preset.borderHardNoGoZone)).coerceAtMost(1f)
        return EngineController.Limit(borderIntrusion.facing, ship.maxSpeed * (1f - avoidForce))
    }

    private fun vMaxToObstacle(dt: Float, obstacle: ShipAPI, distance: Float): EngineController.Limit? {
        val direction = obstacle.location - ship.location
        val dirFacing = direction.facing
        val distanceLeft = direction.length - distance

        // Already colliding.
        if (distanceLeft <= 0f) return EngineController.Limit(dirFacing, 0f)

        val vObstacle = vectorProjectionLength(obstacle.timeAdjustedVelocity, direction)
        val aObstacle = vectorProjectionLength(state.accelerationTracker[obstacle], direction)
        val decelShip = ship.collisionDeceleration(dirFacing)

        val vMax: Float = when {
            // Take obstacle acceleration into account when the obstacle is doing a brake check.
            // The acceleration is approximated as total velocity loss. Including actual
            // acceleration (shipAcc + aAbs) in calculations leads to erratic behavior.
            vObstacle > 0f && aObstacle < 0f -> {
                vMax(dt, distanceLeft, decelShip)
            }

            // Obstacle is moving towards the ship. If the obstacle is a friendly,
            // non flamed out ship, assume both ship will try to avoid the collision.
            vObstacle < 0f && obstacle.owner == ship.owner && !obstacle.engineController.isFlamedOut -> {
                val decelObstacle = obstacle.collisionDeceleration(-dirFacing)
                val decelDistObstacle = decelerationDist(dt, -vObstacle, decelObstacle)

                vMax(dt, distanceLeft - decelDistObstacle, decelShip)
            }

            // Maximum velocity that will not lead to collision with inert obstacle.
            else -> vMax(dt, distanceLeft, decelShip) + vObstacle
        }

        return if (vMax > ship.maxSpeed) null
        else EngineController.Limit(dirFacing, vMax)
    }

    /** Ship deceleration for collision avoidance purposes. */
    private fun ShipAPI.collisionDeceleration(collisionFacing: Float): Float {
        val angleFromBow = absShortestRotation(facing, collisionFacing)
        return when {
            angleFromBow < 30f -> deceleration
            angleFromBow < 150f -> strafeAcceleration
            else -> acceleration
        }
    }

    /** Maximum velocity in given direction to not overshoot target. */
    private fun vMax(dt: Float, dist: Float, deceleration: Float): Float {
        val (q, _) = quad(0.5f, 0.5f, -dist / (deceleration * dt * dt)) ?: return 0f
        return floor(q) * deceleration * dt
    }

    /** Distance covered by ship when decelerating from given velocity. */
    private fun decelerationDist(dt: Float, velocity: Float, deceleration: Float): Float {
        val v = velocity * dt
        val a = deceleration * dt * dt

        val t = ceil(v / a)
        return (v + a) * t * 0.5f
    }

    /**
     * The advance() method for fast-time ships is invoked multiple times per frame,
     * whereas normal-time ships have their coordinates updated only once per frame.
     *
     * This discrepancy can cause fast-time ships to calculate their movement
     * based on target coordinates that may appear to change erratically,
     * resulting in imprecise movement commands.
     *
     * To address this issue, the movement of fast-time ships is also calculated
     * once per frame. The resulting movement is then interpolated for any additional
     * advance() calls within the same frame, ensuring smoother and more precise movement.
     */
    private inner class InterpolateValue {
        private var prevValue: Vector2f = Vector2f()
        private var value: Vector2f = Vector2f()

        private var timestamp: Int = 0
        private var dtSum: Float = 0f

        fun advance(dt: Float, nextValue: () -> Vector2f): Vector2f {
            val timeMult: Float = ship.mutableStats.timeMult.modifiedValue

            if (state.frameCount > timestamp) {
                timestamp = state.frameCount
                dtSum = 0f

                prevValue = value
                value = nextValue()
            }

            // No need to interpolate for ships in normal time flow.
            if (timeMult == 1f) return value

            dtSum += dt

            val delta = (value - prevValue) / timeMult
            return prevValue + delta * (dtSum / Global.getCombatEngine().elapsedInLastFrame)
        }
    }
}
