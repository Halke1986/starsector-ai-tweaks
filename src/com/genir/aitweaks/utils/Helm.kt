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

    fun heading2(ship: ShipAPI, target: Vector2f, dt: Float) {
        if ((target - ship.location).length() < 1f) {
            if (!ship.velocity.isZeroVector()) ship.giveCommand(DECELERATE, null, 0)
            return
        }


        val r = -ship.facing + 90f
        val d = rotate(target - ship.location, r)
        val v = rotate(ship.velocity, r)

        var v2 = d / d.length() * ship.maxSpeed
//
//
//
//        debugPlugin[4] = if (v.y > vMaxa) (vMaxa / v.y) else ""
//        if (v.y > vMaxa) v2 *= (vMaxa / v.y)
//
//        debugPlugin[5] = if (-v.y > vMaxb) (vMaxb / -v.y) else ""
//        if (-v.y > vMaxb) v2 *= (vMaxb / -v.y)
//
//        debugPlugin[6] = if (-v.x > vMaxl) (vMaxl / -v.x) else ""
//        if (-v.x > vMaxl) v2 *= (vMaxl / -v.x)
//
//        debugPlugin[7] = if (v.x > vMaxr) (vMaxr / v.x) else ""
//        if (v.x > vMaxr) v2 *= (vMaxr / v.x)


        val e = v2 - v
//        debugVertices.add(Line(ship.location, ship.location + rotate(v2, -r), Color.YELLOW))
//        debugVertices.add(Line(ship.location, ship.location + rotate(e, -r), Color.CYAN))


        val s = ship.strafeAcceleration * dt
        val a = ship.acceleration * dt
        val b = ship.deceleration * dt

        val absAccel = listOf(e.y / a, -e.y / b, -e.x / s, e.x / s).map { if (it < 1f) 0f else it }
        val maxAccel = absAccel.maxOrNull()!!
        val accel = absAccel.map { it / maxAccel }

//        debugPlugin[0] = "a ${shouldDecel(d.y, v.y, ship.acceleration, dt)} ${vMax(d.y, v.y, ship.acceleration, dt)} ${d.y} ${v.y}"
//        debugPlugin[1] = "d ${shouldDecel(-d.y, -v.y, ship.deceleration, dt)} ${vMax(-d.y, -v.y, ship.deceleration, dt)}  ${-d.y} ${-v.y}"
//        debugPlugin[2] = "l ${shouldDecel(-d.x, -v.x, ship.strafeAcceleration, dt)} ${vMax(-d.x, -v.x, ship.strafeAcceleration, dt)}  ${-d.x} ${-v.x}"
//        debugPlugin[3] = "r ${shouldDecel(d.x, v.x, ship.strafeAcceleration, dt)} ${vMax(d.x, v.x, ship.strafeAcceleration, dt)} ${d.x} ${v.x}"

        debugPlugin[0] = "a ${accel[0]}"
        debugPlugin[1] = "d ${accel[1]}"
        debugPlugin[2] = "l ${accel[2]}"
        debugPlugin[3] = "r ${accel[3]}"

        val da = shouldDecel(d.y, v.y, ship.deceleration, dt)
        val db = shouldDecel(-d.y, -v.y, ship.acceleration, dt)
        val dl = shouldDecel(-d.x, -v.x, ship.strafeAcceleration, dt)
        val dr = shouldDecel(d.x, v.x, ship.strafeAcceleration, dt)
//
        if (!da && (db || Random.nextFloat() < accel[0])) ship.move(true, ACCELERATE, DECELERATE)
        if (!db && (da || Random.nextFloat() < accel[1])) ship.move(true, ACCELERATE_BACKWARDS, DECELERATE)
        if (!dl && (dr || Random.nextFloat() < accel[2])) ship.move(true, STRAFE_LEFT, DECELERATE)
        if (!dr && (dl || Random.nextFloat() < accel[3])) ship.move(true, STRAFE_RIGHT, DECELERATE)
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
        return if (d > 0) selectDir2(d, e, v, ap, an, dt)
        else selectDir2(-d, -e, -v, an, ap, dt)?.let { !it }
    }

    private fun selectDir2(d: Float, e: Float, v: Float, a: Float, ad: Float, dt: Float): Boolean? {
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