package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.debugPlugin
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

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

//fun setHeading(ship: ShipAPI, target: Vector2f) {
//    val toTarget = VectorUtils.getDirectionalVector(ship.location, target)
//    val expectedVelocity = toTarget * ship.maxSpeed
//    val dv = expectedVelocity - ship.velocity
//
//
//    val dvLocal = rotate(dv, -ship.facing + 90f)
//
////    debugPlugin[0] = "ex $expectedVelocity"
////    debugPlugin[1] = "vs ${ship.velocity}"
//
//    debugPlugin[3] = "dv $dv"
//    debugPlugin[4] = "dl $dvLocal"
//
//    giveCommand(ship, if (dvLocal.x > 0) STRAFE_RIGHT else STRAFE_LEFT)
//    giveCommand(ship, if (dvLocal.y > 0) ACCELERATE else ACCELERATE_BACKWARDS)
//}

fun setHeading(ship: ShipAPI, target: Vector2f) {
    val d = rotate(target - ship.location, -ship.facing + 90f)
    val e = d.normalise(null) * ship.maxSpeed
    val v = rotate(ship.velocity, -ship.facing + 90f)

    setHeading2y(ship, d.y, e.y, v.y, ship.acceleration, ship.deceleration, ACCELERATE, ACCELERATE_BACKWARDS)
    setHeading2x(ship, d.x, e.x, v.x, ship.strafeAcceleration, ship.strafeAcceleration, STRAFE_RIGHT, STRAFE_LEFT)

//    setHeading2(-d.y, -e.y, -v.y, ship.deceleration, ACCELERATE_BACKWARDS)
//    setHeading2(d.x, e.x, v.x, ship.strafeAcceleration, STRAFE_RIGHT)
//    setHeading2(-d.x, -e.x, -v.x, ship.strafeAcceleration, STRAFE_LEFT)
}

fun setHeading2x(ship: ShipAPI, d: Float, e: Float, v: Float, ap: Float, an: Float, positive: ShipCommand, negative: ShipCommand) {
    debugPlugin[1] = "$d $e $v"

    val cmd = if (d > 0) setHeading3x(d, e, v, ap, positive, negative)
    else setHeading3x(-d, -e, -v, an, negative, positive)

    cmd?.let { giveCommand(ship, it) }
}

fun setHeading3x(d: Float, e: Float, v: Float, a: Float, accel: ShipCommand, decel: ShipCommand) = when {
    v < 0 -> accel                  // Is heading away from target
    (v * v) / (a * 2f) > d -> { debugPlugin[0] = "decel"; decel} // Will overshot target
    d < 0.75f -> null               // Is already on target
    v > e -> { debugPlugin[0] = "decel"; decel}
    else -> { debugPlugin[0] = "accel"; accel}                   // Head towards target
}

fun setHeading2y(ship: ShipAPI, d: Float, e: Float, v: Float, ap: Float, an: Float, positive: ShipCommand, negative: ShipCommand) {
    val cmd = if (d > 0) setHeading3y(d, e, v, ap, positive, negative)
    else setHeading3y(-d, -e, -v, an, negative, positive)

    cmd?.let { giveCommand(ship, it) }
}

fun setHeading3y(d: Float, e: Float, v: Float, a: Float, accel: ShipCommand, decel: ShipCommand) = when {
    v < 0 -> accel                  // Is heading away from target
    (v * v) / (a * 2f) > d -> decel // Will overshot target
    d < 0.75f -> null               // Is already on target
    v > e -> decel
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
    debugPlugin[cmd] = cmd
    ship.giveCommand(cmd, null, 0)
}