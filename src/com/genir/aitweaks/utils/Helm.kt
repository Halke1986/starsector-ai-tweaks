package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.debug.Line
import com.genir.aitweaks.debug.debugPlugin
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

        val v2 = d / d.length() * (ship.maxSpeed * dt)
        val e = v2 - v

        val s = ship.strafeAcceleration * dt * dt
        val a = ship.acceleration * dt * dt
        val b = ship.deceleration * dt * dt

        val absAccel = listOf(e.y / a, -e.y / b, e.x / s, -e.x / s)
        val maxAccel = absAccel.maxOrNull()!!
        val f = absAccel.map { it / maxAccel }

//        debugPlugin[0] = v
//        debugPlugin[1] = e

//        debugVertices.add(Line(ship.location, ship.location + rotate(e, -r) / dt, Color.YELLOW))
//        debugVertices.add(Line(ship.location, ship.location + rotate(e - v, ship.facing - 90f), Color.GREEN))

        if (d.length() >= 1f) {

            ship.move(selectDir3(d.y, e.y, v.y, f[0], f[1], a, b), ACCELERATE, ACCELERATE_BACKWARDS)
            ship.move(selectDir3(d.x, e.x, v.x, f[2], f[3], s, s), STRAFE_RIGHT, STRAFE_LEFT)
        } else if (!v.isZeroVector()) ship.giveCommand(DECELERATE, null, 0)
    }

    private fun selectDir3(d: Float, e: Float, v: Float, fp: Float, fn: Float, ap: Float, an: Float): Boolean? {
        return if (d > 0) selectDir3B(d, e, v, fp, ap, an)
        else selectDir3B(-d, -e, -v, fn, an, ap)?.let { !it }
    }

    private fun selectDir3B(d: Float, e: Float, v: Float, f: Float, ap: Float, an: Float): Boolean? {
        val s = d - v
        val t = sqrt(2f * s / an)
        val k = an / 2f
        val vMax = t * an - k

//        debugPlugin[4] = "s $s"
//        debugPlugin[1] = "ap $ap"
//        debugPlugin[2] = "v $v"
//        debugPlugin[3] = "e $e"

//        debugPlugin[4] = "s $s"
//        debugPlugin[5] = "t $t"
//        debugPlugin[6] = "k $k"
//        debugPlugin[7] = "m $vMax"

//        return when {
//            v > vMax -> false
//            v + ap <= vMax && e > ap -> true
//            else -> null
//        }

//        debugPlugin[0] = f

        return when {
            v > vMax -> false
            v + ap > vMax -> null
//            f <= 0f -> null
            f >= 1f -> true
            Random.nextFloat() < f -> true
            else -> null
        }
    }

    fun heading2(ship: ShipAPI, target: Vector2f, dt: Float) {
        if ((target - ship.location).length() < 1f) {
            if (!ship.velocity.isZeroVector()) ship.giveCommand(DECELERATE, null, 0)
            return
        }


        val r = -ship.facing + 90f
        val d = rotate(target - ship.location, r)
        val v = rotate(ship.velocity, r)

        var v2 = d / d.length() * ship.maxSpeed

        val e = v2 - v

        val s = ship.strafeAcceleration * dt
        val a = ship.acceleration * dt
        val b = ship.deceleration * dt

        val absAccel = listOf(e.y / a, -e.y / b, -e.x / s, e.x / s).map { if (it < 1f) 0f else it }
        val maxAccel = absAccel.maxOrNull()!!
        val accel = absAccel.map { it / maxAccel }

        val da = shouldDecel(d.y, v.y, ship.deceleration, dt)
        val db = shouldDecel(-d.y, -v.y, ship.acceleration, dt)
        val dl = shouldDecel(-d.x, -v.x, ship.strafeAcceleration, dt)
        val dr = shouldDecel(d.x, v.x, ship.strafeAcceleration, dt)
//
        if (!da && (db || 0 < accel[0])) ship.move(true, ACCELERATE, DECELERATE)
        if (!db && (da || 0 < accel[1])) ship.move(true, ACCELERATE_BACKWARDS, DECELERATE)
        if (!dl && (dr || 0 < accel[2])) ship.move(true, STRAFE_LEFT, DECELERATE)
        if (!dr && (dl || 0 < accel[3])) ship.move(true, STRAFE_RIGHT, DECELERATE)
    }

    private fun shouldDecel(d: Float, v: Float, a: Float, dt: Float) = vMax(d, v, a, dt) < v

    private fun vMax(d: Float, v: Float, a: Float, dt: Float): Float {
        val s = d - v * dt
        val t = sqrt(2f * s / a)
        val k = a * dt / 2f
        return t * a - k
    }

    fun heading(ship: ShipAPI, target: Vector2f, dt: Float) {
        val d = rotate(target - ship.location, -ship.facing + 90f)
        val e = d.normalise(null) * ship.maxSpeed
        val v = rotate(ship.velocity, -ship.facing + 90f)

        debugVertices.add(Line(ship.location, ship.location + rotate(e, ship.facing - 90f), Color.YELLOW))
        debugVertices.add(Line(ship.location, ship.location + rotate(e - v, ship.facing - 90f), Color.GREEN))

        debugPlugin[0] = ship.acceleration
        debugPlugin[1] = ship.deceleration
        debugPlugin[2] = ship.strafeAcceleration

        if (d.length() >= 1f) {
            val s = ship.strafeAcceleration
            val a = ship.acceleration
            val b = ship.deceleration
            ship.move(selectDir(d.y, e.y, v.y, a, b, dt), ACCELERATE, ACCELERATE_BACKWARDS)
            ship.move(selectDir(d.x, e.x, v.x, s, s, dt), STRAFE_RIGHT, STRAFE_LEFT)
        } else if (!v.isZeroVector()) ship.giveCommand(DECELERATE, null, 0)
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