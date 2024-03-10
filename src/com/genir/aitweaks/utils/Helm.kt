package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
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

    val cmd = if (d > 0) setFacing2(d, v, a, ShipCommand.TURN_LEFT, ShipCommand.TURN_RIGHT)
    else setFacing2(-d, -v, a, ShipCommand.TURN_RIGHT, ShipCommand.TURN_LEFT)

    cmd?.let { giveCommand(ship, it) }
}

fun setFacing2(d: Float, v: Float, a: Float, accel: ShipCommand, decel: ShipCommand) = when {
    v < 0 -> accel                  // Is turning away from target
    (v * v) / (a * 2f) > d -> decel // Will overshot target
    d < 0.75f -> null               // Is already on target
    else -> accel                   // Turn towards target
}

fun setHeading(ship: ShipAPI, target: Vector2f) {
    val toTarget = VectorUtils.getDirectionalVector(ship.location, target)
    val expectedVelocity = toTarget * ship.maxSpeed
    val dv = expectedVelocity - ship.velocity


    val dvLocal = rotate(dv, -ship.facing + 90f)

//    debugPlugin[0] = "ex $expectedVelocity"
//    debugPlugin[1] = "vs ${ship.velocity}"

    debugPlugin[3] = "dv $dv"
    debugPlugin[4] = "dl $dvLocal"

    giveCommand(ship, if (dvLocal.x > 0) ShipCommand.STRAFE_RIGHT else ShipCommand.STRAFE_LEFT)
    giveCommand(ship, if (dvLocal.y > 0) ShipCommand.ACCELERATE else ShipCommand.ACCELERATE_BACKWARDS)
}

fun giveCommand(ship: ShipAPI, cmd: ShipCommand) {
    debugPlugin[cmd] = cmd
    ship.giveCommand(cmd, null, 0)
}