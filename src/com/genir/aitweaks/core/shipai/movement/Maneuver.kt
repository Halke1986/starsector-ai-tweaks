package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.Assignment
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.global.AttackCoordinator
import com.genir.aitweaks.core.shipai.movement.Movement.Companion.movement
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection
import com.genir.aitweaks.core.utils.types.RotationMatrix
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotated
import org.lwjgl.util.vector.Vector2f

@Suppress("MemberVisibilityCanBePrivate")
class Maneuver(val ai: CustomShipAI) {
    private val movement: Movement = ai.ship.movement
    private val engineController: CollisionAwareEngineController = CollisionAwareEngineController(ai, movement)
    private val collisionAvoidance: CollisionAvoidance = CollisionAvoidance(ai)

    private var prevFrameIdx = 0

    // Temporary point used for steering maneuvers. The controller steers
    // toward this point, without changing the final destination.
    private var steeringPoint: Vector2f = Vector2f()
    var destination: Vector2f = Vector2f()

    var expectedVelocity: Vector2f = Vector2f()
    var expectedFacing: Direction = movement.facing

    // Make strafe rotation direction random, but consistent for the given ship.
    private val strafeRotation = RotationMatrix(if (this.hashCode() % 2 == 0) 90f else -90f)

    fun advance(dt: Float) {
        if (prevFrameIdx != ai.globalAI.frameTracker.count) {
            engineController.clearCommands()

            setFacing(dt)
            setHeading(dt, ai.maneuverTarget)
        }

        engineController.executeCommands()

        prevFrameIdx = ai.globalAI.frameTracker.count
    }

    private fun setFacing(dt: Float) {
        val systemOverride: Direction? = ai.systemAI?.overrideFacing()
        val currentAttackTarget: CombatEntityAPI? = ai.finishBurstTarget ?: ai.attackTarget
        val weaponGroup = if (currentAttackTarget == ai.attackTarget) ai.attackingGroup
        else ai.finishBurstWeaponGroup!!

        val newExpectedFacing: Direction? = when {
            // Let movement system determine the ship facing.
            systemOverride != null -> {
                systemOverride
            }

            ai.focusOnNavigating() -> {
                val navigateTo: Vector2f = ai.assignment.navigateTo!!

                val lookAt = when {
                    // Face movement target location.
                    !ai.assignment.arrivedAt -> {
                        navigateTo
                    }

                    // When forming line abreast, face the enemy side of the map.
                    ai.assignment.type == Assignment.Type.NAVIGATE_IN_FORMATION -> {
                        Vector2f(
                            navigateTo.x,
                            movement.location.y + if (movement.ship.owner == 0) 1e3f else -1e3f
                        )
                    }

                    // If already at the assignment location, face the center of the map.
                    else -> {
                        Vector2f()
                    }
                }

                (lookAt - movement.location).facing
            }

            // Face the attack target.
            currentAttackTarget != null -> {
                weaponGroup.shipAttackFacing(currentAttackTarget)
            }

            // Face threat vector when no target.
            ai.threatVector.isNonZero -> {
                (-ai.threatVector).facing
            }

            // Nothing to do. Stop rotation.
            else -> null
        }

        if (newExpectedFacing != null) {
            val prevExpectedFacing: Direction = expectedFacing
            val angularVelocity = (newExpectedFacing - prevExpectedFacing).degrees / dt
            engineController.facing(dt, newExpectedFacing, angularVelocity)
            expectedFacing = newExpectedFacing
        } else {
            engineController.facing(dt, movement.facing, 0f)
            expectedFacing = movement.facing
        }
    }

    private fun setHeading(dt: Float, maneuverTarget: ShipAPI?) {
        val systemOverride: ExpectedManeuver? = ai.systemAI?.overrideHeading()
        val backoffOverride: ExpectedManeuver? = ai.ventModule.overrideHeading(maneuverTarget)
        val navigateTo: Vector2f? = ai.assignment.navigateTo
        val speedLimits = collisionAvoidance.gatherSpeedLimits(dt)

        val expectedManeuver: ExpectedManeuver = when {
            // Let movement system determine ship heading.
            systemOverride != null -> {
                systemOverride
            }

            // Heading to assignment location takes priority.
            navigateTo != null && (ai.threatVector.isZero || !ai.assignment.arrivedAt) -> {
                ExpectedManeuver(navigateTo, null, Vector2f())
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
                calculateAttackLocation(dt, maneuverTarget, speedLimits)
            }

            // Nothing to do, stop the ship.
            else -> {
                ExpectedManeuver(movement.location, null, Vector2f())
            }
        }

        destination.set(expectedManeuver.destination) // Set to avoid relying on changing value.
        steeringPoint.set(expectedManeuver.steeringPoint ?: expectedManeuver.destination)

        expectedVelocity = engineController.heading(dt, steeringPoint, expectedManeuver.velocityAtDestination, speedLimits)
    }

