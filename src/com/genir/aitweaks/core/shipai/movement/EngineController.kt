package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.movement.CollisionAvoidance.Companion.movementPriority
import com.genir.aitweaks.core.utils.sqrt
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.LinearMotion
import com.genir.aitweaks.core.utils.types.LinearMotion.Companion.intersection
import com.genir.aitweaks.core.utils.types.RotationMatrix
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedX
import org.lwjgl.util.vector.Vector2f
import java.awt.Color.BLUE
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/** Engine Controller for AI piloted ships. */
class EngineController(val ai: CustomShipAI, movement: Movement) : BasicEngineController(movement) {
    data class Destination(val location: Vector2f, val velocity: Vector2f)

    private data class Bound(
        val r: RotationMatrix,
        val speedLimit: Float,
        val obstacle: Movement?,
        val pMin: Float,
        val pMax: Float,
    )

    fun heading(dt: Float, destination: Destination, limits: List<CollisionAvoidance.Limit> = listOf()): Vector2f {
        return heading(dt, destination.location, destination.velocity) { toShipFacing, ve -> limitVelocity(dt, toShipFacing, ve, limits) }
    }

    private fun limitVelocity(dt: Float, toShipFacing: Direction, expectedVelocityRaw: Vector2f, limits: List<CollisionAvoidance.Limit>): LimitedVelocity? {
        val rotationToShip = toShipFacing.rotationMatrix
        val expectedVelocity = (expectedVelocityRaw.rotatedReverse(rotationToShip) / dt)

//        limits.forEach { lim ->
//            val obs = lim.obstacle?.let { "${it.velocity}" } ?: ""
//            log("${ship.name} $lim V ${movement.velocity} E $expectedVelocity $obs")
//        }

        // Discard speed limits with value too high to affect the ship movement.
        val vLim = max(movement.velocity.length, expectedVelocity.length)
        val relevantLimits = limits.filter { limit ->
            limit.speedLimit <= vLim
        }

        if (relevantLimits.isEmpty()) {
            return null
        }

        val bounds: List<Bound> = buildBounds(relevantLimits)
        if (bounds.isEmpty()) {
            // In case of strongly contradicting speed limits, stop the ship.
            if (movement.velocity.isNonZero) {
                giveCommand(DECELERATE)
            }

            return null
        }

//        debugDrawBounds(bounds)

        val limitedVelocity = handleOngoingOverspeed(bounds, rotationToShip)
            ?: handleExpectedOverspeed(bounds, expectedVelocity)

        // Translate the limited velocity to ship frame of reference.
        if (limitedVelocity != null) {
            return LimitedVelocity(
                limitedVelocity.movementOverridden,
                limitedVelocity.velocity.rotated(rotationToShip) * dt,
            )
        }

        return null
    }

    private fun handleOngoingOverspeed(bounds: List<Bound>, rotationToShip: RotationMatrix): LimitedVelocity? {
        val shipPriority = movement.ship.movementPriority
        val deltaV: Vector2f? = findSafeVelocityDV(bounds, calculateVelocityParallelToBound = { bound ->
            // When ship has a higher priority than the obstacle, do not brake sharply.
            // Let the handleExpectedOverspeed method handle the collision gracefully.
            // This applies only to allies, which will cooperate with the ship.
            val obstacle = bound.obstacle
            if (obstacle != null && obstacle.ship.owner == movement.ship.owner && shipPriority > obstacle.ship.movementPriority) {
                return@findSafeVelocityDV null
            }

            val velocity = movement.velocity.rotated(bound.r)
            if (velocity.x <= bound.speedLimit) {
                // Velocity does not exceed the bound.
                return@findSafeVelocityDV null
            }

            val slideMagnitude = abs(bound.speedLimit - velocity.x)

            velocity.y + slideMagnitude * slideDirection(bound)
        })

        if (deltaV != null) {
            // Use the two ship thrust vectors that form the quadrant containing
            // the velocity delta (DV) vector, rather than applying proportional
            // thrust aligned exactly with the DV vector. This approach achieves
            // the required DV in the shortest possible time and intentionally
            // applies an additional perpendicular thrust component, allowing the
            // ship to "slide" past obstacles naturally.
            val dvs = deltaV.rotated(rotationToShip)
            if (dvs.y > 0) giveCommand(ACCELERATE)
            if (dvs.y < 0) giveCommand(ACCELERATE_BACKWARDS)
            if (dvs.x < 0) giveCommand(STRAFE_LEFT)
            if (dvs.x > 0) giveCommand(STRAFE_RIGHT)

//            Debug.drawVector(movement.location, movement.velocity, GREEN)
//            Debug.drawVector(movement.location, movement.velocity + deltaV, YELLOW)
//            Debug.drawLine(movement.location, movement.ship.customShipAI!!.movement.headingPoint, MAGENTA)

            return LimitedVelocity(movementOverridden = true, movement.velocity + deltaV)
        }

        return null
    }

