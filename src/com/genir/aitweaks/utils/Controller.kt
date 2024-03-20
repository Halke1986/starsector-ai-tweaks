package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.utils.extensions.strafeAcceleration
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

class Controller {
    fun heading(ship: ShipAPI, target: Vector2f, targetVelocity: Vector2f, dt: Float) {
        if ((target - ship.location).length() < 1f && targetVelocity.isZeroVector()) {
            if (!ship.velocity.isZeroVector()) ship.move(DECELERATE)
            return
        }

        // Transform input into ships frame of reference.
        // Account for ship angular velocity, as linear
        // acceleration is applied by the game engine after
        // rotation.
        // Change unit of time from second to animation frame (* dt).
        val w = ship.angularVelocity * dt
        val r = Rotation(90f - ship.facing - w)
        val d = r.rotate(target - ship.location)
        val v = r.rotate(ship.velocity - targetVelocity) * dt

        val af = ship.acceleration * dt * dt
        val ab = ship.deceleration * dt * dt
        val al = ship.strafeAcceleration * dt * dt

        // Calculate expected velocity change.
        val v2 = VectorUtils.resize(Vector2f(d), ship.maxSpeed * dt)
        val e = v2 - v

        // Calculate proportional thrust required
        // to achieve the expected velocity change.
        val absAccel = listOf(e.y / af, -e.y / ab, -e.x / al, e.x / al).map { if (it > 1f) it else 0f }
        val maxAccel = absAccel.maxOrNull()!!
        val f = absAccel.map { if (maxAccel != 0f) it / maxAccel else 0f }.toMutableList()

        // Apply forward trust continuously if heading is
        // within ~1deg towards target, for visual effect.
        if (d.y > 50f * abs(d.x)) f[0] = 1f

        if (shouldAccelerate(+d.y, +v.y, f[0], af, ab)) ship.move(ACCELERATE)
        if (shouldAccelerate(-d.y, -v.y, f[1], ab, af)) ship.move(ACCELERATE_BACKWARDS)
        if (shouldAccelerate(-d.x, -v.x, f[2], al, al)) ship.move(STRAFE_LEFT)
        if (shouldAccelerate(+d.x, +v.x, f[3], al, al)) ship.move(STRAFE_RIGHT)
    }

    fun facing(ship: ShipAPI, target: Vector2f, dt: Float) {
        if ((target - ship.location).length() < 1f) return
        val tgtFacing = VectorUtils.getFacing(target - ship.location)

        val r = MathUtils.getShortestRotation(ship.facing, tgtFacing)
        val a = ship.turnAcceleration * dt * dt
        val w = ship.angularVelocity * dt

        if (shouldAccelerate(+r, +w, 1f, a, a)) ship.move(TURN_LEFT)
        if (shouldAccelerate(-r, -w, 1f, a, a)) ship.move(TURN_RIGHT)
    }

    private fun shouldAccelerate(d: Float, v: Float, f: Float, ap: Float, an: Float) = when {
        d < 0 && v < 0 && vMax(-d, -v, ap) < -v -> true // decelerate to avoid overshooting target
        d < 0 || f == 0f -> false
        v + ap > vMax(d, v, an) -> false // accelerate only if it will not cause overshooting
        f >= 1f -> true
        Random.nextFloat() < f -> true // apply proportional thrust
        else -> false
    }

    // Maximum velocity in given direction to not overshoot target.
    private fun vMax(d: Float, v: Float, a: Float): Float {
        val s = d - v  // location in next frame
        val t = sqrt(2f * s / a)
        val k = a / 2f // correction to take into account game engine discrete calculations
        return t * a - k
    }

    private fun ShipAPI.move(cmd: ShipCommand) = this.giveCommand(cmd, null, 0)
}