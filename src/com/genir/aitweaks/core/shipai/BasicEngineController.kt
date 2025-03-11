package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.sqrt
import org.lazywizard.lazylib.ext.clampLength
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
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
    fun heading(dt: Float, heading: Vector2f, targetVelocity: Vector2f, limitVelocity: ((Direction, Vector2f) -> Vector2f)? = null): Vector2f {
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
        val toShipFacing = (-ship.facing.direction) - w + 90f
        val r = toShipFacing.rotationMatrix
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
            if (ship.velocity.isNonZero) {
                ship.command(DECELERATE)
            }
            return Vector2f()
        }

        // Proportional thrust required to achieve
        // the expected velocity change.
        val ff = +dv.y / af
        val fb = -dv.y / ab
        val fl = -dv.x / al
        val fr = +dv.x / al
        val fMax = max(max(ff, fb), max(fl, fr))

        val overspeedX = vmx.sign == v.x.sign && abs(vmx) < abs(v.x)
        val overspeedY = vmy.sign == v.y.sign && abs(vmy) < abs(v.y)

        // Give commands to achieve the calculated thrust.
        if (shouldAccelerate(overspeedY, ff, fMax)) ship.command(ACCELERATE)
        if (shouldAccelerate(overspeedY, fb, fMax)) ship.command(ACCELERATE_BACKWARDS)
        if (shouldAccelerate(overspeedX, fl, fMax)) ship.command(STRAFE_LEFT)
        if (shouldAccelerate(overspeedX, fr, fMax)) ship.command(STRAFE_RIGHT)

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
    fun facing(dt: Float, facing: Direction, targetAngularVelocity: Float) {
        // Change unit of time from second to
        // animation frame duration (* dt).
        val w = ship.angularVelocity * dt
        val a = ship.turnAcceleration * dt * dt
        val d = min(abs(w), ship.turnDeceleration * dt * dt) * -sign(w)

        // Estimate target angular velocity.
        val wt = targetAngularVelocity * dt

        // Angular distance between expected facing and ship facing.
        val r = facing - ship.facing.direction

        // Expected velocity change.
        val we = r.sign * vMax(r.length, a) + wt
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

    companion object {
        /** Maximum velocity calculations for use outside the engine controller. */
        fun vMax(dt: Float, dist: Float, deceleration: Float): Float {
            return vMax(dist, deceleration * dt * dt) / dt
        }

        /** Calculates the maximum velocity in a given direction to avoid
         * overshooting the target at distance `s` with acceleration `a`. */
        private fun vMax(s: Float, a: Float): Float {
            if (s <= 0f) {
                return 0f
            }

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
            // Finally, v(s) can be extended to real arguments by using
            // the following approximation:
            return sqrt((a * a) + (2 * a * s)) - a
        }

        /** Decide if the ship should accelerate in the given
         * direction to reach its target without overshooting. */
        private fun shouldAccelerate(overspeed: Boolean, f: Float, m: Float) = when {
            f < 0.5f -> false
            overspeed && f >= 0.5f -> true // braking to not overshoot target
            f >= m -> true
            else -> f / m > Random.nextFloat() // apply proportional thrust
        }
    }
}