    private fun handleExpectedOverspeed(bounds: List<Bound>, expectedVelocity: Vector2f): LimitedVelocity? {
        val expectedVelocityCapped = expectedVelocity.clampLength(movement.maxSpeed)

        val deltaV = findSafeVelocityDV(bounds, calculateVelocityParallelToBound = { bound ->
            // Velocity does not exceed the bound.
            val vx = expectedVelocity.rotatedX(bound.r)
            if (vx <= bound.speedLimit) {
                return@findSafeVelocityDV null
            }

            val slideMagnitude = sqrt(bound.speedLimit * bound.speedLimit + expectedVelocityCapped.lengthSquared)

            slideMagnitude * slideDirection(bound)
        })

        if (deltaV != null) {
//            Debug.drawVector(movement.location, movement.velocity + deltaV, YELLOW)
//            Debug.drawLine(movement.location, movement.ship.customShipAI!!.movement.headingPoint, MAGENTA)

            return LimitedVelocity(movementOverridden = false, movement.velocity + deltaV)
        }

        return null
    }

    /** If the ship's current velocity exceeds any speed limit, calculate
     * a new velocity vector that does not exceed the speed limit and can
     * be reached in the shortest possible time. */
    private fun findSafeVelocityDV(bounds: List<Bound>, calculateVelocityParallelToBound: (Bound) -> Float?): Vector2f? {
        var lowestDeltaV: Vector2f? = null

        // Find the closest point on the boundary relative to the velocity vector.
        // This point will be the new, limited velocity.
        for (bound in bounds) {
            val limitedVelocityY = calculateVelocityParallelToBound(bound) ?: continue

            val limitedVelocity = Vector2f(
                bound.speedLimit,
                limitedVelocityY.coerceIn(bound.pMin, bound.pMax),
            )

            val dv = limitedVelocity.rotatedReverse(bound.r) - movement.velocity
            if (lowestDeltaV == null || dv.lengthSquared < lowestDeltaV.lengthSquared) {
                lowestDeltaV = dv
            }
        }

        return lowestDeltaV
    }

    /** Calculate direction the ship should "slide" along the boundary to avoid an obstacle. */
    private fun slideDirection(bound: Bound): Float {
        // Default slide direction takes the ship closer to its intended destination.
        val destination = (ai.maneuver.headingPoint - movement.location).rotated(bound.r)
        val defaultDirection = destination.y.sign

        // Do not yield to enemy, hulks and abstract obstacles represented by null.
        val obstacle = bound.obstacle ?: return defaultDirection
        if (movement.ship.owner != obstacle.ship.owner) {
            return defaultDirection
        }

        // Obstacle movement parameters.
        val toObstacle = (obstacle.location - movement.location)
        val obstacleCustomHeading = obstacle.ship.customShipAI?.maneuver?.headingPoint
        val obstacleDirection = when {
            obstacleCustomHeading != null -> {
                obstacleCustomHeading - obstacle.location
            }

            else -> {
                obstacle.velocity * 1000f
            }
        }

        // Check if ship intends to cross obstacle velocity vector.
        val (k, t) = intersection(
            LinearMotion(Vector2f(), destination),
            LinearMotion(toObstacle.rotated(bound.r), obstacleDirection.rotated(bound.r)),
        ) ?: return defaultDirection

        // If the ship has a lower movement priority than the obstacle,
        // it must proactively yield right-of-way—even if their paths
        // only intersect further ahead.
        val hasLowerPriority = movement.ship.movementPriority < obstacle.ship.movementPriority
        if (hasLowerPriority && k > 0f && t > 0f) {
            return -defaultDirection
        }

        // Ship does not intend to cross the obstacle velocity vector.
        if (k < 0f || k > 0.99f || t < 0f || t > 0.99f) {
            return defaultDirection
        }

        // If two ships' paths intersect, one should yield the right of way.
        val shouldYield = when {
            movement.ship.movementPriority != obstacle.ship.movementPriority -> {
                movement.ship.movementPriority < obstacle.ship.movementPriority
            }

            movement.acceleration != obstacle.acceleration -> {
                movement.acceleration > obstacle.acceleration
            }

            movement.ship.mass != obstacle.ship.mass -> {
                movement.ship.mass < obstacle.ship.mass
            }

            else -> {
                movement.ship.hashCode() < obstacle.ship.hashCode()
            }
        }

        if (shouldYield) {
            return -defaultDirection
        }

        return defaultDirection
    }

