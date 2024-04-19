package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.debug.drawEngineLines
import com.genir.aitweaks.utils.extensions.strafeAcceleration
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.resize
import org.lwjgl.util.vector.Vector2f
import kotlin.math.floor
import kotlin.math.min

class Controller2(val ship: ShipAPI) {
    fun heading(target: Vector2f, targetVelocity: Vector2f, dt: Float) {
//        if ((target - ship.location).length() < 1f ) {
//            if (!ship.velocity.isZeroVector()) ship.move(DECELERATE)
//            return
//        }

        val r = Rotation(90f - ship.facing)
        val d = r.rotate(target - ship.location)
        val v = r.rotate(ship.velocity) * dt
        val vd = r.rotate(targetVelocity) * dt

        val af = ship.acceleration * dt * dt
        val ab = ship.deceleration * dt * dt
        val al = ship.strafeAcceleration * dt * dt

        var ve = Vector2f(d).resize(ship.maxSpeed * dt)

        val d2 = d - v + vd

        ve.x = if (ve.x > 0) min(ve.x, vMax(d2.x, al))
        else -min(-ve.x, vMax(-d2.x, al))

        ve.x = when {
            ve.x > 0 -> min(ve.x, vMax(d2.x, al))
            else -> -min(-ve.x, vMax(-d2.x, al))
        }

        ve.y = when {
            ve.y > 0 -> min(ve.y, vMax(d2.y, ab))
            else -> -min(-ve.y, vMax(-d2.y, af))
        }

        ve = (ve + vd)

        val dv = ve - v

        if (shouldAccelerate(+dv.y, af)) ship.move(ACCELERATE)
        if (shouldAccelerate(-dv.y, ab)) ship.move(ACCELERATE_BACKWARDS)
        if (shouldAccelerate(-dv.x, al)) ship.move(STRAFE_LEFT)
        if (shouldAccelerate(+dv.x, al)) ship.move(STRAFE_RIGHT)

        drawEngineLines(ship)
    }


    private fun shouldAccelerate(dv: Float, a: Float) = when {
        dv / a > 0.5 -> true
//        dv > 0 -> true
        else -> false
    }


    // Maximum velocity in given direction to not overshoot target.
    private fun vMax(d: Float, a: Float): Float {
        val (q, _) = quad(0.5f, 0.5f, -d / a) ?: return 0f
        return floor(q) * a
    }


//    private fun vMax2(d: Float, a: Float): Float {
//        var u = floor(d / a)
//        var i = 0
//
//        while (u >= i) {
//            i++
//            u -= i
//        }
//
//        return a * i
//    }


    private fun ShipAPI.move(cmd: ShipCommand) = this.giveCommand(cmd, null, 0)
}