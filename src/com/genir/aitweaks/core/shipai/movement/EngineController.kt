package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.RotationMatrix
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedX
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedY
import org.lwjgl.util.vector.Vector2f
import java.awt.Color.BLUE
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/** Engine Controller for AI piloted ships. */
class EngineController(ship: ShipAPI) : BasicEngineController(ship) {
    data class Destination(val location: Vector2f, val velocity: Vector2f)

    /** Limit allows to restrict velocity to not exceed
     * max speed in a direction along a given heading. */
    data class Limit(
        val direction: Direction,
        val speedLimit: Float,
        val obstacle: ShipAPI?,
    )

    private data class Bound(
        val r: RotationMatrix,
        val speedLimit: Float,
        val obstacle: ShipAPI?,
        val pMin: Float,
        val pMax: Float,
    )

    fun heading(dt: Float, destination: Destination, limits: List<Limit> = listOf()): Vector2f {
        return heading(dt, destination.location, destination.velocity) { toShipFacing, ve -> limitVelocity(dt, toShipFacing, ve, limits) }
    }

    private fun limitVelocity(dt: Float, toShipFacing: Direction, expectedVelocityRaw: Vector2f, limits: List<Limit>): Vector2f? {
        val rotationToShip = toShipFacing.rotationMatrix
        val expectedVelocity = expectedVelocityRaw.rotatedReverse(rotationToShip) / dt
        val vLim = max(ship.velocity.length, expectedVelocity.length)

//        limits.forEach { lim ->
//            val obs = lim.obstacle?.let { "${it.velocity}" } ?: ""
//            log("${ship.name} $lim V ${ship.velocity} E $expectedVelocity $obs")
//        }

        // Discard speed limits with value too high to affect the ship movement.
        val relevantLimits = limits.filter { limit ->
            limit.speedLimit <= vLim
        }

        if (relevantLimits.isEmpty()) {
            return null
        }

        val bounds: List<Bound> = buildBounds(relevantLimits)
        if (bounds.isEmpty()) {
            // In case of strongly contradicting speed limits, stop the ship.
            if (ship.velocity.isNonZero) {
                ship.command(ShipCommand.DECELERATE)
            }

            return Vector2f()
        }

//        debugDrawBounds(bounds)

        val deltaV: Vector2f? = findSafeVelocityDV(bounds, expectedVelocity)
        if (deltaV != null) {
            // Use the two ship thrust vectors that form the quadrant containing
            // the velocity delta (DV) vector, rather than applying proportional
            // thrust aligned exactly with the DV vector. This approach achieves
            // the required DV in the shortest possible time and intentionally
            // applies an additional perpendicular thrust component, allowing the
            // ship to "slide" past obstacles naturally.
            val dvs = deltaV.rotated(rotationToShip)
            if (dvs.y > 0) ship.command(ShipCommand.ACCELERATE)
            if (dvs.y < 0) ship.command(ShipCommand.ACCELERATE_BACKWARDS)
            if (dvs.x < 0) ship.command(ShipCommand.STRAFE_LEFT)
            if (dvs.x > 0) ship.command(ShipCommand.STRAFE_RIGHT)

//            Debug.drawVector(ship.location, ship.velocity, GREEN)
//            Debug.drawVector(ship.location, ship.velocity + deltaV, YELLOW)
//            Debug.drawLine(ship.location, ship.customShipAI!!.movement.headingPoint, MAGENTA)

            return ship.velocity + deltaV
        }

        return null
    }

    /** If the ship's current velocity exceeds any speed limit, calculate
     * a new velocity vector that does not exceed the speed limit and can
     * be reached in the shortest possible time. */
    private fun findSafeVelocityDV(bounds: List<Bound>, expectedVelocity: Vector2f): Vector2f? {
        var lowestDeltaV: Vector2f? = null

        // Find the closest point on the boundary relative to the velocity vector.
        // This point will be the new, limited velocity.
        for (bound in bounds) {
            // Velocity does not exceed the bound.
            val vx = ship.velocity.rotatedX(bound.r)
            if (vx <= bound.speedLimit) {
                continue
            }

            val obstacle = bound.obstacle
            val expectedX = expectedVelocity.rotatedX(bound.r)

            val slideOffset = when {
                // If an allied or inert obstacle is pushing the ship away
                // from its destination, try to deliberately slide past it.
                obstacle != null && !obstacle.isHostile(ship) && expectedX >= bound.speedLimit -> {
                    val obstacleVelocity = obstacle.customShipAI?.movement?.expectedVelocity ?: obstacle.velocity
                    val obstacleY = obstacleVelocity.rotatedY(bound.r)
                    abs(bound.speedLimit - vx) * -obstacleY.sign
                }

                // Apply an acceleration in the expected direction on top of
                // avoiding the speed limit.
                obstacle != null -> {
                    val expectedY = expectedVelocity.rotatedY(bound.r)
                    abs(bound.speedLimit - vx) * expectedY.sign
                }

                else -> {
                    0f
                }
            }

            val vCorrectedY = ship.velocity.rotatedY(bound.r) + slideOffset
            val closestPoint = Vector2f(bound.speedLimit, vCorrectedY.coerceIn(bound.pMin, bound.pMax))
            val limitedVelocity = closestPoint.rotatedReverse(bound.r)
            val dv = limitedVelocity - ship.velocity

            if (lowestDeltaV == null || dv.lengthSquared < lowestDeltaV.lengthSquared) {
                lowestDeltaV = dv
            }
        }

        return lowestDeltaV
    }

    private fun buildBounds(limits: List<Limit>): List<Bound> {
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

            val p1 = ship.location + Vector2f(bound.speedLimit, pMin).rotatedReverse(bound.r)
            val p2 = ship.location + Vector2f(bound.speedLimit, pMax).rotatedReverse(bound.r)
            Debug.drawLine(p1, p2, BLUE)
        }
    }
}
