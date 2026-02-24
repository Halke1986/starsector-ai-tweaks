package com.genir.aitweaks.core.shipai.movement

import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.movement.CollisionAvoidance.Companion.movementPriority
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.RotationMatrix
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedX
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Wraps EngineController and enforces local collision avoidance.
 *
 * Accepts heading and velocity orders, evaluates nearby entities for
 * potential collisions, and overrides thruster commands when necessary
 * to prevent unsafe movement.
 */
class CollisionAwareEngineController(val ai: CustomShipAI, movement: Movement) : EngineController(movement) {
    data class Destination(val location: Vector2f, val velocity: Vector2f)

    private data class Bound(
        val r: RotationMatrix,
        val speedLimit: Float,
        val obstacle: Movement?,
        val pMin: Float,
        val pMax: Float,
    )

    fun heading(dt: Float, destination: Destination, limits: List<SpeedLimit> = listOf()): Vector2f {
        return heading(dt, destination.location, destination.velocity) { toShipFacing, ve -> limitVelocity(dt, toShipFacing, ve, limits) }
    }

    private fun limitVelocity(dt: Float, toShipFacing: Direction, expectedVelocityRaw: Vector2f, limits: List<SpeedLimit>): Vector2f? {
        val rotationToShip = toShipFacing.rotationMatrix
        val expectedVelocity = (expectedVelocityRaw.rotatedReverse(rotationToShip) / dt).clampedLength(movement.maxSpeed)

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
            // Leave the behavior undefined in the unusual case
            // of strongly contradicting speed limits.
            return null
        }

//        debugDrawBounds(bounds)

        val velocityFinder = HandleExpectedOverspeed(expectedVelocity)
        val limitedVelocity: Vector2f = velocityFinder.findSafeVelocity(bounds)
            ?: return null

//        Debug.drawVector(movement.location, expectedVelocity, Color.MAGENTA)
//        Debug.drawVector(movement.location, limitedVelocity, Color.YELLOW)

        // Translate the limited velocity to ship frame of reference.
        return limitedVelocity.rotated(rotationToShip) * dt
    }

    private data class VelocityEval(var velocity: Vector2f?, var score: Float)

    /** If the ship's expected velocity exceeds a speed limit, compute a new
     * velocity vector that respects the limit while moving the ship as efficiently
     * as possible toward its destination. */
    private inner class HandleExpectedOverspeed(val expectedVelocity: Vector2f) {
        val expectedSpeed: Float = expectedVelocity.length

        fun findSafeVelocity(bounds: List<Bound>): Vector2f? {
            // Find a velocity vector that respect the bounds, while allowing
            // the ship to travel the fastest and with direction aligned with
            // the expected velocity.
            val accumulator: VelocityEval = VelocityEval(null, 0f)

            for (bound: Bound in bounds) {
                val expected: Vector2f = expectedVelocity.rotated(bound.r)

                // Expected velocity does not exceed the bound.
                if (expected.x < bound.speedLimit) {
                    continue
                }

                // Calculate and evaluate velocity vectors that respect the bound
                // while allowing the ship to move at the expected speed. If no such
                // vector exists, evaluate the vector that violates the bound the least.
                if (expectedSpeed >= abs(bound.speedLimit)) {
                    val yOffset = sqrt(expectedSpeed * expectedSpeed - bound.speedLimit * bound.speedLimit)
                    evaluateVelocity(accumulator, -yOffset, bound)
                    evaluateVelocity(accumulator, +yOffset, bound)
                } else {
                    evaluateVelocity(accumulator, 0f, bound)
                }
            }

            return accumulator.velocity
        }

        private fun evaluateVelocity(accumulator: VelocityEval, yOffset: Float, bound: Bound) {
            val y: Float = yOffset.coerceIn(bound.pMin, bound.pMax)
            val v: Vector2f = Vector2f(bound.speedLimit, y).clampedLength(expectedSpeed).rotatedReverse(bound.r)

            val angleToExpected: Float = (expectedVelocity.facing - v.facing).length
            val angleNormalized = 1f - angleToExpected / 180f
            val angleSquared = angleNormalized * angleNormalized

            // Prefer directions where the ship can travel faster.
            // Strongly prefer directions aligned with the expected velocity vector.
            val score = v.length * angleSquared

            if (accumulator.velocity == null || score > accumulator.score) {
                accumulator.velocity = v
                accumulator.score = score
            }
        }
    }

    private fun shouldYield(obstacle: Movement): Boolean {
        return when {
            // Do not yield to enemy and hulks.
            movement.ship.owner != obstacle.ship.owner -> {
                false
            }

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
    }

    private fun buildBounds(limits: List<SpeedLimit>): List<Bound> {
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

            Debug.drawLine(p1, p2, Color.BLUE)
        }
    }
}
