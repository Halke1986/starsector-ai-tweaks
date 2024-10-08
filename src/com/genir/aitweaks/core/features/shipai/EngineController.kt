package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.*
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.clampLength
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*
import kotlin.random.Random

/** Engine Controller controls the ship heading and facing
 * by issuing appropriate engine commands.
 *
 * Note: Due to Starsector combat engine order of operations,
 * the controller works better when called from ship AI, as
 * opposed to every frame combat plugin. */
class EngineController(val ship: ShipAPI) {
    private var prevFacing: Float = 0f
    private var prevHeading: Vector2f = Vector2f()

    /** Limit allows to restrict velocity to not exceed
     * max speed in a direction along a given heading. */
    data class Limit(val heading: Float, val speed: Float)

    /** Values used to decelerate the ship to standstill. */
    val allStop: Vector2f = Vector2f(Float.MAX_VALUE, Float.MAX_VALUE)
    val rotationStop: Float = Float.MAX_VALUE

    /** Set ship heading towards selected location. Appropriate target
     * leading is calculated based on estimated target velocity. If ship
     * is already at 'heading' location, it will match the target velocity.
     * Limits are used to restrict the velocity, e.g. for collision avoidance
     * purposes. Returns the calculated expected velocity. */
    fun heading(dt: Float, heading: Vector2f, limits: List<Limit> = listOf()): Vector2f {
        if (heading == allStop) return stop()

        // Change unit of time from second to
        // animation frame duration (* dt).
        val af = ship.acceleration * dt * dt
        val ab = ship.deceleration * dt * dt
        val al = ship.strafeAcceleration * dt * dt
        val vMax = max(ship.maxSpeed, ship.velocity.length) * dt + af

        // Transform input into ship frame of reference. Account for
        // ship angular velocity, as linear acceleration is applied
        // by the game engine after rotation.
        val w = ship.angularVelocity * dt
        val toShipFacing = 90f - ship.facing - w
        val r = Rotation(toShipFacing)
        val d = (heading - ship.location).rotated(r)
        val v = (ship.velocity).rotated(r) * dt

        // Estimate target linear velocity.
        val vt = (heading - prevHeading).rotated(r)
        prevHeading = heading.copy

        // Maximum velocity towards target for both axis of movement.
        // Any higher velocity would lead to overshooting target location.
        // Note: vmx and vmy are always integer multiples of respective
        // acceleration values, without any fractional part.
        val vmx = if (d.x > 0) vMax(d.x, al) else -vMax(-d.x, al)
        val vmy = if (d.y > 0) vMax(d.y, ab) else -vMax(-d.y, af)

        // Expected velocity directly towards target location.
        val vtt = if (vmx == 0f || vmy == 0f) Vector2f(vmx, vmy)
        else d * min(vmx / d.x, vmy / d.y)

        // Expected velocity change.
        val ve = (vtt + vt).clampLength(vMax)
        val vec = limitVelocity(dt, toShipFacing, ve, limits)
        val dv = vec - v

        // Stop if expected velocity is less than half of velocity
        // change unit. Stop is applied regardless of distance to
        // target in case the low velocity is the result of a
        // collision avoidance speed limit.
        if (vec.length < af / 2f) return stop()

        // Proportional thrust required to achieve
        // the expected velocity change.
        val ff = +dv.y / af
        val fb = -dv.y / ab
        val fl = -dv.x / al
        val fr = +dv.x / al
        val fMax = max(max(ff, fb), max(fl, fr))

        // Give commands to achieve the calculated thrust.
        if (shouldAccelerate(+d.y, ff, fMax)) ship.command(ACCELERATE)
        if (shouldAccelerate(-d.y, fb, fMax)) ship.command(ACCELERATE_BACKWARDS)
        if (shouldAccelerate(-d.x, fl, fMax)) ship.command(STRAFE_LEFT)
        if (shouldAccelerate(+d.x, fr, fMax)) ship.command(STRAFE_RIGHT)

        return vec.rotatedReverse(r) / dt
    }

    /** Set ship facing. */
    fun facing(dt: Float, facing: Float) {
        if (facing == rotationStop && ship.angularVelocity == 0f) return

        // Change unit of time from second to
        // animation frame duration (* dt).
        val a = ship.turnAcceleration * dt * dt
        val w = ship.angularVelocity * dt

        // Estimate target angular velocity.
        val wt = MathUtils.getShortestRotation(prevFacing, facing)
        prevFacing = facing

        // Angular distance between expected facing and ship facing.
        val r = MathUtils.getShortestRotation(ship.facing, facing)

        // Expected velocity change.
        val we = if (facing == rotationStop) 0f else sign(r) * vMax(abs(r), a) + wt
        val dw = we - w

        if (shouldAccelerate(+r, +dw / a, 0f)) ship.command(TURN_LEFT)
        if (shouldAccelerate(-r, -dw / a, 0f)) ship.command(TURN_RIGHT)
    }

    /** Decelerate the ship to standstill. */
    private fun stop(): Vector2f {
        if (!ship.velocity.isZeroVector()) ship.command(DECELERATE)
        return Vector2f()
    }

    /** Calculate the maximum velocity in a given direction to
     * avoid overshooting the target.
     *
     * Note: The Starsector engine simulates motion in a discrete
     * manner, where the distance covered during accelerated motion
     * is calculated using the equation:
     *
     * s = (a * t^2 / 2) + (a * t * u / 2)
     *
     * where `a` is acceleration, `t` is time, and `u` is the duration
     * of a single simulation frame. */
    private fun vMax(d: Float, a: Float): Float {
        val (q, _) = quad(0.5f, 0.5f, -d / a) ?: return 0f
        return floor(q) * a
    }

    /** Decide if the ship should accelerate in the given
     * direction to reach its target without overshooting. */
    private fun shouldAccelerate(d: Float, f: Float, m: Float) = when {
        f < 0.5f -> false
        d < 0f && f >= 0.5f -> true // braking to not overshoot target
        f >= m -> true
        else -> f / m > Random.nextFloat() // apply proportional thrust
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
        val angleToLimit = MathUtils.getShortestRotation(expectedHeading, lowestLimit.heading)
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
        val angleFromLimit = abs(MathUtils.getShortestRotation(expectedHeading, heading))
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
