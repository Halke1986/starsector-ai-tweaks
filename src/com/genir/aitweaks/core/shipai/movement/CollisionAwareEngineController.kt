package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand.*
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.LinearMotion
import com.genir.aitweaks.core.utils.types.RotationMatrix
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedX
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Wraps EngineController and enforces local collision avoidance.
 *
 * Accepts heading and velocity orders, evaluates nearby entities for
 * potential collisions, and overrides thruster commands when necessary
 * to prevent unsafe movement.
 */
class CollisionAwareEngineController(val ai: CustomShipAI, movement: Movement) : EngineController(movement) {
    fun heading(dt: Float, destination: Destination, limits: List<SpeedLimit> = listOf()): Vector2f {
        return heading(dt, destination.location, destination.velocity) { ve, rotationToShip ->
            limitVelocity(ve, rotationToShip, limits)
        }
    }

    /** If the ship's expected velocity exceeds a speed limit, compute a new
     * velocity vector that respects the limit while moving the ship as efficiently
     * as possible toward its destination. */
    private fun limitVelocity(expectedVelocity: Vector2f, rotationToShip: RotationMatrix, limits: List<SpeedLimit>): Vector2f? {
        val dodgeSpeed = maxOf(expectedVelocity.length, ship.maxSpeed)
        val bounds: List<Bound> = compileBounds(expectedVelocity, dodgeSpeed, limits)
        if (bounds.isEmpty()) {
            return null
        }

        var boundToYield: Bound? = null
        var boundToYieldExceededBy = 0f

        // Find a bound to yield, if any. If there are
        // multiple, select the one exceeded the most.
        for (bound in bounds) {
            val exceededBy = expectedVelocity.rotatedX(bound.r) - bound.unconstrainedSpeedLimit
            if (shouldYield(bound) && exceededBy > boundToYieldExceededBy) {
                boundToYield = bound
                boundToYieldExceededBy = exceededBy
            }
        }

        val limitedVelocity: Vector2f = findSafeVelocity(bounds, expectedVelocity.facing, dodgeSpeed, boundToYield)
            ?: return null

        // Use the two ship thrust vectors that form the quadrant containing
        // the velocity delta (DV) vector, rather than applying proportional
        // thrust aligned exactly with the DV vector. This approach achieves
        // the required DV in the shortest possible time and intentionally
        // applies an additional perpendicular thrust component, allowing the
        // ship to "slide" past obstacles naturally.
        val dv = (limitedVelocity - movement.velocity).rotated(rotationToShip)
        if (dv.y > 0) giveCommand(ACCELERATE)
        if (dv.y < 0) giveCommand(ACCELERATE_BACKWARDS)
        if (dv.x < 0) giveCommand(STRAFE_LEFT)
        if (dv.x > 0) giveCommand(STRAFE_RIGHT)

//        debugDrawBounds(bounds)
//        Debug.drawVector(movement.location, expectedVelocity, Color.MAGENTA)
//        Debug.drawVector(movement.location, limitedVelocity, Color.YELLOW)
//        Debug.drawVector(movement.location, movement.velocity, Color.GREEN)

        return limitedVelocity
    }

    /** Find a velocity vector that respect the bounds, while allowing
     * the ship to travel the fastest and with direction aligned with
     * the expected velocity. */
    private fun findSafeVelocity(bounds: List<Bound>, expectedVelocityFacing: Direction, dodgeSpeed: Float, boundToYield: Bound?): Vector2f? {
        // Calculate and evaluate velocity vectors that respect the bound
        // while allowing the ship to move at the expected speed.
        val accumulator = VelocityEval(expectedVelocityFacing, dodgeSpeed + 10f, boundToYield)
        for (bound: Bound in bounds) {
            if (abs(bound.speedLimit) <= dodgeSpeed) {
                val yOffset = sqrt(dodgeSpeed * dodgeSpeed - bound.speedLimit * bound.speedLimit)
                accumulator.evaluate(-yOffset, bound)
                accumulator.evaluate(+yOffset, bound)
            }
        }
        if (accumulator.velocity != null) {
            return accumulator.velocity
        }

        // Relax constraints by ignoring movement deconflicting.
        if (boundToYield != null) {
            val noYieldAccumulator = VelocityEval(expectedVelocityFacing, dodgeSpeed + 10f, null)
            for (bound: Bound in bounds) {
                if (abs(bound.speedLimit) <= dodgeSpeed) {
                    val yOffset = sqrt(dodgeSpeed * dodgeSpeed - bound.speedLimit * bound.speedLimit)
                    noYieldAccumulator.evaluate(-yOffset, bound)
                    noYieldAccumulator.evaluate(+yOffset, bound)
                }
            }
            if (noYieldAccumulator.velocity != null) {
                return noYieldAccumulator.velocity
            }
        }

        // Fall back to selecting a vector that violates the bounds the least.
        val fallbackAccumulator = FallbackVelocityEval(expectedVelocityFacing)
        for (bound: Bound in bounds) {
            fallbackAccumulator.evaluate(0f, bound)
        }

        return fallbackAccumulator.velocity
    }

