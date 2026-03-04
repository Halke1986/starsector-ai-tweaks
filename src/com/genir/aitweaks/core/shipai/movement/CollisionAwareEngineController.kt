package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand.*
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.movement.Movement.Companion.movement
import com.genir.aitweaks.core.utils.types.LinearMotion
import com.genir.aitweaks.core.utils.types.RotationMatrix
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedX
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedY
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.abs
import kotlin.math.sign
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
        val obstacle: CombatEntityAPI?,
        val pMin: Float,
        val pMax: Float,
    )

    fun heading(dt: Float, destination: Destination, limits: List<SpeedLimit> = listOf()): Vector2f {
        return heading(dt, destination.location, destination.velocity) { ve, rotationToShip ->
            limitVelocity(ve, rotationToShip, limits)
        }
    }

    /** If the ship's expected velocity exceeds a speed limit, compute a new
     * velocity vector that respects the limit while moving the ship as efficiently
     * as possible toward its destination. */
    private fun limitVelocity(expectedVelocity: Vector2f, rotationToShip: RotationMatrix, limits: List<SpeedLimit>): Vector2f? {
        // Discard speed limits with value too high to affect the ship movement.
        val expectedSpeed = expectedVelocity.length
        val relevantLimits = limits.filter { limit ->
            limit.speedLimit <= expectedSpeed
        }

        if (relevantLimits.isEmpty()) {
            return null
        }

        // Clamp reverse (negative) speed limits, so they stay below the ship's
        // maximum speed. This alters the resulting velocity vector, allowing
        // the ship to slide past the obstacle instead of continuously backing away.
        // Because this increases collision risk, do not apply when avoiding
        // enemy missiles.
        val speedConstraint = movement.maxSpeed / -2f
        val constrainedLimits = relevantLimits.map { limit ->
            return@map if (limit.obstacle != null && limit.obstacle !is MissileAPI) {
                SpeedLimit(
                    limit.direction,
                    limit.speedLimit.coerceAtLeast(speedConstraint),
                    limit.obstacle
                )
            } else {
                limit
            }
        }

        val bounds: List<Bound> = buildBounds(constrainedLimits)
        if (bounds.isEmpty()) {
            // Leave the behavior undefined in the unusual case
            // of strongly contradicting speed limits.
            return null
        }

        val limitedVelocity: Vector2f = findSafeVelocity(bounds, expectedVelocity, expectedSpeed)
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
//        Debug.drawVector(movement.location, limitedVelocity.resized(200f), Color.YELLOW)

        return limitedVelocity
    }


    /** Find a velocity vector that respect the bounds, while allowing
     * the ship to travel the fastest and with direction aligned with
     * the expected velocity. */
    private fun findSafeVelocity(bounds: List<Bound>, expectedVelocity: Vector2f, expectedSpeed: Float): Vector2f? {
        val exceededBounds: MutableList<Bound> = mutableListOf()

        var boundToYield: Bound? = null
        var boundToYieldExceededBy = 0f

        for (bound: Bound in bounds) {
            // Expected velocity does not exceed the bound.
            val expectedX = expectedVelocity.rotatedX(bound.r)
            val exceededBy = expectedX - bound.speedLimit
            if (exceededBy <= 0f) {
                continue
            }

            exceededBounds.add(bound)

            if (shouldYield(bound) && exceededBy > boundToYieldExceededBy) {
                boundToYield = bound
                boundToYieldExceededBy = exceededBy
            }
        }

        val accumulator = VelocityEval(expectedVelocity, boundToYield)
        for (bound: Bound in exceededBounds) {
            // Calculate and evaluate velocity vectors that respect the bound
            // while allowing the ship to move at the expected speed. If no such
            // vector exists, evaluate the vector that violates the bound the least.
            if (expectedSpeed >= abs(bound.speedLimit)) {
                val yOffset = sqrt(expectedSpeed * expectedSpeed - bound.speedLimit * bound.speedLimit)
                accumulator.evaluate(-yOffset, bound)
                accumulator.evaluate(+yOffset, bound)
            } else {
                accumulator.evaluate(0f, bound)
            }
        }

        return accumulator.velocity
    }

    private inner class VelocityEval(val expectedVelocity: Vector2f, val boundToYield: Bound?) {
        var velocity: Vector2f? = null
        var score: Float = 0f

        fun evaluate(yOffset: Float, bound: Bound) {
            val y: Float = yOffset.coerceIn(bound.pMin, bound.pMax)
            val v: Vector2f = Vector2f(bound.speedLimit, y).rotatedReverse(bound.r)

            // Discard velocities that would cross obstacle direction vector.
            val obstacleToYield = (boundToYield?.obstacle as? ShipAPI)?.movement
            val obstacleAI = obstacleToYield?.ship?.customShipAI?.maneuver
            if (y != 0f && boundToYield != null && obstacleAI != null) {
                val obstacleDirection = (obstacleAI.attackPoint ?: obstacleAI.headingPoint) - obstacleToYield.location
                if (obstacleDirection.rotatedY(boundToYield.r).sign == v.rotatedY(boundToYield.r).sign) {
                    return
                }
            }

            val angleToExpected: Float = (expectedVelocity.facing - v.facing).length
            val angleNormalized = 1f - angleToExpected / 180f
            val angleSquared = angleNormalized * angleNormalized

            // Prefer directions where the ship can travel faster.
            // Strongly prefer directions aligned with the expected velocity vector.
            val score = v.length * angleSquared

            if (this.velocity == null || score > this.score) {
                this.velocity = v
                this.score = score
            }
        }
    }

    /** Movement de-conflicting. */
    private fun shouldYield(bound: Bound): Boolean {
        // Yield only to allies controlled by custom AI.
        val obstacle = (bound.obstacle as? ShipAPI)?.movement
            ?: return false
        val obstacleAI = obstacle.ship.customShipAI?.maneuver
            ?: return false
        if (movement.ship.owner != obstacle.ship.owner) {
            return false
        }

        // If the obstacle is a retreating ally, the ship must proactively
        // yield right-of-way even if their paths do not intersect.
        val obstacleBackingOff = obstacle.ship.aiFlags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF)
        if (obstacleBackingOff != ai.ventModule.isBackingOff) {
            return obstacleBackingOff
        }

        // Check if ship intends to cross obstacle velocity vector.
        val obstacleDirection = (obstacleAI.attackPoint ?: obstacleAI.headingPoint) - obstacle.location
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
                    pMax = minOf(pMax, distance)
                } else {
                    pMin = maxOf(pMin, distance)
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
                    pMin = bound.pMin.coerceAtLeast(minOf(-200f, bound.pMax))
                    pMax = bound.pMax
                }

                bound.pMax == 1e4f -> {
                    pMin = bound.pMin
                    pMax = bound.pMax.coerceAtMost(maxOf(200f, bound.pMin))
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
