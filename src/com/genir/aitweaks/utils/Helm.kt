package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.debugPlugin
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

fun setFacing(ship: ShipAPI, target: Vector2f) {
    val tgtFacing = VectorUtils.getFacing(target - ship.location)
    val d = MathUtils.getShortestRotation(ship.facing, tgtFacing)
    val v = ship.angularVelocity
    val a = ship.turnAcceleration

    val cmd = if (d > 0) setFacing2(d, v, a, TURN_LEFT, TURN_RIGHT)
    else setFacing2(-d, -v, a, TURN_RIGHT, TURN_LEFT)

    cmd?.let { giveCommand(ship, it) }
}

fun setFacing2(d: Float, v: Float, a: Float, accel: ShipCommand, decel: ShipCommand) = when {
    v < 0 -> accel                  // Is turning away from target
    (v * v) / (a * 2f) > d -> decel // Will overshot target
    d < 0.75f -> null               // Is already on target
    else -> accel                   // Turn towards target
}

fun setHeading(ship: ShipAPI, target: Vector2f, dtUnused: Float) {
    val d = rotate(target - ship.location, -ship.facing + 90f)
    val e = d.normalise(null) * ship.maxSpeed
    val v = rotate(ship.velocity, -ship.facing + 90f)

    debugPlugin[0] = ship.velocity.length()

    if (abs(d.x) < 1f && !v.isZeroVector()) {
        giveCommand(ship, DECELERATE)
        return
    }

    if (abs(d.x) < 1f && v.isZeroVector()) {
        return
    }

    setHeading2(ship, d.y, e.y, v.y, ship.acceleration, ship.deceleration, ACCELERATE, ACCELERATE_BACKWARDS)
    setHeading2(ship, d.x, e.x, v.x, ship.strafeAcceleration, ship.strafeAcceleration, STRAFE_RIGHT, STRAFE_LEFT)
}

fun setHeading2(ship: ShipAPI, d: Float, e: Float, v: Float, ap: Float, an: Float, positive: ShipCommand, negative: ShipCommand) {
    val cmd = if (d > 0) setHeading3(d, e, v, ap, an, positive, negative)
    else setHeading3(-d, -e, -v, an, ap, negative, positive)

    cmd?.let { giveCommand(ship, it) }
}

fun setHeading3(d: Float, e: Float, v: Float, a: Float, ad: Float, accel: ShipCommand, decel: ShipCommand): ShipCommand? {
    val dt = debugPlugin.dtTracker.dt()
    val s = d - v * dt
    val t = sqrt(2f * s / ad)
    val k = ad * dt / 2f
    val vMax = min(t * ad - k, e)

    return when {
        v < 0 -> accel                  // Is heading away from target
        v > vMax -> decel
        d < 0.75f -> null               // Is already on target
        v + a * dt <= vMax -> accel
        else -> null                   // Head towards target
    }
}

val ShipAPI.strafeAcceleration: Float
    get() = this.acceleration * when (this.hullSize) {
        HullSize.FIGHTER -> 0.75f
        HullSize.FRIGATE -> 1.0f
        HullSize.DESTROYER -> 0.75f
        HullSize.CRUISER -> 0.5f
        HullSize.CAPITAL_SHIP -> 0.25f
        else -> 1.0f
    }


fun giveCommand(ship: ShipAPI, cmd: ShipCommand) {
    debugPlugin[cmd] = cmd
    ship.giveCommand(cmd, null, 0)
}

class Helm {}

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