package com.genir.aitweaks.utils

import com.fs.starfarer.api.Global
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
import kotlin.math.floor


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

var vPrev = Vector2f(0f, 0f)

var expd = 0f

fun setHeading(ship: ShipAPI, target: Vector2f, dtUnused: Float) {
    val dt = debugPlugin.dtTracker.dt()

    val d = rotate(target - ship.location, -ship.facing + 90f)
    val e = d.normalise(null) * ship.maxSpeed
    val v = rotate(ship.velocity, -ship.facing + 90f)

    val a = ship.strafeAcceleration
//    expd = abs(((a / 60) * (abs(v.x) / a)) + (v.x * v.x) / (a * 2f))
//    expd = abs((abs(v.x) / 30f) + (v.x * v.x) / (a * 2f))

//    val brk = if ((v.x * v.x) / (a * 2f) > abs(d.x)) "brk" else "  "


    val dv = a * dt
    val f = floor(abs(v.x) / dv)

    val base = (f + 1) * (f + 1) - 1
    val rem = ((abs(v.x) / dv) - f) * f
    val brkDist = (base + rem) * a * dt * dt * 0.5f

    expd = brkDist + abs(v.x) * dt

    debugPlugin[0] = "v ${v.x}"
//    debugPlugin[1] = "f $f"
    debugPlugin[2] = "b $brkDist"
    debugPlugin[3] = "d ${d.x}"
    debugPlugin[4] = "dt ${debugPlugin.dtTracker.dt()}"


    if (!Global.getCombatEngine().isPaused) {
        Global.getLogger(Helm::class.java).info("${v.x} ${d.x} $brkDist $dt")
    }


//    debugPlugin[3] = "a ${ship.strafeAcceleration / 60}"
//    debugPlugin[4] = "s ${ship.velocity.length()}"

    if (abs(d.x) < 1f && !v.isZeroVector()) {
        giveCommand(ship, DECELERATE)
        return
    }

    if (abs(d.x) < 1f && v.isZeroVector()) {
        return
    }

//    setHeading2(ship, d.y, e.y, v.y, ship.acceleration, ship.deceleration, ACCELERATE, ACCELERATE_BACKWARDS)
//    setHeading2(ship, d.x, e.x, v.x, ship.strafeAcceleration, ship.strafeAcceleration, STRAFE_RIGHT, STRAFE_LEFT)
    setHeading2(ship, d.x, e.x, v.x, ship.strafeAcceleration, ship.strafeAcceleration, STRAFE_RIGHT, STRAFE_LEFT)

    vPrev = v
}

fun setHeading2(ship: ShipAPI, d: Float, e: Float, v: Float, ap: Float, an: Float, positive: ShipCommand, negative: ShipCommand) {
    val cmd = if (d > 0) setHeading3(d, e, v, an, positive, negative)
    else setHeading3(-d, -e, -v, ap, negative, positive)

    cmd?.let { giveCommand(ship, it) }
}

fun setHeading3(d: Float, e: Float, v: Float, a: Float, accel: ShipCommand, decel: ShipCommand) = when {
    v < 0 -> accel                  // Is heading away from target
//    (v * v) / (a * 2f) > d -> decel     // Will overshot target
    expd > d -> decel     // Will overshot target
    d < 0.75f -> null               // Is already on target
//    v > e -> decel
    else -> accel                   // Head towards target
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
//    debugPlugin[cmd] = cmd
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














