package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.Rotation.Companion.rotated
import com.genir.aitweaks.core.utils.extensions.copy
import com.genir.aitweaks.core.utils.extensions.facing
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.resized
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

/** Engine Controller for AI piloted ships. */
class EngineController(ship: ShipAPI) : BasicEngineController(ship) {
    private var prevFacing: Float = 0f
    private var prevHeading: Vector2f = Vector2f()

    /** Values used to decelerate the ship to standstill. */
    val allStop: Vector2f = Vector2f(Float.MAX_VALUE, Float.MAX_VALUE)
    val rotationStop: Float = Float.MAX_VALUE

    /** Limit allows to restrict velocity to not exceed
     * max speed in a direction along a given heading. */
    data class Limit(val heading: Float, val speed: Float)

    fun heading(dt: Float, heading: Vector2f, limits: List<Limit> = listOf()): Vector2f {
        if (heading == allStop) return heading(dt, ship.location, Vector2f())

        // Estimate target linear velocity.
        val vt = (heading - prevHeading) / dt
        prevHeading = heading.copy

        return heading(dt, heading, vt) { toShipFacing, ve -> limitVelocity(dt, toShipFacing, ve, limits) }
    }

    fun facing(dt: Float, facing: Float) {
        if (facing == rotationStop) return facing(dt, ship.facing, 0f)

        // Estimate target angular velocity.
        val wt = shortestRotation(prevFacing, facing) / dt
        prevFacing = facing

        facing(dt, facing, wt)
    }

    private fun limitVelocity(dt: Float, toShipFacing: Float, expectedVelocity: Vector2f, absLimits: List<Limit>): Vector2f {
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
        val angleToLimit = shortestRotation(expectedHeading, lowestLimit.heading)
        val angleToNewFacing = angleToLimit - headingOffset * angleToLimit.sign
        val newFacing = expectedHeading + angleToNewFacing

        // Stop if angle to new heading is right, to avoid erratic behavior
        // when avoiding collision and being stopped close to destination.
        if (angleToNewFacing >= 89f) return Vector2f()

        // Clamp new heading to not violate any of the speed limits.
        val newSpeed = limits.fold(expectedSpeed) { clampedSpeed, lim ->
            lim.clampSpeed(newFacing, clampedSpeed)
        }

        return expectedVelocity.rotated(Rotation(angleToNewFacing)).resized(newSpeed)
    }

    /** Clamp expectedSpeed to maximum speed in which ship can travel
     * along the expectedHeading and not break the Limit.
     *
     * From right triangle, the equation for max speed is:
     * maxSpeed = speedLimit / cos( abs(limitFacing - velocityFacing) )
     *
     * To avoid using trigonometric functions, f(x) = 1/cos(x) is approximated as
     * g(t) = 1/t + t/5 where t = PI/2 - x. */
    private fun Limit.clampSpeed(expectedHeading: Float, expectedSpeed: Float): Float {
        val angleFromLimit = absShortestRotation(expectedHeading, heading)
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
