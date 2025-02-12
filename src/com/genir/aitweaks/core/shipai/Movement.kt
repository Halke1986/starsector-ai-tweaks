package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.Preset.Companion.hulkSizeFactor
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*

@Suppress("MemberVisibilityCanBePrivate")
class Movement(override val ai: CustomShipAI) : AttackCoordinator.Coordinable {
    private val ship: ShipAPI = ai.ship
    private val engineController: EngineController = EngineController(ship)

    var headingPoint: Vector2f = Vector2f()
    var expectedVelocity: Vector2f = Vector2f()
    var expectedFacing: Direction = ship.facing.direction

    // Used for communication with attack coordinator.
    override var proposedHeadingPoint: Vector2f? = null
    override var reviewedHeadingPoint: Vector2f? = null

    private val interpolateHeading = InterpolateMovement<Vector2f>(ship)
    private val interpolateFacing = InterpolateMovement<Direction>(ship)

    // Make strafe rotation direction random, but consistent for the given ship.
    private val strafeRotation = RotationMatrix(if (this.hashCode() % 2 == 0) 10f else -10f)

    fun advance(dt: Float) {
        setHeading(dt, ai.maneuverTarget)
        setFacing(dt)
    }

    private fun setHeading(dt: Float, maneuverTarget: ShipAPI?) {
        val systemOverride: Vector2f? = ai.systemAI?.overrideHeading()
        val backoffOverride: Vector2f? = ai.ventModule.overrideHeading(maneuverTarget)
        val navigateTo: Vector2f? = ai.assignment.navigateTo

        val newHeadingPoint: Vector2f? = interpolateHeading.advance(dt) {
            when {
                // Let movement system determine ship heading.
                systemOverride != null -> {
                    systemOverride
                }

                // Heading to assignment location takes priority.
                navigateTo != null && (ai.threatVector.isZero || !ai.assignment.arrivedAt) -> {
                    navigateTo
                }

                // Hand over control to backoff module when the ship is backing off.
                backoffOverride != null -> {
                    backoffOverride
                }

                // Orbit target at the effective weapon range.
                // Rotate away from threat if there are multiple enemy ships around.
                // Chase the target if there are no other enemy ships around.
                // Strafe target randomly if in range and no other threat.
                maneuverTarget != null -> {
                    calculateAttackLocation(maneuverTarget)
                }

                // Nothing to do, stop the ship.
                else -> null

            }?.copy // Copy to avoid relying on changing value.
        }

        val shouldStop = newHeadingPoint == null
        headingPoint = newHeadingPoint ?: ship.location

        expectedVelocity = engineController.heading(dt, headingPoint, shouldStop, gatherSpeedLimits(dt))
    }

    private fun setFacing(dt: Float) {
        val systemOverride: Direction? = ai.systemAI?.overrideFacing()
        val currentAttackTarget: CombatEntityAPI? = ai.finishBurstTarget ?: ai.attackTarget
        val weaponGroup = if (currentAttackTarget == ai.attackTarget) ai.attackingGroup
        else ai.finishBurstWeaponGroup!!

        val newExpectedFacing: Direction? = interpolateFacing.advance(dt) {
            when {
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
                    weaponGroup.attackFacing(currentAttackTarget)
                }

                // Face threat vector when no target.
                ai.threatVector.isNonZero -> {
                    (-ai.threatVector).facing
                }

                // Nothing to do. Stop rotation.
                else -> null
            }
        }

        val shouldStop = newExpectedFacing == null
        expectedFacing = newExpectedFacing ?: ship.facing.direction

