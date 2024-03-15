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

    fun heading3(ship: ShipAPI, target: Vector2f, dt: Float) {
        val r = -ship.facing + 90f
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
            ship.move(selectDir3(d.y, v.y, f[0], f[1], a, b), ACCELERATE, ACCELERATE_BACKWARDS)
            ship.move(selectDir3(d.x, v.x, f[2], f[3], s, s), STRAFE_RIGHT, STRAFE_LEFT)
        } else if (!v.isZeroVector()) ship.giveCommand(DECELERATE, null, 0)
    }

    private fun selectDir3(d: Float, v: Float, fp: Float, fn: Float, ap: Float, an: Float): Boolean? {
        return if (d > 0) selectDir3B(d, v, fp, ap, an)
        else selectDir3B(-d, -v, fn, an, ap)?.let { !it }
    }

    private fun selectDir3B(d: Float, v: Float, f: Float, ap: Float, an: Float): Boolean? {
        val s = d - v
        val t = sqrt(2f * s / an)
        val k = an / 2f
        val vMax = t * an - k

        return when {
            v > vMax -> false
            v + ap > vMax -> null
            f >= 1f -> true
            Random.nextFloat() < f -> true
            else -> null
        }
    }

    private fun vMax(d: Float, v: Float, a: Float, dt: Float): Float {
        val s = d - v * dt
        val t = sqrt(2f * s / a)
        val k = a * dt / 2f
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