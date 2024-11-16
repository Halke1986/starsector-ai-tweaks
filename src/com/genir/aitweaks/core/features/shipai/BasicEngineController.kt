package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.core.utils.Rotation
import com.genir.aitweaks.core.utils.Rotation.Companion.rotated
import com.genir.aitweaks.core.utils.Rotation.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.div
import com.genir.aitweaks.core.utils.extensions.isZero
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.minus
import com.genir.aitweaks.core.utils.extensions.plus
import com.genir.aitweaks.core.utils.shortestRotation
import com.genir.aitweaks.core.utils.times
import org.lazywizard.lazylib.ext.clampLength
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*
import kotlin.random.Random

/**
 * Basic Engine Controller controls the ship heading and facing
 * by issuing appropriate engine commands.
 *
 * Note: Due to Starsector combat engine order of operations,
 * the controller works better when called from ship AI, as
 * opposed to every frame combat plugin.
 */
open class BasicEngineController(val ship: ShipAPI) {
    /**
     * Set ship heading towards selected location. Appropriate target
     * leading is calculated based on provided target velocity. If the ship
     * is already at 'heading' location, it will match the target velocity.
     * `limitVelocity` lambda is used to restrict the velocity, e.g. for
     * collision avoidance purposes. Returns the calculated expected velocity.
     */
    fun heading(dt: Float, heading: Vector2f, targetVelocity: Vector2f, limitVelocity: ((Float, Vector2f) -> Vector2f)? = null): Vector2f {
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
        val vt = targetVelocity.rotated(r) * dt

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
        if (vec.length < af / 2f) {
            if (!ship.velocity.isZero) ship.command(DECELERATE)
            return Vector2f()
        }

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

    /**
     * Set ship facing.
     *
     * Due to how Starsector combat engine works, the actual change in ship facing
     * takes effect in the following frame, when the provided facing is already
     * obsolete. However, the engine controller matches the ship angular velocity
     * with changing facing values, effectively extrapolating the expected ship
     * facing to the next frame.
     */
    fun facing(dt: Float, facing: Float, targetAngularVelocity: Float) {
        // Change unit of time from second to
        // animation frame duration (* dt).
        val w = ship.angularVelocity * dt
        val a = ship.turnAcceleration * dt * dt
        val d = min(abs(w), ship.turnDeceleration * dt * dt) * -sign(w)

        // Estimate target angular velocity.
        val wt = targetAngularVelocity * dt

        // Angular distance between expected facing and ship facing.
        val r = shortestRotation(ship.facing, facing)

        // Expected velocity change.
        val we = sign(r) * vMax(abs(r), a) + wt
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

    /** Calculates the maximum velocity in a given direction to avoid
     * overshooting a target at distance `s` with acceleration `a`. */
    private fun vMax(s: Float, a: Float): Float {
        // The Starsector engine simulates motion in a discrete manner, where
        // the distance covered during accelerated motion is described using
        // a non-Newtonian formula:
        //
        // s(0) = 0
        // s(n) = s(n-1) + n a u
        //
        // where `s(n)` is the distance covered after `n` frames, `a` is
        // acceleration per frame and `u` is the duration of a single
        // simulation frame (here normalized to 1). This recursive formula
        // can be converted into an explicit integer function and solved
        // for `v` (where `v = a n`), giving:
        //
        // v(s) = sqrt(a² / 4 + 2 a s) - a / 2
        //
        // Finally, v(s) can be extended to real arguments by using the
        // following approximation:
        return sqrt((a * a) + (2f * a * s)) - a
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
