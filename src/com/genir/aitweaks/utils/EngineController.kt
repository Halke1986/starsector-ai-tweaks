package com.genir.aitweaks.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.utils.extensions.strafeAcceleration
import org.lazywizard.lazylib.MathUtils.getShortestRotation
import org.lazywizard.lazylib.ext.*
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

const val noMovementExpected = Float.MAX_VALUE
const val targetReachedThreshold = 2f

class EngineController {
    /** Set ship heading towards 'target' location. Appropriate target leading
     * is added to calculated heading to account for 'targetVelocity'. If ship
     * is already at 'target' location, it will match 'targetVelocity'.
     * Returns the calculated heading angle. */
    fun heading(ship: ShipAPI, target: Vector2f, targetVelocity: Vector2f): Float {
        val tr = target - ship.location
        val trl = tr.length()

        // Stop if reached stationary target.
        if (trl < targetReachedThreshold && targetVelocity.isZeroVector()) {
            if (!ship.velocity.isZeroVector()) ship.move(DECELERATE)
            return noMovementExpected
        }

        // Clamp target velocity so that the component towards
        // the ship is ignored. Without this, the ship would
        // slow down to match target velocity when still far
        // from the target.
        val a = Rotation(90f - tr.getFacing())
        val vp = a.rotate(targetVelocity)
        val vc = a.reverse(Vector2f(vp.x, max(vp.y, 0f)))

        // Transform input into ship frame of reference.
        // Account for ship angular velocity, as linear
        // acceleration is applied by the game engine after
        // rotation. Change unit of time from second to
        // animation frame duration (* dt).
        val dt = Global.getCombatEngine().elapsedInLastFrame
        val w = ship.angularVelocity * dt
        val r = Rotation(90f - ship.facing - w)
        val d = r.rotate(tr)
        val v = r.rotate(ship.velocity) * dt
        val vt = r.rotate(vc) * dt
        val vr = v - vt

        // Calculate expected velocity change.
        val vMax = ship.maxSpeed * dt
        val vExpected = (Vector2f(d).resize(vMax) + vt).resize(vMax)
        val dv = vExpected - v

        val af = ship.acceleration * dt * dt
        val ab = ship.deceleration * dt * dt
        val al = ship.strafeAcceleration * dt * dt

        // Calculate proportional thrust required
        // to achieve the expected velocity change.
        val absAccel = listOf(dv.y / af, -dv.y / ab, -dv.x / al, dv.x / al).map { if (it > 1f) it else 0f }
        val maxAccel = absAccel.maxOrNull()!!
        val f = absAccel.map { if (maxAccel != 0f) it / maxAccel else 0f }.toMutableList()

        // Apply forward trust continuously if heading almost
        // straight towards target, for visual effect.
        if (d.y > 20f * abs(d.x)) f[0] = 1f

        if (shouldAccelerate(+d.y, +vr.y, f[0], af, ab)) ship.move(ACCELERATE)
        if (shouldAccelerate(-d.y, -vr.y, f[1], ab, af)) ship.move(ACCELERATE_BACKWARDS)
        if (shouldAccelerate(-d.x, -vr.x, f[2], al, al)) ship.move(STRAFE_LEFT)
        if (shouldAccelerate(+d.x, +vr.x, f[3], al, al)) ship.move(STRAFE_RIGHT)

        // If close to target, return target heading as expected heading.
        // Else, return actual expected heading.
        return if (trl < 10f) targetVelocity.getFacing()
        else return r.reverse(vExpected).getFacing()
    }

    /** Set ship facing towards 'target' location. If ship is already facing
     * 'target' location, it will match the angular component of 'targetVelocity'.
     * Returns the calculated facing angle. */
    fun facing(ship: ShipAPI, target: Vector2f, targetVelocity: Vector2f, facingOffset: Float = 0f): Float {
        val tr = target - ship.location
        if (tr.length() < targetReachedThreshold) return noMovementExpected

        // Calculate parameters of the rotation
        // needed to match the expected facing.
        val dt = Global.getCombatEngine().elapsedInLastFrame
        val expectedFacing = tr.getFacing() + facingOffset
        val r = getShortestRotation(ship.facing, expectedFacing)
        val a = ship.turnAcceleration * dt * dt
        val w = ship.angularVelocity * dt

        // Calculate target relative angular velocity.
        // Required to calculate correct braking distance.
        val tr2 = tr + (targetVelocity - ship.velocity) * dt
        val f2 = tr2.getFacing() + facingOffset
        val wt = getShortestRotation(expectedFacing, f2)
        val wr = w - wt

        if (shouldAccelerate(+r, +wr, 1f, a, a)) ship.move(TURN_LEFT)
        if (shouldAccelerate(-r, -wr, 1f, a, a)) ship.move(TURN_RIGHT)

        return expectedFacing
    }

    /** Decide if the ship should accelerate in the given
     * direction to reach its target without overshooting. */
    private fun shouldAccelerate(d: Float, v: Float, f: Float, ap: Float, an: Float) = when {
        d < 0 && v < 0 && vMax(-d, -v, ap) < -v -> true // decelerate to avoid overshooting target
        d < 0 || f == 0f -> false
        v + ap > vMax(d, v, an) -> false // accelerate only if it will not cause overshooting
        f >= 1f -> true
        Random.nextFloat() < f -> true // apply proportional thrust
        else -> false
    }

    /** Maximum velocity in given direction to not overshoot target. */
    private fun vMax(d: Float, v: Float, a: Float): Float {
        val s = d - v  // location in next frame
        val t = sqrt(2f * s / a)
        val k = a / 2f // correction to take into account game engine discrete calculations
        return t * a - k
    }

    private fun ShipAPI.move(cmd: ShipCommand) = this.giveCommand(cmd, null, 0)
}