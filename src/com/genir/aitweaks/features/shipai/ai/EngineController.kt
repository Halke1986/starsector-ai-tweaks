package com.genir.aitweaks.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.debug.debugVertex
import com.genir.aitweaks.utils.Rotation
import com.genir.aitweaks.utils.div
import com.genir.aitweaks.utils.extensions.rotated
import com.genir.aitweaks.utils.extensions.rotatedReverse
import com.genir.aitweaks.utils.quad
import com.genir.aitweaks.utils.times
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.*
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.*
import kotlin.random.Random

class EngineController(val ship: ShipAPI) {
    private val noMovementExpected = Float.MAX_VALUE

    /** Set ship heading towards 'target' location. Appropriate target
     * leading is calculated based on 'targetVelocity'. If ship is already
     * at 'target' location, it will match the target velocity. Returns the
     * expected heading angle. */
    fun heading(target: Vector2f, targetVelocity: Vector2f, clampV: ((Vector2f) -> Vector2f)? = null): Float {
        // Change unit of time from second to
        // animation frame duration (* dt).
        val dt = Global.getCombatEngine().elapsedInLastFrame
        val af = ship.acceleration * dt * dt
        val ab = ship.deceleration * dt * dt
        val al = ship.strafeAcceleration * dt * dt
        val vMax = max(ship.maxSpeed, ship.velocity.length()) * dt + af

        // Transform input into ship frame of reference. Account for
        // ship angular velocity, as linear acceleration is applied
        // by the game engine after rotation.
        val w = ship.angularVelocity * dt
        val r = Rotation(90f - ship.facing - w)
        val l = r.rotate(target - ship.location)
        val v = r.rotate(ship.velocity) * dt
        val vt = r.rotate(targetVelocity) * dt

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

        // Maximum velocity towards target for both axis of movement.
        // Any higher velocity would lead to overshooting target location.
        val vmx = if (d.x > 0) vMax(d.x, al) else -vMax(-d.x, al)
        val vmy = if (d.y > 0) vMax(d.y, ab) else -vMax(-d.y, af)

        // Expected velocity directly towards target location.
        val vtt = if (vmx == 0f || vmy == 0f) Vector2f(vmx, vmy)
        else d * min(vmx / d.x, vmy / d.y)

        // Expected velocity change.
        val ve = (vtt + vt).clampLength(vMax)
        val vec = clampV?.let { it(ve.rotatedReverse(r)).rotated(r) } ?: ve
        val dv = vec - v

        debugVertex(ship.location, ship.location + vec.rotatedReverse(r) / dt, Color.BLUE)

        // Proportional thrust required to achieve
        // the expected velocity change.
        val ff = +dv.y / af
        val fb = -dv.y / ab
        val fl = -dv.x / al
        val fr = +dv.x / al
        val fMax = max(max(ff, fb), max(fl, fr))

        if (shouldAccelerate(+d.y, ff, fMax)) ship.move(ACCELERATE)
        if (shouldAccelerate(-d.y, fb, fMax)) ship.move(ACCELERATE_BACKWARDS)
        if (shouldAccelerate(-d.x, fl, fMax)) ship.move(STRAFE_LEFT)
        if (shouldAccelerate(+d.x, fr, fMax)) ship.move(STRAFE_RIGHT)

        return r.reverse(vec).getFacing()
    }

    /** Set ship facing towards 'target' location. Returns the expected facing angle. */
    fun facing(target: Vector2f, targetVelocity: Vector2f): Float {
        // Change unit of time from second to
        // animation frame duration (* dt).
        val dt = Global.getCombatEngine().elapsedInLastFrame
        val a = ship.turnAcceleration * dt * dt
        val w = ship.angularVelocity * dt
        val vr = (targetVelocity - ship.velocity) * dt

        // Calculate facing and facing change.
        val reachedTarget = (target - ship.location).length() < ship.collisionRadius / 2f
        val f = if (reachedTarget) ship.facing else (target - ship.location).getFacing()
        val df = if (reachedTarget) 0f else (target + vr - ship.location).getFacing() - f

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

    /** Maximum velocity in given direction to not overshoot target. */
    fun vMax(d: Float, a: Float): Float {
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