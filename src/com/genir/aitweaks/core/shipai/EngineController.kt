package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import org.lazywizard.lazylib.ext.clampLength
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.min

/** Engine Controller for AI piloted ships. */
class EngineController(ship: ShipAPI) : BasicEngineController(ship) {
    private var prevFacing: Direction = Direction(0f)
    private var prevHeading: Vector2f = Vector2f()

    /** Limit allows to restrict velocity to not exceed
     * max speed in a direction along a given heading. */
    data class Limit(val heading: Direction, val speed: Float)

    fun heading(dt: Float, heading: Vector2f, limits: List<Limit> = listOf()): Vector2f {
        return heading(dt, heading, false, limits)
    }

    fun heading(dt: Float, heading: Vector2f, shouldStop: Boolean, limits: List<Limit> = listOf()): Vector2f {
        if (shouldStop) {
            return heading(dt, ship.location, Vector2f())
        }

        // Estimate target linear velocity.
        val globalVelocityLimit = 600f
        val vt = ((heading - prevHeading) / dt).clampLength(globalVelocityLimit)
        prevHeading = heading.copy

        return heading(dt, heading, vt) { toShipFacing, ve -> limitVelocity(dt, toShipFacing, ve, limits) }
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

    private fun limitVelocity(dt: Float, toShipFacing: Direction, expectedVelocity: Vector2f, absLimits: List<Limit>): Vector2f {
        // No relevant speed limits found, move ahead.
        if (absLimits.isEmpty()) return expectedVelocity

        // Translate speed limit to ship frame of reference.
        // Clamp the speed limit so that different limits remain
        // distinguishable by producing a slightly different value
        // of f(x) = 1/cos(x) for a given hading, instead of all
        // being equal to 0f.
        val limits = absLimits.map { Limit(it.heading + toShipFacing, (it.speed * dt).coerceAtLeast(0.00001f)) }

        // Find the most severe speed limit.
        val expectedSpeed = expectedVelocity.length
        val expectedHeading = expectedVelocity.facing
        val lowestLimit = limits.minByOrNull { it.clampSpeed(expectedHeading, expectedSpeed) }
            ?: return expectedVelocity

        // Most severe speed limit does not influence speed ahead.
        if (lowestLimit.clampSpeed(expectedHeading, expectedSpeed) == expectedSpeed) return expectedVelocity

        // Find new heading that circumvents the lowest speed limit.
        val headingOffset = lowestLimit.headingOffset(expectedSpeed)
        val angleToLimit = lowestLimit.heading - expectedHeading
        val angleToNewFacing = angleToLimit - headingOffset * angleToLimit.sign
        val newFacing = expectedHeading + angleToNewFacing

        // Stop if angle to new heading is right, to avoid erratic behavior
        // when avoiding collision and being stopped close to destination.
        if (angleToNewFacing.length >= 89f) return Vector2f()

        // Clamp new heading to not violate any of the speed limits.
        val newSpeed = limits.fold(expectedSpeed) { clampedSpeed, lim ->
            lim.clampSpeed(newFacing, clampedSpeed)
        }

        return expectedVelocity.rotated(angleToNewFacing.rotationMatrix).resized(newSpeed)
    }

    /** Clamp expectedSpeed to maximum speed in which ship can travel
     * along the expectedHeading and not break the Limit.
     *
     * From right triangle, the equation for max speed is:
     * maxSpeed = speedLimit / cos( abs(limitFacing - velocityFacing) )
     *
     * To avoid using trigonometric functions, f(x) = 1/cos(x) is approximated as
     * g(t) = 1/t + t/5 where t = PI/2 - x. */
    private fun Limit.clampSpeed(expectedHeading: Direction, expectedSpeed: Float): Float {
        val angleFromLimit = (heading - expectedHeading).length
        if (angleFromLimit >= 90f) return expectedSpeed

        val t = (PI / 2f - angleFromLimit * DEGREES_TO_RADIANS)
        val e = speed * (1f / t + t / 5f)
        return min(e, expectedSpeed)
    }

    /** Calculate angle from the limit heading, at which traveling
     * with expectedSpeed will not break the limit. */
    private fun Limit.headingOffset(expectedSpeed: Float): Float {
        val a = 1f
        val b = -5f * (expectedSpeed / speed)
        val c = 5f

        val t = quad(a, b, c)!!.second
        return abs(t - PI / 2f) * RADIANS_TO_DEGREES
    }
}
