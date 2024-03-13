package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.Line
import com.genir.aitweaks.debugPlugin
import com.genir.aitweaks.debugVertices
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

class Controller {
    fun facing(ship: ShipAPI, target: Vector2f, dt: Float) {
        if ((target - ship.location).length() < 1f) return
        val tgtFacing = VectorUtils.getFacing(target - ship.location)

        val r = MathUtils.getShortestRotation(ship.facing, tgtFacing)
        val a = ship.turnAcceleration
        ship.move(selectDir(r, 0f, ship.angularVelocity, a, a, dt), TURN_LEFT, TURN_RIGHT)
    }

    fun heading(ship: ShipAPI, target: Vector2f, dt: Float) {
        val d = rotate(target - ship.location, -ship.facing + 90f)
        val e = d.normalise(null) * ship.maxSpeed
        val v = rotate(ship.velocity, -ship.facing + 90f)

        debugVertices.add(Line(ship.location, ship.location + rotate(e, ship.facing - 90f), Color.YELLOW))
        debugVertices.add(Line(ship.location, ship.location + rotate(e-v, ship.facing - 90f), Color.GREEN))

        debugPlugin[0] = e.x
        debugPlugin[1] = e.y
        debugPlugin[2] = v.x
        debugPlugin[3] = v.y

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