package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.Rotation.Companion.rotated
import com.genir.aitweaks.core.utils.extensions.facing
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.resized
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

class EngineController(ship: ShipAPI) : BasicEngineController(ship) {
    /** Limit allows to restrict velocity to not exceed
     * max speed in a direction along a given heading. */
    data class Limit(val heading: Float, val speed: Float)

    /** Set ship heading towards selected location. Appropriate target
     * leading is calculated based on estimated target velocity. If ship
     * is already at 'heading' location, it will match the target velocity.
     * Limits are used to restrict the velocity, e.g. for collision avoidance
     * purposes. Returns the calculated expected velocity. */
    fun heading(dt: Float, heading: Vector2f, limits: List<Limit> = listOf()): Vector2f {
        return super.heading(dt, heading) { toShipFacing, ve -> limitVelocity(dt, toShipFacing, ve, limits) }
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
        val angleFromLimit = abs(shortestRotation(expectedHeading, heading))
        if (angleFromLimit >= 90f) return expectedSpeed

        val t = (PI / 2f - angleFromLimit * DEGREES_TO_RADIANS).toFloat()
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
        return abs(t - PI / 2f).toFloat() * RADIANS_TO_DEGREES
    }
}
