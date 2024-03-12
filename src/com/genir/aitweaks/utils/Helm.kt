package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.debugPlugin
import com.genir.aitweaks.utils.extensions.strafeAcceleration
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.min
import kotlin.math.sqrt

fun setFacing(ship: ShipAPI, target: Vector2f) {
    val tgtFacing = VectorUtils.getFacing(target - ship.location)
    val d = MathUtils.getShortestRotation(ship.facing, tgtFacing)
    val a = ship.turnAcceleration

    setHeading2(ship, d, 0f, ship.angularVelocity, a, a, TURN_LEFT, TURN_RIGHT)
}

fun setHeading(ship: ShipAPI, target: Vector2f) {
    val d = rotate(target - ship.location, -ship.facing + 90f)
    val e = d.normalise(null) * ship.maxSpeed
    val v = rotate(ship.velocity, -ship.facing + 90f)

    if (d.length() >= 1f) {
        setHeading2(ship, d.y, e.y, v.y, ship.acceleration, ship.deceleration, ACCELERATE, ACCELERATE_BACKWARDS)
        setHeading2(ship, d.x, e.x, v.x, ship.strafeAcceleration, ship.strafeAcceleration, STRAFE_RIGHT, STRAFE_LEFT)
    } else if (!v.isZeroVector())
        giveCommand(ship, DECELERATE)
}

fun setHeading2(ship: ShipAPI, d: Float, e: Float, v: Float, ap: Float, an: Float, positive: ShipCommand, negative: ShipCommand) {
    if (d > 0) setHeading3(ship, d, e, v, ap, an, positive, negative)
    else setHeading3(ship, -d, -e, -v, an, ap, negative, positive)
}

fun setHeading3(ship: ShipAPI, d: Float, e: Float, v: Float, a: Float, ad: Float, accel: ShipCommand, decel: ShipCommand) {
    val dt = debugPlugin.dtTracker.dt()
    val s = d - v * dt
    val t = sqrt(2f * s / ad)
    val k = ad * dt / 2f
    val vMax = min(t * ad - k, if (e != 0f) e else 1e6f)

    when {
        v > vMax -> decel
        v + a * dt <= vMax -> accel
        else -> null
    }?.let { giveCommand(ship, it) }
}

fun ShipAPI.prepCmd(cmd: ShipCommand): (Unit) -> Unit = {
    debugPlugin[cmd] = cmd
    this.giveCommand(cmd, null, 0)
}

fun giveCommand(ship: ShipAPI, cmd: ShipCommand) {
    debugPlugin[cmd] = cmd
    ship.giveCommand(cmd, null, 0)
}

class DtTracker(val span: Float) {
    var tSum = 0f
    val dts: MutableList<Float> = mutableListOf()

    fun advance(dt: Float) {
        dts.add(dt)
        tSum += dt

        while (dts.isNotEmpty() && tSum > span) {
            tSum -= dts.removeFirst()
        }
    }

    fun dt() = tSum / dts.count()
}