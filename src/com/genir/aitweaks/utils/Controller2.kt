package com.genir.aitweaks.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.utils.extensions.strafeAcceleration
import org.lazywizard.lazylib.ext.*
import org.lwjgl.util.vector.Vector2f
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class Controller2(val ship: ShipAPI) {
    private var prevTargetLocation: Vector2f? = null

    fun heading(target: Vector2f) {
        val dt = Global.getCombatEngine().elapsedInLastFrame
        val vMax = ship.maxSpeed * dt
        val af = ship.acceleration * dt * dt
        val ab = ship.deceleration * dt * dt
        val al = ship.strafeAcceleration * dt * dt

        val r = Rotation(90f - ship.facing)
        val l = r.rotate(target - ship.location)
        val v = r.rotate(ship.velocity) * dt

        // Estimate target velocity.
        val vt = r.rotate(target - (prevTargetLocation ?: target))
        prevTargetLocation = target

        // Stop if reached an immobile target location. Distance
        // threshold for stopping is the distance the ship covers
        // in one frame after accelerating from a standstill.
        if ((target - ship.location).length() < af && vt.isZeroVector()) {
            if (!ship.velocity.isZeroVector()) ship.move(DECELERATE)
            return
        }

        // Distance to target location in next frame.
        val d = l - v + vt

        // Velocity towards target,
        // in target frame of reference.
        val vtt = Vector2f(d).resize(vMax) - vt

        // Velocity towards target, limited to
        // not overshoot the target location.
        val vtm = Vector2f(if (vtt.x > 0) min(vtt.x, vMax(d.x, al))
        else -min(-vtt.x, vMax(-d.x, al)), if (vtt.y > 0) min(vtt.y, vMax(d.y, ab))
        else -min(-vtt.y, vMax(-d.y, af)))

        // Expected velocity change, in ship frame of reference.
        val ve = (vtm + vt).clampLength(vMax)
        val dv = ve - v

        // Calculate proportional thrust required
        // to achieve the expected velocity change.
        val ff = +dv.y / af
        val fb = -dv.y / ab
        val fl = -dv.x / al
        val fr = +dv.x / al
        val fMax = max(max(ff, fb), max(fl, fr))

        if (shouldAccelerate(+d.y, ff, fMax)) ship.move(ACCELERATE)
        if (shouldAccelerate(-d.y, fb, fMax)) ship.move(ACCELERATE_BACKWARDS)
        if (shouldAccelerate(-d.x, fl, fMax)) ship.move(STRAFE_LEFT)
        if (shouldAccelerate(+d.x, fr, fMax)) ship.move(STRAFE_RIGHT)
    }

    // Maximum velocity in given direction to not overshoot target.
    private fun vMax(d: Float, a: Float): Float {
        val (q, _) = quad(0.5f, 0.5f, -d / a) ?: return 0f
        return floor(q) * a
    }

    private fun shouldAccelerate(d: Float, f: Float, m: Float) = when {
        f < 0.5f -> false
        d < 0f && f >= 0.5f -> true // braking to not overshoot target
        f >= m -> true
        else -> f / m > Random.nextFloat() // apply proportional thrust
    }

    private fun ShipAPI.move(cmd: ShipCommand) = this.giveCommand(cmd, null, 0)
}