    private inner class VelocityEval(val expectedVelocityFacing: Direction, val maxAllowedSpeed: Float, val boundToYield: Bound?) {
        var velocity: Vector2f? = null
        var score: Float = 0f

        fun evaluate(yOffset: Float, bound: Bound) {
            val y: Float = yOffset.coerceIn(bound.minPoint, bound.maxPoint)
            val velocity: Vector2f = Vector2f(bound.speedLimit, y).rotatedReverse(bound.r)

            if (velocity.length > maxAllowedSpeed) {
                return
            }

            // Discard velocities that would cross obstacle direction vector.
            val obstacleToYield = boundToYield?.obstacle
            if (y != 0f && boundToYield != null && obstacleToYield != null) {
                val obstacleDirection = obstacleDirection(obstacleToYield)
                val intersection: Pair<Float, Float>? = LinearMotion.intersection(
                    LinearMotion(movement.location, velocity),
                    LinearMotion(obstacleToYield.location, obstacleDirection),
                )

                if (intersection != null) {
                    val (k, t) = intersection
                    if (k > 0f && t > 0f) {
                        return
                    }
                }
            }

            // Prefer directions where the ship can travel faster.
            // Strongly prefer directions aligned with the expected velocity vector.
            val angleToExpected: Float = (expectedVelocityFacing - velocity.facing).length
            val angleNormalized = 1f - angleToExpected / 180f
            val angleSquared = angleNormalized * angleNormalized

            val directionChangePenalty = if (obstacleToYield == null && (velocity.facing - movement.velocity.facing).length > 60f) {
                0.5f
            } else {
                1.0f
            }

            val score = velocity.length * angleSquared * directionChangePenalty

            if (this.velocity == null || score > this.score) {
                this.velocity = velocity
                this.score = score
            }
        }
    }

    private inner class FallbackVelocityEval(val expectedVelocityFacing: Direction) {
        var velocity: Vector2f? = null
        var score: Float = Float.MAX_VALUE

        fun evaluate(yOffset: Float, bound: Bound) {
            val y: Float = yOffset.coerceIn(bound.minPoint, bound.maxPoint)
            val velocity: Vector2f = Vector2f(bound.speedLimit, y).rotatedReverse(bound.r)

            val score = velocity.length

            if (this.velocity == null || score < this.score) {
                this.velocity = velocity
                this.score = score
            }
        }
    }

    private fun obstacleDirection(obstacle: CombatEntityAPI): Vector2f {
        val obstacleAI = (obstacle as? ShipAPI)?.customShipAI?.maneuver
        if (obstacleAI != null) {
            return (obstacleAI.attackPoint ?: obstacleAI.headingPoint) - obstacle.location
        }

        // Fallback to velocity.
        return obstacle.velocity
    }

    /** Movement de-conflicting. */
    private fun shouldYield(bound: Bound): Boolean {
        val obstacle: ShipAPI = bound.obstacle as? ShipAPI
            ?: return false

        // Yield only to allies.
        if (movement.ship.owner != obstacle.owner) {
            return false
        }

        // If the obstacle is a retreating ally, the ship must proactively
        // yield right-of-way even if their paths do not intersect.
        val obstacleBackingOff = obstacle.aiFlags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF)
        if (obstacleBackingOff != ai.ventModule.isBackingOff) {
            return obstacleBackingOff
        }

        // Check if ship intends to cross obstacle velocity vector.
        val obstacleDirection = obstacleDirection(obstacle)
        val direction = (ai.maneuver.attackPoint ?: ai.maneuver.headingPoint) - movement.location

