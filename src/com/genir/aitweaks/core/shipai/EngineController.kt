package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedX
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedY
import com.genir.aitweaks.core.utils.sqrt
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max
import kotlin.math.min

/** Engine Controller for AI piloted ships. */
class EngineController(ship: ShipAPI) : BasicEngineController(ship) {
    private var prevFacing: Direction = 0f.direction

    /** Limit allows to restrict velocity to not exceed
     * max speed in a direction along a given heading. */
    data class Limit(val direction: Direction, val speedLimit: Float)

    data class Destination(val location: Vector2f, val velocity: Vector2f)

    private data class Bound(val r: RotationMatrix, val speedLimit: Float, val pMin: Float, val pMax: Float)

    fun heading(dt: Float, destination: Destination, limits: List<Limit> = listOf()): Vector2f {
        return heading(dt, destination.location, destination.velocity) { toShipFacing, ve -> limitVelocity(dt, toShipFacing, ve, limits) }
    }

    fun facing(dt: Float, facing: Direction) {
        facing(dt, facing, false)
    }

    fun facing(dt: Float, facing: Direction, shouldStop: Boolean) {
        if (shouldStop) {
            return facing(dt, ship.facing.direction, 0f)
        }

        // Estimate target angular velocity.
        val wt = (facing - prevFacing).degrees / dt
        prevFacing = facing

        facing(dt, facing, wt)
    }

    private fun limitVelocity(dt: Float, toShipFacing: Direction, expectedVelocityRaw: Vector2f, limits: List<Limit>): Vector2f {
        if (limits.isEmpty()) {
            return expectedVelocityRaw
        }

        val bounds: List<Bound> = buildBounds(limits)
        if (bounds.isEmpty()) {
            return Vector2f()
        }

//        bounds.forEach { bound ->
//            val p1 = ship.location + Vector2f(bound.speedLimit, bound.pMin.coerceIn(-200f, 200f)).rotatedReverse(bound.r)
//            val p2 = ship.location + Vector2f(bound.speedLimit, bound.pMax.coerceIn(-200f, 200f)).rotatedReverse(bound.r)
//            Debug.drawLine(p1, p2, BLUE)
//        }

        val rotationToShip = toShipFacing.rotationMatrix
        val expectedVelocity = expectedVelocityRaw.rotatedReverse(rotationToShip) / dt

        val overspeed = handleOngoingOverspeed(bounds) ?: handlePotentialOverspeed(bounds, expectedVelocity)
        if (overspeed != null) {
            return overspeed.rotated(rotationToShip) * dt
        }

        return expectedVelocityRaw
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
        val points: MutableList<Vector2f> = mutableListOf()

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

            points.add(Vector2f(bound.speedLimit, bound.pMin.coerceIn(-boundSpan, boundSpan)).rotatedReverse(bound.r))
            points.add(Vector2f(bound.speedLimit, bound.pMax.coerceIn(-boundSpan, boundSpan)).rotatedReverse(bound.r))
        }

        // No relevant speed limits.
        if (points.isEmpty()) {
            return null
        }

        val expectedFacing = expectedVelocity.facing
        val best: Vector2f? = points.maxWithOrNull(compareBy {
            val angle = (it.facing - expectedFacing).length
            val angleWeight = (180f - angle) / 180f

            if (angle > 89f) {
                return@compareBy 0f
            }

            it.lengthSquared * angleWeight * angleWeight
        })

        return best ?: Vector2f()
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
            relaxedBounds = intersectBounds(rawBounds, tolerance)

            step /= 2
            tolerance += if (relaxedBounds.isEmpty()) {
                step
            } else {
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
                val vx = v.rotatedX(other.r)

                // Limits are perpendicular.
                if (vx == 0f) {
                    return@inner
                }

                val px = p.rotatedX(other.r)
                val distance = (other.speedLimit + tolerance - px) / vx

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