    private fun buildBounds(limits: List<CollisionAvoidance.Limit>): List<Bound> {
        val rawBounds: List<Bound> = limits.map { limit ->
            val r = (-limit.direction).rotationMatrix
            Bound(r, limit.speedLimit, limit.obstacle, 0f, 0f)
        }

        val strictBounds = intersectBounds(rawBounds, 0f)
        if (strictBounds.isNotEmpty()) {
            return strictBounds
        }

        // If conflicting speed limits are detected, use a binary search
        // algorithm to determine bounds that minimally violates them.
        val minLimit = limits.minOf { it.speedLimit }
        val span = 600f - minLimit

        var step: Float = span / 2
        var tolerance = span / 2
        var relaxedBounds = listOf<Bound>()

        for (i in 0..7) {
            val newRelaxedBounds = intersectBounds(rawBounds, tolerance)

            step /= 2
            tolerance += if (newRelaxedBounds.isEmpty()) {
                step
            } else {
                relaxedBounds = newRelaxedBounds
                -step
            }
        }

        return relaxedBounds
    }

    private fun intersectBounds(rawBounds: List<Bound>, tolerance: Float): List<Bound> {
        return rawBounds.mapNotNull { bound ->
            // Find enclosing velocity bounds.
            val speedLimit = bound.speedLimit + tolerance
            val p = Vector2f(speedLimit, 0f).rotatedReverse(bound.r)
            val v = Vector2f(0f, 1f).rotatedReverse(bound.r)

            var pMax = 1e4f
            var pMin = -1e4f

            // Intersect bounds.
            rawBounds.forEach inner@{ other ->
                if (other === bound) {
                    return@inner
                }

                val otherSpeedLimit = other.speedLimit + tolerance
                val vx = v.rotatedX(other.r)

                // Limits are perpendicular.
                if (vx == 0f) {
                    // Bound is entirely behind other bounds.
                    if (otherSpeedLimit < speedLimit) {
                        pMin = Float.MAX_VALUE
                        pMax = -Float.MAX_VALUE
                    }

                    return@inner
                }

                val px = p.rotatedX(other.r)
                val distance = (otherSpeedLimit - px) / vx

                if (vx > 0f) {
                    pMax = min(pMax, distance)
                } else {
                    pMin = max(pMin, distance)
                }
            }

            // Bound is entirely behind other bounds.
            if (pMin > pMax) {
                return@mapNotNull null
            }

            return@mapNotNull Bound(bound.r, speedLimit, bound.obstacle, pMin, pMax)
        }
    }

    private fun debugDrawBounds(bounds: List<Bound>) {
        bounds.forEach { bound ->
            val pMin: Float
            val pMax: Float

            when {
                bound.pMin == -1e4f && bound.pMax == 1e4f -> {
                    pMin = bound.pMin.coerceIn(-200f, 200f)
                    pMax = bound.pMax.coerceIn(-200f, 200f)
                }

                bound.pMin == -1e4f -> {
                    pMin = bound.pMin.coerceAtLeast(min(-200f, bound.pMax))
                    pMax = bound.pMax
                }

                bound.pMax == 1e4f -> {
                    pMin = bound.pMin
                    pMax = bound.pMax.coerceAtMost(max(200f, bound.pMin))
                }

                else -> {
                    pMin = bound.pMin
                    pMax = bound.pMax
                }
            }

            val p1 = movement.location + Vector2f(bound.speedLimit, pMin).rotatedReverse(bound.r)
            val p2 = movement.location + Vector2f(bound.speedLimit, pMax).rotatedReverse(bound.r)
            Debug.drawLine(p1, p2, BLUE)
        }
    }
}
