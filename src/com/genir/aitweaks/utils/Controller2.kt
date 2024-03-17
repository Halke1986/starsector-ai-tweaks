package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.debug.debugPlugin
import com.genir.aitweaks.utils.extensions.strafeAcceleration
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

class Controller2 {
    fun heading(ship: ShipAPI, target: Vector2f, dt: Float) {
        val r = VectorUtils.getFacing(target - ship.location) - 90f
        debugPlugin[0] = r

//        val v = rotate(ship.velocity * dt, -r)
//        debugPlugin[1] = v

        val af = ship.acceleration * dt * dt
        val ab = ship.deceleration * dt * dt
        val al = ship.strafeAcceleration * dt * dt

        debugPlugin[2] = accelInDir(r, af, ab, al)

    }

    // Angle in ship coordinates, 0f is forward.
    private fun accelInDir(angle: Float, af: Float, ab: Float, al: Float): Float {
        val sin = sin(-angle)
        val cos = cos(-angle)

        val f = (+af * cos).coerceAtLeast(0f)
        val b = (-ab * cos).coerceAtLeast(0f)
        val l = (-al * sin).coerceAtLeast(0f)
        val r = (+al * sin).coerceAtLeast(0f)

        return f + b + l + r
    }
}