    private fun calculateAttackLocation(dt: Float, maneuverTarget: ShipAPI, speedLimits: List<SpeedLimit>): ExpectedManeuver {
        // If the ship is near the navigation objective, it should
        // position itself between the objective and the target.
        ai.assignment.navigateTo?.let {
            return ExpectedManeuver(maneuverTarget.location - it, null, Vector2f())
        }

        val targetRoot = maneuverTarget.root
        val isAttackingStation = targetRoot.isStation && maneuverTarget.isModule && targetRoot != maneuverTarget
        val directApproach = movement.location - maneuverTarget.location

        val attackVector: Vector2f = when {
            // When attacking a station, avoid positioning the ship
            // with station bulk obstructing the targeted module.
            isAttackingStation -> {
                maneuverTarget.location - targetRoot.location
            }

            // Move straight to the target if ship is an assault ship.
            ai.isAssaultShip -> {
                directApproach
            }

            // If the enemy is retreating, attempt to intercept and block its escape route.
            Global.getCombatEngine().getFleetManager(movement.ship.owner xor 1).getTaskManager(false).isInFullRetreat -> {
                when (movement.ship.owner) {
                    0 -> Vector2f(0f, 1f)
                    else -> Vector2f(0f, -1f)
                }
            }

            // Move straight to the target if no threat. This may happen
            // if the target is far away, beyond the threat search radius.
            ai.threatVector.isZero -> {
                directApproach
            }

            // If the target is far away, move directly toward it instead of using the local threat vector.
            // This is because the threat vector near the target may differ from the one in the current location.
            maneuverTarget !in ai.threats -> {
                directApproach
            }

            ai.is1v1 -> {
                val toShip = movement.location - maneuverTarget.location
                // Chase the target, but only when it's attempting to escape and is faster than the ship.
                if ((toShip.facing - maneuverTarget.velocity.facing).length > 90f && maneuverTarget.velocity.length > movement.maxSpeed * 0.7f) {
                    directApproach
                } else {
                    -ai.threatVector.rotated(strafeRotation)
                }
            }

            // Try to position away from the threat.
            else -> {
                -ai.threatVector
            }
        }

        // Calculate attack location in global coordinates.
        var attackLocation = preferredAttackLocation(attackVector)
        attackLocation = attackLocation.resized(ai.attackRange) + maneuverTarget.location
        attackLocation = coordinateAttackLocation(maneuverTarget, attackLocation)

        return approachTarget(dt, maneuverTarget.movement, attackLocation, speedLimits)
    }

    /** Take into account other entities when planning ship attack location. */
    private fun coordinateAttackLocation(maneuverTarget: ShipAPI, attackLocation: Vector2f): Vector2f {
        // Coordinate the attack with allied ships.
        val coordinator: AttackCoordinator = ai.globalAI.maneuverCoordinator
        val (coordinatedAttackLocation, taskForceSize) = coordinator.coordinateAttack(ai, maneuverTarget, attackLocation)
        var adjustedAttackLocation = coordinatedAttackLocation

        // Coordinate the attack to avoid hulks. Assault ships don't try
        // to avoid hulks, as this interferes with coordinating burn drives.
        // The exception is when the assault ship operates alone.
        val isAlone = taskForceSize == 1
        if (!ai.isAssaultShip || isAlone) {
            adjustedAttackLocation = avoidHulks(maneuverTarget, adjustedAttackLocation) ?: adjustedAttackLocation
        }

        return adjustedAttackLocation
    }

    private fun preferredAttackLocation(approachVector: Vector2f): Vector2f {
        when {
            // Do not modify the approach vector when chasing retreating enemies.
            Global.getCombatEngine().getFleetManager(movement.ship.owner xor 1).getTaskManager(false).isInFullRetreat -> {
                return approachVector
            }

            // Skirmishers should prefer attacking from the sides,
            // where they do not block the line of fire of larger ships.
            movement.ship.isSkirmisher -> {
                return Vector2f(
                    approachVector.x,
                    approachVector.y * 0.66f,
                )
            }

            // Default behavior is to strafe away from the map border and prefer the map center.
            else -> {
                val engine = Global.getCombatEngine()
                val borderDistX = (movement.location.x * movement.location.x) / (engine.mapWidth * engine.mapWidth * 0.25f)
                val borderDistY = (movement.location.y * movement.location.y) / (engine.mapHeight * engine.mapHeight * 0.25f)
                val borderWeight = maxOf(borderDistX, borderDistY) * 2f
                return approachVector.resized(1f) - movement.location.resized(borderWeight)
            }
        }
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
            val shipSize = angularSize(toHulk.lengthSquared, movement.ship.collisionRadius)
            val arc: Float = angularSize(toHulk.lengthSquared, hulk.collisionRadius + shipSize / 2f)

            val facing: Direction = toHulk.facing
            Arc(minOf(arc, 90f), facing)
        }.toList()

