package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.debug.Line
import com.genir.aitweaks.debug.debugVertices
import com.genir.aitweaks.utils.extensions.strafeAcceleration
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

class Controller {
    fun facing(ship: ShipAPI, target: Vector2f, dt: Float) {
        if ((target - ship.location).length() < 1f) return
        val tgtFacing = VectorUtils.getFacing(target - ship.location)

        val r = MathUtils.getShortestRotation(ship.facing, tgtFacing)
        val a = ship.turnAcceleration
        ship.move(selectDir(r, 0f, ship.angularVelocity, a, a, dt), TURN_LEFT, TURN_RIGHT)
    }

    fun facing2(ship: ShipAPI, target: Vector2f, dt: Float) {
        if ((target - ship.location).length() < 1f) return
        val tgtFacing = VectorUtils.getFacing(target - ship.location)

        val r = MathUtils.getShortestRotation(ship.facing, tgtFacing)
        val a = ship.turnAcceleration * dt * dt
        val w = ship.angularVelocity * dt

        if (selectDir4(+r, +w, 1f, a, a)) ship.move2(TURN_LEFT)
        if (selectDir4(-r, -w, 1f, a, a)) ship.move2(TURN_RIGHT)
    }

    fun heading3(ship: ShipAPI, target: Vector2f, dt: Float) {
        val w = ship.angularVelocity * dt
        val r = -(ship.facing + w) + 90f
        val d = rotate(target - ship.location, r)
        val v = rotate(ship.velocity, r) * dt

        val s = ship.strafeAcceleration * dt * dt
        val a = ship.acceleration * dt * dt
        val b = ship.deceleration * dt * dt

        val v2 = d / d.length() * (ship.maxSpeed * dt)
        val e = v2 - v

        val absAccel = listOf(e.y / a, -e.y / b, e.x / s, -e.x / s)
        val maxAccel = absAccel.maxOrNull()!!
        val f = absAccel.map { it / maxAccel }

//        debugVertices.add(Line(ship.location, ship.location + rotate(e, -r) / dt, Color.YELLOW))
//        debugVertices.add(Line(ship.location, ship.location + rotate(e - v, ship.facing - 90f), Color.GREEN))

        if (d.length() >= 1f) {
            if (selectDir4(+d.y, +v.y, f[0], a, b)) ship.move2(ACCELERATE)
            if (selectDir4(-d.y, -v.y, f[1], b, a)) ship.move2(ACCELERATE_BACKWARDS)
            if (selectDir4(-d.x, -v.x, f[3], s, s)) ship.move2(STRAFE_LEFT)
            if (selectDir4(+d.x, +v.x, f[2], s, s)) ship.move2(STRAFE_RIGHT)
        } else if (!v.isZeroVector()) ship.giveCommand(DECELERATE, null, 0)
    }

    private fun selectDir4(d: Float, v: Float, f: Float, ap: Float, an: Float) = when {
        d < 0 && v < 0 && vMax(-d, -v, ap) < -v -> true // decelerate
        d < 0 -> false
        v + ap > vMax(d, v, an) -> false
        f >= 1f -> true
        Random.nextFloat() < f -> true
        else -> false
    }

    private fun vMax(d: Float, v: Float, a: Float): Float {
        val s = d - v
        val t = sqrt(2f * s / a)
        val k = a / 2f
        return t * a - k
    }

    private fun selectDir(d: Float, e: Float, v: Float, ap: Float, an: Float, dt: Float): Boolean? {
        return if (d > 0) selectDirB(d, e, v, ap, an, dt)
        else selectDirB(-d, -e, -v, an, ap, dt)?.let { !it }
    }

    private fun selectDirB(d: Float, e: Float, v: Float, a: Float, ad: Float, dt: Float): Boolean? {
        val s = d - v * dt
        val t = sqrt(2f * s / ad)
        val k = ad * dt / 2f
        val vMax = min(t * ad - k, if (e != 0f) e else 1e6f)

        return when {
            v > vMax -> false
            v + a * dt <= vMax -> true
            else -> null
        }
    }

    private fun ShipAPI.move(dir: Boolean?, positive: ShipCommand, negative: ShipCommand) {
        if (dir == null) return
        val cmd = if (dir) positive else negative

        this.move2(cmd)
    }

    private fun ShipAPI.move2(cmd: ShipCommand) {
        when (cmd) {
            ACCELERATE -> Vector2f(0f, this.acceleration)
            ACCELERATE_BACKWARDS -> Vector2f(0f, -this.deceleration)
            STRAFE_RIGHT -> Vector2f(this.strafeAcceleration, 0f)
            STRAFE_LEFT -> Vector2f(-this.strafeAcceleration, 0f)
            else -> null
        }?.let {
            val l = Line(this.location, this.location + rotate(it.scale(1f / 3f) as Vector2f, this.facing - 90f), Color.BLUE)
            debugVertices.add(l)
        }

        this.giveCommand(cmd, null, 0)
    }
}