        // Ship and obstacle move roughly along the same path. Do not yield.
        if ((obstacleDirection.facing - direction.facing).length < 45f) {
            return false
        }

        val (k, t) = LinearMotion.intersection(
            LinearMotion(movement.location, direction),
            LinearMotion(obstacle.location, obstacleDirection),
        ) ?: return false

        // Ship does not intend to cross the obstacle velocity vector.
        if (k < 0f || k > 0.9f || t < 0f || t > 0.9f) {
            return false
        }

        val timeToIntersection = (direction * k).length / movement.maxSpeed
        val obstacleTimeToIntersection = (obstacleDirection * t).length / obstacle.maxSpeed

        return timeToIntersection > obstacleTimeToIntersection
    }

    private fun compileBounds(expectedVelocity: Vector2f, dodgeSpeed: Float, limits: List<SpeedLimit>): List<Bound> {
        var isAvoidinCollision = false
        val rawBounds: MutableList<RawBound> = mutableListOf()

        // Clamp reverse (negative) speed limits, so they stay below the ship's
        // maximum speed. This alters the resulting velocity vector, allowing
        // the ship to slide past the obstacle instead of continuously backing away.
        val boundConstraint = dodgeSpeed * -0.66f

        for (limit in limits) {
            // Discard speed limits with value too high to affect the ship movement.
            if (limit.speedLimit > dodgeSpeed) {
                continue
            }

            // Rotation to limit FoR.
            val r = (-limit.direction).rotationMatrix
            val exceededBy = expectedVelocity.rotatedX(r) - limit.speedLimit
            if (exceededBy > 0f) {
                isAvoidinCollision = true
            }

            val constrainedLimit = limit.speedLimit.coerceAtLeast(boundConstraint)
            val bound = RawBound(r, limit.speedLimit, constrainedLimit, limit.obstacle)
            rawBounds.add(bound)
        }

        if (!isAvoidinCollision) {
            return listOf()
        }

        return filterAndConnectBounds(rawBounds)
    }

    private fun filterAndConnectBounds(rawBounds: List<RawBound>): List<Bound> {
        val strictBounds = intersectBounds(rawBounds, 0f)
        if (strictBounds.isNotEmpty()) {
            return strictBounds
        }

        // If conflicting speed limits are detected, use a binary search
        // algorithm to determine bounds that minimally violates them.
        val minLimit = rawBounds.minOf { it.speedLimit }
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

    private fun intersectBounds(rawBounds: List<RawBound>, tolerance: Float): List<Bound> {
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
                    pMax = minOf(pMax, distance)
                } else {
                    pMin = maxOf(pMin, distance)
                }
            }

            // Bound is entirely behind other bounds.
            if (pMin > pMax) {
                return@mapNotNull null
            }

            return@mapNotNull Bound(bound.r, bound.unconstrainedSpeedLimit, speedLimit, bound.obstacle, null, pMin, pMax)
        }
    }

    private fun debugDrawBounds(bounds: List<Bound>) {
        val vMax = ship.maxSpeed

        Debug.drawCircle(movement.location, vMax, Color.GRAY)

        for (bound in bounds) {
            if (abs(bound.speedLimit) >= vMax) {
                continue
            }

            val vLim = sqrt(vMax * vMax - bound.speedLimit * bound.speedLimit)

            val pMin: Float = bound.minPoint.coerceIn(-vLim, vLim)
            val pMax: Float = bound.maxPoint.coerceIn(-vLim, vLim)

            val p1 = movement.location + Vector2f(bound.speedLimit, pMin).rotatedReverse(bound.r)
            val p2 = movement.location + Vector2f(bound.speedLimit, pMax).rotatedReverse(bound.r)

            Debug.drawLine(p1, p2, Color.BLUE)
        }
    }

    data class Destination(val location: Vector2f, val velocity: Vector2f)

    private data class RawBound(
        val r: RotationMatrix,
        val unconstrainedSpeedLimit: Float,
        val speedLimit: Float,
        val obstacle: CombatEntityAPI?,
    )

    private data class Bound(
        val r: RotationMatrix,
        val unconstrainedSpeedLimit: Float,
        val speedLimit: Float,
        val obstacle: CombatEntityAPI?,
        val yieldDirection: Int?,
        val minPoint: Float,
        val maxPoint: Float,
    )
}
