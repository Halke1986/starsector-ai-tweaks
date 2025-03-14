package com.genir.aitweaks.core.shipai

import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.RotationMatrix
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedX
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedY
import com.genir.aitweaks.core.utils.sqrt
import org.lwjgl.util.vector.Vector2f
import java.awt.Color.BLUE
import kotlin.math.max
import kotlin.math.min

/** Engine Controller for AI piloted ships. */
class EngineController(val ai: CustomShipAI) : BasicEngineController(ai.ship) {
    /** Limit allows to restrict velocity to not exceed
     * max speed in a direction along a given heading. */
    data class Limit(val direction: Direction, val speedLimit: Float, val priority: Float = 0f)

    data class Destination(val location: Vector2f, val velocity: Vector2f)

    private data class Bound(val r: RotationMatrix, val speedLimit: Float, val pMin: Float, val pMax: Float)

    fun heading(dt: Float, destination: Destination, limits: List<Limit> = listOf()): Vector2f {
        return heading(dt, destination.location, destination.velocity) { toShipFacing, ve -> limitVelocity(dt, toShipFacing, ve, limits) }
    }

    private fun limitVelocity(dt: Float, toShipFacing: Direction, expectedVelocityRaw: Vector2f, limits: List<Limit>): Vector2f {
        val relevantLimits = limits.filter { limit ->
            limit.priority >= ai.movement.priority
        }

        if (relevantLimits.isEmpty()) {
            return expectedVelocityRaw
        }

        val bounds: List<Bound> = buildBounds(relevantLimits)
        debug(bounds)

        val rotationToShip = toShipFacing.rotationMatrix
        val expectedVelocity = expectedVelocityRaw.rotatedReverse(rotationToShip) / dt
        val overspeed = handleOngoingOverspeed(bounds) ?: handlePotentialOverspeed(bounds, expectedVelocity)
        if (overspeed != null) {
            return overspeed.rotated(rotationToShip) * dt
        }

        return expectedVelocityRaw
    }

    private fun debug(bounds: List<Bound>) {
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

            val p1 = ship.location + Vector2f(bound.speedLimit, pMin).rotatedReverse(bound.r)
            val p2 = ship.location + Vector2f(bound.speedLimit, pMax).rotatedReverse(bound.r)
            Debug.drawLine(p1, p2, BLUE)
        }
    }

    private fun handleOngoingOverspeed(bounds: List<Bound>): Vector2f? {
        var closestPoint: Vector2f? = null
        var closestDist = Float.MAX_VALUE

        bounds.forEach { bound ->
            // Velocity does not exceed the bound.
            val vx = ship.velocity.rotatedX(bound.r)
            if (vx <= bound.speedLimit) {
                return@forEach
            }

            // Find the closest point on the boundary relative to the velocity vector.
            val vy = ship.velocity.rotatedY(bound.r)
            val closestLocal = Vector2f(bound.speedLimit, vy.coerceIn(bound.pMin, bound.pMax))
            val closest = closestLocal.rotatedReverse(bound.r)
            val dist = closest.lengthSquared

            if (dist > 0f && dist < closestDist) {
                closestPoint = closest
                closestDist = dist
            }
        }

        // No ongoing overspeed found.
        if (closestPoint == null) {
            return null
        }

        val dv = ship.velocity - closestPoint!!
        return ship.velocity - dv.resized(ship.maxSpeed)
    }

    private fun handlePotentialOverspeed(bounds: List<Bound>, expectedVelocity: Vector2f): Vector2f? {
        val vMax2 = expectedVelocity.lengthSquared

        val expectedFacing = expectedVelocity.facing
        val evaluatePoint = fun(p: Vector2f, speedLimit: Float): Float {
            val angle = (p.facing - expectedFacing).length
            val angleWeight = (180f - angle) / 180f

            if (angle > 89f && speedLimit >= 0) {
                return 0f
            }

            return p.lengthSquared * angleWeight * angleWeight
        }

        var bestPoint: Vector2f? = null
        var bestPointEval = 0f

        bounds.forEach { bound ->
            // Velocity does not exceed the bound.
            val vx = expectedVelocity.rotatedX(bound.r)
            if (vx <= bound.speedLimit) {
                return@forEach
            }

            val vLim2 = bound.speedLimit * bound.speedLimit
            val boundSpan = if (vMax2 > vLim2) {
                sqrt(vMax2 - vLim2)
            } else {
                0f
            }

            val p1 = Vector2f(bound.speedLimit, bound.pMin.coerceIn(-boundSpan, boundSpan)).rotatedReverse(bound.r)
            val p1Eval = evaluatePoint(p1, bound.speedLimit)
            if (p1Eval > bestPointEval) {
                bestPoint = p1
                bestPointEval = p1Eval
            }

            val p2 = Vector2f(bound.speedLimit, bound.pMax.coerceIn(-boundSpan, boundSpan)).rotatedReverse(bound.r)
            val p2Eval = evaluatePoint(p2, bound.speedLimit)
            if (p2Eval > bestPointEval) {
                bestPoint = p2
                bestPointEval = p2Eval
            }
        }

        return bestPoint ?: expectedVelocity
    }

    private fun buildBounds(limits: List<Limit>): List<Bound> {
        val rawBounds: List<Bound> = limits.map { limit ->
            val r = (-limit.direction).rotationMatrix
            Bound(r, limit.speedLimit, 0f, 0f)
        }

        val strictBounds = intersectBounds(rawBounds, 0f)
        if (strictBounds.isNotEmpty()) {
            return strictBounds
        }

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

            return@mapNotNull Bound(bound.r, speedLimit, pMin, pMax)
        }
    }
}
