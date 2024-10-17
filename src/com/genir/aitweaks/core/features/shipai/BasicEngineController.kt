package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.core.utils.Rotation
import com.genir.aitweaks.core.utils.div
import com.genir.aitweaks.core.utils.extensions.*
import com.genir.aitweaks.core.utils.quad
import com.genir.aitweaks.core.utils.times
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.clampLength
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.random.Random

/** Engine Controller controls the ship heading and facing
 * by issuing appropriate engine commands.
 *
 * Note: Due to Starsector combat engine order of operations,
 * the controller works better when called from ship AI, as
 * opposed to every frame combat plugin. */
open class BasicEngineController(val ship: ShipAPI) {
    private var prevFacing: Float = 0f
    private var prevHeading: Vector2f = Vector2f()

    /** Values used to decelerate the ship to standstill. */
    val allStop: Vector2f = Vector2f(Float.MAX_VALUE, Float.MAX_VALUE)
    val rotationStop: Float = Float.MAX_VALUE

    /** Set ship heading towards selected location. Appropriate target
     * leading is calculated based on estimated target velocity. If the ship
     * is already at 'heading' location, it will match the target velocity.
     * limitVelocity lambda is used to restrict the velocity, e.g. for collision
     * avoidance purposes. Returns the calculated expected velocity. */
    fun heading(dt: Float, heading: Vector2f, limitVelocity: ((Float, Vector2f) -> Vector2f)? = null): Vector2f {
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
        val vmx = if (d.x > 0) vMax(d.x, al) else -vMax(-d.x, al)
        val vmy = if (d.y > 0) vMax(d.y, ab) else -vMax(-d.y, af)

        // Expected velocity directly towards target location.
        val vtt = if (vmx == 0f || vmy == 0f) Vector2f(vmx, vmy)
        else d * min(vmx / d.x, vmy / d.y)

        // Expected velocity change.
        val ve = (vtt + vt).clampLength(vMax)
        val vec = limitVelocity?.invoke(toShipFacing, ve) ?: ve
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

    fun facing(dt: Float, facingNextFrame: Vector2f) {
        facing(dt, (facingNextFrame - ship.location).facing)
    }

    /**
     * Set ship facing.
     *
     * The provided facing parameter should represent the expected ship facing
     * for the current frame, as long as the method if called from within
     * ShipAIPlugin.advance.
     *
     * Due to how Starsector combat engine works, the actual change in ship facing
     * takes effect in the following frame, when the provided facing is already
     * obsolete. However, the engine controller matches the ship angular velocity
     * with changing facing values, effectively extrapolating the expected ship
     * facing to the next frame.
     *
     * This is important when using ship facing to aim hardpoints: facing value
     * should be calculated based on current frame target leading solution.
     */
    fun facing(dt: Float, facing: Float) {
        if (facing == rotationStop && ship.angularVelocity == 0f) return

        // Change unit of time from second to
        // animation frame duration (* dt).
        val w = ship.angularVelocity * dt
        val a = ship.turnAcceleration * dt * dt
        val d = min(abs(w), ship.turnDeceleration * dt * dt) * -sign(w)

        // Estimate target angular velocity.
        val wt = MathUtils.getShortestRotation(prevFacing, facing)
        prevFacing = facing

        // Angular distance between expected facing and ship facing.
        val r = MathUtils.getShortestRotation(ship.facing, facing)

        // Expected velocity change.
        val we = if (facing == rotationStop) 0f else sign(r) * vMax(abs(r), a) + wt
        val dw = we - w

        // Compare each possible movement option
        // against the expected velocity change.
        val er = abs(dw + a)
        val ed = abs(dw - d)
        val el = abs(dw - a)

        when {
            er < el && er < ed -> ship.command(TURN_RIGHT)
            el < ed -> ship.command(TURN_LEFT)
            else -> Unit // Let the ship decelerate.
        }
    }

    /** Decelerate the ship to standstill. */
    private fun stop(): Vector2f {
        if (!ship.velocity.isZeroVector()) ship.command(DECELERATE)
        return Vector2f()
    }

    /** Calculate the maximum velocity in a given direction to
     * avoid overshooting the target.
     *
     * The Starsector engine simulates motion in a discrete manner,
     * where the distance covered during accelerated motion is
     * calculated using the following non-Newtonian equation:
     *
     * 2 s = a t t + a t u
     *
     * where `a` is acceleration, `t` is time, and `u` is the duration
     * of a single simulation frame, here normalized to 1. This gives
     * the following equation for velocity:
     *
     * 0 = v v / a + v - 2 s
     */
    private fun vMax(s: Float, a: Float): Float {
        return quad(1f / a, 1f, -s * 2f)?.first ?: 0f
    }

    /** Decide if the ship should accelerate in the given
     * direction to reach its target without overshooting. */
    private fun shouldAccelerate(d: Float, f: Float, m: Float) = when {
        f < 0.5f -> false
        d < 0f && f >= 0.5f -> true // braking to not overshoot target
        f >= m -> true
        else -> f / m > Random.nextFloat() // apply proportional thrust
    }
}