        val mergedArcs = Arc.mergeOverlapping(arcs)
        val obstacle = mergedArcs.firstOrNull { it.contains(toHeadingAngle) } ?: return null

        val angle1 = obstacle.facing - obstacle.halfAngle.toDirection
        val angle2 = obstacle.facing + obstacle.halfAngle.toDirection

        val toShipAngle = (movement.location - maneuverTarget.location).facing
        val offset1 = (angle1 - toShipAngle).length
        val offset2 = (angle2 - toShipAngle).length

        val newAngle = if (offset1 < offset2) angle1 else angle2

        val newHeadingPoint = (newAngle).unitVector.resized(dist) + maneuverTarget.location
        return newHeadingPoint
    }

    private fun approachTarget(
        dt: Float,
        maneuverTarget: Movement,
        attackLocation: Vector2f,
        speedLimits: List<SpeedLimit>,
    ): ExpectedManeuver {
        // Perform calculations in target frame of reference.
        val toShip = movement.location - maneuverTarget.location
        val toAttackLocation = attackLocation - maneuverTarget.location

        // Ship is close enough to target to start orbiting it.
        val dist = toShip.length
        if (dist < maxOf(maneuverTarget.ship.maxRange, ai.attackRange * 1.2f)) {
            return orbitTarget(dt, maneuverTarget, attackLocation, speedLimits)
        }

        val pointsOfTangency: Pair<Vector2f, Vector2f> = pointsOfTangency(-toShip, ai.attackRange)
            ?: return orbitTarget(dt, maneuverTarget, attackLocation, speedLimits)

        val cosTangent: Float = ai.attackRange / dist
        val cosTarget: Float = dotProduct(toAttackLocation, toShip) / (ai.attackRange * dist)
        if (cosTangent <= cosTarget) {
            return orbitTarget(dt, maneuverTarget, attackLocation, speedLimits)
        }

        // If heading directly to the attack location would bring the ship too close to the target,
        // instead, navigate to a tangential point on the attack radius around the target.
        val point1Dir = (pointsOfTangency.first + toShip).facing - toAttackLocation.facing
        val point2Dir = (pointsOfTangency.second + toShip).facing - toAttackLocation.facing

        val tangentialAttackVector = if (point1Dir.length < point2Dir.length) pointsOfTangency.first else pointsOfTangency.second
        return ExpectedManeuver(
            destination = attackLocation,
            steeringPoint = movement.location + tangentialAttackVector,
            velocityAtDestination = maneuverTarget.velocity,
        )
    }

    /** Maintain distance to maneuver target while approaching the expected attackLocation.
     * As long as the maneuver target is slower than the ship, this will result in the ship
     * orbiting the maneuver target until it reaches the attack location. */
    private fun orbitTarget(
        dt: Float,
        maneuverTarget: Movement,
        attackLocation: Vector2f,
        speedLimits: List<SpeedLimit>,
    ): ExpectedManeuver {
        // Perform calculations in target frame of reference.
        val toShip = movement.location - maneuverTarget.location
        val toAttackLocation = attackLocation - maneuverTarget.location

        // Calculate the orbit parameters.
        val closestLocationOnOrbit = maneuverTarget.location + toShip.resized(toAttackLocation.length)
        val orbitVelocityVector = toShip.rotated(RotationMatrix(-90f * (toShip.facing - toAttackLocation.facing).sign)).resized(1f)

        // The ship tries to maintain distance from maneuver target.
        // If the maneuver target is slower than the ship, use the
        // remaining ship velocity to move around the orbit.
        val maxSpeed = maxOf(movement.maxSpeed, movement.velocity.length) + movement.acceleration * dt
        val t = solve(maneuverTarget.velocity, orbitVelocityVector, maxSpeed)?.largerNonNegative

        val approximateOrbitLength = (attackLocation - closestLocationOnOrbit).length
        val orbitSpeed = EngineController.vMax(dt, approximateOrbitLength, movement.acceleration)
        val cappedOrbitSpeed = t?.coerceAtMost(orbitSpeed) ?: 0f

        val expectedVelocity = maneuverTarget.velocity + orbitVelocityVector * cappedOrbitSpeed
        val expectedDirection = expectedVelocity.facing

        // Clamp the expected velocity to within the speed limits. Collision avoidance logic
        // would reduce velocity anyway, but if applied afterwards it can cause jitter.
        val clampedSpeed = speedLimits.fold(expectedVelocity.length) { clampedSpeed, lim ->
            lim.clampSpeed(expectedDirection, clampedSpeed)
        }

        // Instruct the engine controller to position the ship on the calculated orbit,
        // and assume the calculated orbital speed.
        return ExpectedManeuver(
            destination = attackLocation,
            steeringPoint = closestLocationOnOrbit,
            velocityAtDestination = expectedVelocity.resized(clampedSpeed),
        )
    }

    data class ExpectedManeuver(
        val destination: Vector2f,
        val steeringPoint: Vector2f?,
        val velocityAtDestination: Vector2f,
    )
}