        engineController.facing(dt, expectedFacing, shouldStop)
    }

    private fun calculateAttackLocation(maneuverTarget: ShipAPI): Vector2f {
        // If the ship is near the navigation objective, it should
        // position itself between the objective and the target.
        ai.assignment.navigateTo?.let { return maneuverTarget.location - it }

        val targetRoot = maneuverTarget.root

        val isAttackingStation = targetRoot.isStation && maneuverTarget.isModule && targetRoot != maneuverTarget
        val attackVector: Vector2f = when {
            // When attacking a station, avoid positioning the ship
            // with station bulk obstructing the targeted module.
            isAttackingStation -> {
                maneuverTarget.location - targetRoot.location
            }

            // Move straight to the target if ship is an assault ship.
            ai.isAssaultShip -> {
                ship.location - maneuverTarget.location
            }

            // If the enemy is retreating, attempt to intercept and block its escape route.
            Global.getCombatEngine().getFleetManager(ship.owner xor 1).getTaskManager(false).isInFullRetreat -> {
                when (ship.owner) {
                    0 -> Vector2f(0f, 1f)
                    else -> Vector2f(0f, -1f)
                }
            }

            // Move straight to the target if no threat. This may happen
            // if the target is far away, beyond the threat search radius.
            ai.threatVector.isZero -> {
                ship.location - maneuverTarget.location
            }

            // If the target is far away, move directly toward it instead of using the local threat vector.
            // This is because the threat vector near the target may differ from the one in the current location.
            maneuverTarget !in ai.threats -> {
                ship.location - maneuverTarget.location
            }

            // Strafe the target randomly, when it's the only threat.
            ai.is1v1 && ai.currentEffectiveRange(maneuverTarget) <= ai.attackingGroup.effectiveRange -> {
                -ai.threatVector.rotated(strafeRotation)
            }

            // Try to position away from the threat.
            else -> {
                -ai.threatVector
            }
        }

        val attackLocation = preferMapCenter(attackVector).resized(ai.attackRange) + maneuverTarget.location
        val coordinatedAttackLocation = coordinateAttackLocation(maneuverTarget, attackLocation)

        // Assault ships don't need angle adjustment, as it interferes with the burn drive.
        if (ai.isAssaultShip && !isAttackingStation) {
            return coordinatedAttackLocation
        }

        return avoidGoingThroughTarget(maneuverTarget, coordinatedAttackLocation)
    }

    /** Take into account other entities when planning ship attack location. */
    fun coordinateAttackLocation(maneuverTarget: ShipAPI, attackLocation: Vector2f): Vector2f {
        // Coordinate the attack with allied ships.
        proposedHeadingPoint = attackLocation
        var adjustedAttackLocation = reviewedHeadingPoint ?: attackLocation

        // Coordinate the attack to avoid hulks. Assault ships don't try
        // to avoid hulks, as this interferes with coordinating burn drives.
        // The exception is when the assault ship operates alone.
        val isAlone = reviewedHeadingPoint == null
        if (!ai.isAssaultShip || isAlone) {
            adjustedAttackLocation = avoidHulks(maneuverTarget, adjustedAttackLocation) ?: adjustedAttackLocation
        }

        return adjustedAttackLocation
    }

    /** Strafe away from map border, prefer the map center. */
    private fun preferMapCenter(approachVector: Vector2f): Vector2f {
        val engine = Global.getCombatEngine()
        val borderDistX = (ship.location.x * ship.location.x) / (engine.mapWidth * engine.mapWidth * 0.25f)
        val borderDistY = (ship.location.y * ship.location.y) / (engine.mapHeight * engine.mapHeight * 0.25f)
        val borderWeight = max(borderDistX, borderDistY) * 2f
        return approachVector.resized(1f) - ship.location.resized(borderWeight)
    }

    /** Adjust the ship's heading to avoid positioning with hulks obstructing its target. */
    private fun avoidHulks(maneuverTarget: CombatEntityAPI, proposedHeading: Vector2f): Vector2f? {
        val toHeadingVector = proposedHeading - maneuverTarget.location
        val toHeadingAngle = toHeadingVector.facing
        val dist = toHeadingVector.length

        val hulks: Sequence<ShipAPI> = Grid.ships(maneuverTarget.location, dist).filter {
            when {
                !it.isHulk -> false

                it.isFighter -> false

                getShortestRotation(it.location, maneuverTarget.location, proposedHeading).length > 90f -> false

                (maneuverTarget.location - it.location).length > dist -> false

                else -> true
            }
        }

        val arcs: List<Arc> = hulks.map { hulk ->
            val toHulk: Vector2f = hulk.location - maneuverTarget.location
            val shipSize = angularSize(toHulk.lengthSquared, ship.collisionRadius)
            val arc: Float = angularSize(toHulk.lengthSquared, hulk.collisionRadius + shipSize / 2f)

            val facing: Direction = toHulk.facing
            Arc(min(arc, 90f), facing)
        }.toList()

        val mergedArcs = Arc.mergeOverlapping(arcs)
        val obstacle = mergedArcs.firstOrNull { it.contains(toHeadingAngle) } ?: return null

        val angle1 = obstacle.facing - (obstacle.angle / 2f)
        val angle2 = obstacle.facing + (obstacle.angle / 2f)

        val toShipAngle = (ship.location - maneuverTarget.location).facing
        val offset1 = (angle1 - toShipAngle).length
        val offset2 = (angle2 - toShipAngle).length

        val newAngle = if (offset1 < offset2) angle1 else angle2

        val newHeadingPoint = (newAngle).unitVector.resized(dist) + maneuverTarget.location
        return newHeadingPoint
    }

    /** Cap the maximum angle between current ship location and
     * planned attack location. Otherwise, the ship may approach
     * the maneuver target too close.*/
    private fun avoidGoingThroughTarget(maneuverTarget: ShipAPI, attackLocation: Vector2f): Vector2f {
        // Do calculations in target frame of reference.
        val toShip = ship.location - maneuverTarget.location
        val toAttackLocation = attackLocation - maneuverTarget.location
        val dist = toShip.length

        // Ship is already too close to target. Cap the angle to avoid getting even closer.
        if (dist <= ai.attackRange) {
            val maxAngle = 15f
            val angle = toShip.facing - toAttackLocation.facing
            if (angle.length > maxAngle) {
                val rotation = (angle * (1f - maxAngle / angle.length)).rotationMatrix
                return toAttackLocation.rotated(rotation) + maneuverTarget.location
            }

            return attackLocation
        }

        // If heading directly to the attack location would bring the ship too close to the target,
        // instead, navigate to a tangential point on the attack radius around the target.
        val cosTangent: Float = ai.attackRange / dist
        val cosTarget: Float = dotProduct(toAttackLocation, toShip) / (ai.attackRange * dist)
        if (cosTangent <= cosTarget) {
            return attackLocation
        }

        val points: Pair<Vector2f, Vector2f> = pointsOfTangency(-toShip, ai.attackRange)!!
        val point1Dir = (points.first + toShip).facing - toAttackLocation.facing
        val point2Dir = (points.second + toShip).facing - toAttackLocation.facing

        return ship.location + if (point1Dir.length < point2Dir.length) points.first else points.second
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

        limits.addAll(avoidBlockingLineOfFire(dt, friendlies))
        limits.addAll(avoidCollisions(dt, friendlies + hulks))
        limits.add(avoidBorder())

        return limits.filterNotNull()
    }

    /** Do not ram friendly ships. */
    private fun avoidCollisions(dt: Float, friendlies: List<ShipAPI>): List<EngineController.Limit?> {
        return friendlies.map { obstacle ->
            val distance = ai.stats.totalCollisionRadius + obstacle.totalCollisionRadius + Preset.collisionBuffer
            vMaxToObstacle(dt, obstacle, distance)
        }
    }

    /** Position the ship to avoid blocking allied ships' lines of fire
     * on the same target while keeping its own line of fire clear. */
    private fun avoidBlockingLineOfFire(dt: Float, allies: List<ShipAPI>): List<EngineController.Limit?> {
        // Don't care about lines of fire when backing off.
        if (ai.ventModule.isBackingOff) {
            return listOf()
        }

        val target = ai.attackTarget ?: return listOf()

        // Blocking line of fire occurs mostly among ships attacking the same target.
        // For simplicity, the AI will try to avoid only those cases of blocking.
        val ais = allies.mapNotNull { it.customShipAI }
        val squad = ais.filter { it.attackTarget == ai.attackTarget }
        if (squad.isEmpty()) {
            return listOf()
        }

        // Calculations are done in target frame of reference. Obstacle
        // is assumed to be stationary to simplify the calculations.
        val shipLineOfFire = ship.location - target.location
        val shipFacing = shipLineOfFire.facing
        val distToTarget = shipLineOfFire.length
        val shipW = angularVelocity(shipLineOfFire, ship.timeAdjustedVelocity)

        var maxLimitLeft: EngineController.Limit? = null
        var maxLimitRight: EngineController.Limit? = null

        squad.forEach { obstacle: CustomShipAI ->
            val obstacleLineOfFire = obstacle.ship.location - target.location
            val obstacleFacing = obstacleLineOfFire.facing
            val angleToObstacle = obstacleFacing - shipFacing

            // Consider the ship farther from the target as potentially being blocked.
            val blocked = if (obstacleLineOfFire.lengthSquared < shipLineOfFire.lengthSquared) ai
            else obstacle

            when {
                // Blocking the line of fire of a backing-off ship is not only allowed
                // but beneficial, as it may provide cover from enemy fire.
                blocked.ventModule.isBackingOff -> return@forEach

                // Too far from obstacle line of fire to consider blocking.
                angleToObstacle.length >= 90f -> return@forEach

                // Ship is moving away from the obstacle.
                angleToObstacle.sign != shipW.sign -> return@forEach

                // Do not consider line of fire blocking if target is out of range, with a tolerance factor.
                blocked.currentEffectiveRange(target) > blocked.attackingGroup.maxRange * 2.5f -> return@forEach
            }

            val arcLength = distToTarget * angleToObstacle.length * DEGREES_TO_RADIANS
            val minDist = (ai.stats.totalCollisionRadius + obstacle.stats.totalCollisionRadius) * 1.1f
            val distance = arcLength - minDist

            // Max speed towards obstacle line of fire.
            val vMax: Float = if (distance < 0f) {
                0f // Already blocking.
            } else {
                val obstacleW = angularVelocity(obstacleLineOfFire, obstacle.ship.timeAdjustedVelocity)
                val obstacleVComponent = obstacleW / obstacleLineOfFire.length
                val obstacleVDirection = if (angleToObstacle.sign == obstacleW.sign) 1
                else -1

                vMax(dt, distance, ship.strafeAcceleration) + obstacleVDirection * obstacleVComponent
            }

            // Line-of-fire blocking occurs when ships orbiting the same target
            // strafe in front of each other. To prevent this, a speed limit is applied
            // only to the strafing velocity. This ensures that only the most restrictive
            // speed limit is used, as all calculated limits are parallel.
            //
            // Additionally, the velocity limits are rotated slightly (by a few degrees)
            // away from the heading. This allows the ship to keep moving toward the target
            // even when constrained by opposing limits.
            if (angleToObstacle.sign > 0) {
                if (maxLimitRight == null || maxLimitRight!!.speed > vMax) {
                    maxLimitRight = EngineController.Limit(shipFacing + 88f, vMax)
                }
            } else {
                if (maxLimitLeft == null || maxLimitLeft!!.speed > vMax) {
                    maxLimitLeft = EngineController.Limit(shipFacing - 88f, vMax)
                }
            }
        }

        return listOf(maxLimitRight, maxLimitLeft)
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

        val borderIntrusion = Vector2f(
            if (l.x > 0) (l.x - mapW + borderZone).coerceAtLeast(0f)
            else (l.x + mapW - borderZone).coerceAtMost(0f),

            if (l.y > 0) (l.y - mapH + borderZone).coerceAtLeast(0f)
            else (l.y + mapH - borderZone).coerceAtMost(0f),
        )

        // Distance into the border zone.
        val d = (borderIntrusion.length - Preset.borderCornerRadius).coerceAtLeast(0f)

        // Ship is far from border, no avoidance required.
        if (d == 0f) {
            return null
        }

        // Allow chasing targets into the border zone.
        val tgtLoc = ai.maneuverTarget?.location
        if (tgtLoc != null && !ai.ventModule.isBackingOff && (getShortestRotation(tgtLoc - ship.location, borderIntrusion)).length < 90f) {
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
    private fun ShipAPI.collisionDeceleration(collisionFacing: Direction): Float {
        val angleFromBow = (collisionFacing - facing.direction).length
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
}
