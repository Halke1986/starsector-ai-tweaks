package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.sqrt
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedReverse
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
open class BasicEngineController(val movement: Movement) : Helm(movement.ship) {
    private var prevFacing: Direction = movement.facing

    data class LimitedVelocity(val movementOverridden: Boolean, val velocity: Vector2f)

    /**
     * Set ship heading towards selected location. Appropriate target
     * leading is calculated based on provided target velocity. If the ship
     * is already at 'heading' location, it will match the target velocity.
     * `limitVelocity` lambda is used to restrict the velocity, e.g. for
     * collision avoidance purposes. Returns the calculated expected velocity.
     */
    fun heading(dt: Float, heading: Vector2f, targetVelocity: Vector2f, limitVelocity: ((Direction, Vector2f) -> LimitedVelocity?)? = null): Vector2f {
        // Change unit of time from second to
        // animation frame duration (* dt).
        val af = movement.acceleration * dt * dt
        val ab = movement.deceleration * dt * dt
        val al = movement.strafeAcceleration * dt * dt
        val vMax = max(movement.maxSpeed, movement.velocity.length) * dt + af

        // Transform input into ship frame of reference. Account for
        // ship angular velocity, as linear acceleration is applied
        // by the game engine after rotation.
        val w = movement.angularVelocity * dt
        val toShipFacing = -(movement.facing + w.toDirection) + 90f.toDirection
        val r = toShipFacing.rotationMatrix
        val d = (heading - movement.location).rotated(r)
        val v = (movement.velocity).rotated(r) * dt
        val vt = targetVelocity.rotated(r) * dt

        // Maximum velocity towards target for both axis of movement.
        // Any higher velocity would lead to overshooting target location.
        val vmx = if (d.x > 0) vMax(d.x, al) else -vMax(-d.x, al)
        val vmy = if (d.y > 0) vMax(d.y, ab) else -vMax(-d.y, af)

        // Expected velocity directly towards target location.
        val vtt = if (vmx == 0f || vmy == 0f) Vector2f(vmx, vmy)
        else d * min(vmx / d.x, vmy / d.y)

        // Allow velocity limiting logic to handle the ship movement, if required.
        val limitedVelocity = limitVelocity?.invoke(toShipFacing, vtt + vt)
        if (limitedVelocity?.movementOverridden == true) {
            return limitedVelocity.velocity.rotatedReverse(r) / dt
        }

        // Expected velocity change.
        val ve = limitedVelocity?.velocity ?: (vtt + vt).clampedLength(vMax)
        val dv = ve - v

        // Stop if arrived at location, that is when expected velocity change
        // and location change is less than half of velocity change unit.
        if (ve.length < af / 2) {
            if (movement.velocity.isNonZero) {
                giveCommand(DECELERATE)
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

        val overSpeedX = vmx.sign == v.x.sign && abs(vmx) < abs(v.x)
        val overSpeedY = vmy.sign == v.y.sign && abs(vmy) < abs(v.y)

        // Give commands to achieve the calculated thrust.
        if (shouldAccelerate(overSpeedY, ff, fMax)) giveCommand(ACCELERATE)
        if (shouldAccelerate(overSpeedY, fb, fMax)) giveCommand(ACCELERATE_BACKWARDS)
        if (shouldAccelerate(overSpeedX, fl, fMax)) giveCommand(STRAFE_LEFT)
        if (shouldAccelerate(overSpeedX, fr, fMax)) giveCommand(STRAFE_RIGHT)

        return ve.rotatedReverse(r) / dt
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
        val w = movement.angularVelocity * dt
        val a = movement.turnAcceleration * dt * dt
        val d = min(abs(w), movement.turnDeceleration * dt * dt) * -sign(w)

        // Estimate target angular velocity.
        val wt = targetAngularVelocity * dt

        // Angular distance between expected facing and ship facing.
        val r = facing - movement.facing

        // Expected velocity change.
        val we = r.sign * vMax(r.length, a) + wt
        val dw = we - w

        // Compare each possible movement option
        // against the expected velocity change.
        val er = abs(dw + a)
        val ed = abs(dw - d)
        val el = abs(dw - a)

        when {
            er < el && er < ed -> giveCommand(TURN_RIGHT)
            el < ed -> giveCommand(TURN_LEFT)
            else -> Unit // Let the ship decelerate.
        }
    }

    /** Set ship facing using estimated target angular velocity. */
    fun facing(dt: Float, facing: Direction, shouldStop: Boolean) {
        if (shouldStop) {
            return facing(dt, movement.facing, 0f)
        }

        // Estimate target angular velocity.
        val wt = (facing - prevFacing).degrees / dt
        prevFacing = facing

        facing(dt, facing, wt)
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
