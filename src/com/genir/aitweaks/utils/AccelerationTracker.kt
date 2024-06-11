package com.genir.aitweaks.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.debug.debugPlugin
import com.genir.aitweaks.utils.extensions.copy
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plusAssign
import org.lwjgl.util.vector.Vector2f

var accelerationTracker: AccelerationTracker = AccelerationTracker()

class AccelerationTracker : BaseEveryFrameCombatPlugin() {
    private data class History(
        var previousVel: Vector2f,
        var previousAccel0: Vector2f,
        var previousAccel1: Vector2f,
        var previousAccel2: Vector2f,
        var previousAccel3: Vector2f,
        var accel: Vector2f,
    )

    private val velocities: MutableMap<ShipAPI, History> = mutableMapOf()

    operator fun get(index: ShipAPI): Vector2f = velocities[index]?.accel ?: Vector2f()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        accelerationTracker = this

        Global.getCombatEngine().ships.forEach { ship ->
            val h = velocities[ship] ?: History(Vector2f(), Vector2f(), Vector2f(), Vector2f(), Vector2f(), Vector2f())

            val currentAccel = (ship.velocity - h.previousVel) / (dt * 4f)

            h.accel += currentAccel
            h.accel -= h.previousAccel3

            h.previousAccel3 = h.previousAccel2
            h.previousAccel2 = h.previousAccel1
            h.previousAccel1 = h.previousAccel0
            h.previousAccel0 = currentAccel

            h.previousVel = ship.velocity.copy

            velocities[ship] = h
        }
    }
}
//
//class AccelerationTracker : BaseEveryFrameCombatPlugin() {
//    private data class Velocity(var previous: Vector2f, var accel: Vector2f)
//
//    private val velocities: MutableMap<ShipAPI, Velocity> = mutableMapOf()
//
//    operator fun get(index: ShipAPI): Vector2f = velocities[index]?.accel ?: Vector2f()
//
//    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
//        accelerationTracker = this
//
//        Global.getCombatEngine().ships.forEach { ship ->
//            val state = velocities[ship] ?: Velocity(Vector2f(), Vector2f())
//
//            state.accel = (ship.velocity - state.previous) / dt
//            state.previous = ship.velocity.copy
//
//            velocities[ship] = state
//        }
//    }
//}
