package com.genir.aitweaks.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.*
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*
import kotlin.random.Random

/**
 * Ship engine controller.
 *
 * Can be used to set ship heading and facing towards selected target.
 * The controller attempts not only to move the ship to the selected
 * target, but also match the target speed. This results in the ship
 * being able to follow moving targets with very little lag.
 *
 * Same instance of controller should be used each frame, otherwise
 * the target speed matching will not work.
 */
class EngineController(val ship: ShipAPI) {
    private var prevTargetLocation: Vector2f? = null
    private var prevTargetFacing: Float? = null

    private val noMovementExpected = Float.MAX_VALUE

    /** Set ship heading towards 'target' location. Appropriate target leading
     * is calculated based on estimated target velocity. If ship
     * is already at 'target' location, it will match the target velocity.
     * Returns the expected heading angle. */
    fun heading(target: Vector2f): Float {
        // Change unit of time from second to
        // animation frame duration (* dt).
        val dt = Global.getCombatEngine().elapsedInLastFrame
        val vMax = ship.maxSpeed * dt
        val af = ship.acceleration * dt * dt
        val ab = ship.deceleration * dt * dt
        val al = ship.strafeAcceleration * dt * dt

        // Transform input into ship frame of reference.
        // Account for ship angular velocity, as linear
        // acceleration is applied by the game engine after
        // rotation.
        val w = ship.angularVelocity * dt
        val r = Rotation(90f - ship.facing - w)
        val l = r.rotate(target - ship.location)
        val v = r.rotate(ship.velocity) * dt

        // Estimate target velocity.
        val vt = r.rotate(target - (prevTargetLocation ?: target))
        if (!Global.getCombatEngine().isPaused) prevTargetLocation = target

        // Stop if reached stationary target. Distance threshold for
        // stopping is the distance the ship covers in one frame after
        // accelerating from a standstill.
        if ((target - ship.location).length() < af && vt.isZeroVector()) {
            if (!ship.velocity.isZeroVector()) ship.move(DECELERATE)
            return noMovementExpected
        }

        // Distance to target location in next frame. Next frame
        // is used because game engine apparently updates location
        // before updating velocity, so it's only the next frame
        // the current changes will take effect.
        val d = l - v + vt

        // Velocity towards target,
        // in target frame of reference.
        val vtt = Vector2f(d).resize(vMax) - vt

        // Velocity towards target, limited to
        // not overshoot the target location.
        val vtm = Vector2f()
        vtm.x = if (vtt.x > 0) min(vtt.x, vMax(d.x, al))
        else -min(-vtt.x, vMax(-d.x, al))
        vtm.y = if (vtt.y > 0) min(vtt.y, vMax(d.y, ab))
        else -min(-vtt.y, vMax(-d.y, af))

        // Expected velocity change, in ship frame of reference.
        val ve = (vtm + vt).clampLength(vMax)
        val dv = ve - v

        // Calculate proportional thrust required
        // to achieve the expected velocity change.
        var ff = +dv.y / af
        val fb = -dv.y / ab
        val fl = -dv.x / al
        val fr = +dv.x / al
        val fMax = max(max(ff, fb), max(fl, fr))

        // Apply forward trust continuously if heading almost
        // straight towards target, for visual effect.
        if (d.y > 20f * abs(d.x) && fb < 0.5f) ff = max(1f, fMax)

        if (shouldAccelerate(+d.y, ff, fMax)) ship.move(ACCELERATE)
        if (shouldAccelerate(-d.y, fb, fMax)) ship.move(ACCELERATE_BACKWARDS)
        if (shouldAccelerate(-d.x, fl, fMax)) ship.move(STRAFE_LEFT)
        if (shouldAccelerate(+d.x, fr, fMax)) ship.move(STRAFE_RIGHT)

        return r.reverse(ve).getFacing()
    }

    /** Set ship facing. Returns the expected facing angle. */
    fun facing(f: Float): Float {
        val dt = Global.getCombatEngine().elapsedInLastFrame
        val a = ship.turnAcceleration * dt * dt
        val w = ship.angularVelocity * dt

        // Estimate the speed of facing change.
        val df = f - (prevTargetFacing ?: f)
        if (!Global.getCombatEngine().isPaused) prevTargetFacing = f

        // Angular distance between expected
        // facing and ship facing the next frame.
        val r = MathUtils.getShortestRotation(ship.facing + w, f + df)

        // Expected velocity change. Since rotation is
        // 1-dimensional, as opposed to the 2-dimensional
        // heading, the calculations can be simplified.
        val we = sign(r) * vMax(abs(r), a) + df
        val dw = we - w

        if (shouldAccelerate(+r, +dw / a, 0f)) ship.move(TURN_LEFT)
        if (shouldAccelerate(-r, -dw / a, 0f)) ship.move(TURN_RIGHT)

        return f
    }

    /** Set ship facing towards 'target' location.
     * Returns the expected facing angle. */
    fun facing(target: Vector2f): Float {
        val tr = target - ship.location
        if (tr.length() < ship.collisionRadius / 2f)
            return prevTargetFacing ?: noMovementExpected

        return facing(tr.getFacing())
    }

    /** Maximum velocity in given direction to not overshoot target. */
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

    private fun ShipAPI.move(cmd: ShipCommand) = this.giveCommand(cmd, null, 0)

    private val ShipAPI.strafeAcceleration: Float
        get() = this.acceleration * when (this.hullSize) {
            ShipAPI.HullSize.FIGHTER -> 0.75f
            ShipAPI.HullSize.FRIGATE -> 1.0f
            ShipAPI.HullSize.DESTROYER -> 0.75f
            ShipAPI.HullSize.CRUISER -> 0.5f
            ShipAPI.HullSize.CAPITAL_SHIP -> 0.25f
            else -> 1.0f
        }
}