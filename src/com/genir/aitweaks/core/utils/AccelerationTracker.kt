package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.utils.extensions.copy
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

var accelerationTracker: AccelerationTracker = AccelerationTracker()

class AccelerationTracker : BaseEveryFrameCombatPlugin() {
    private data class History(
        var previousVel: Vector2f = Vector2f(),
        val accelAverage: RollingAverageVector = RollingAverageVector(4),
        var accel: Vector2f = Vector2f(),
    )

    private val velocities: MutableMap<ShipAPI, History> = mutableMapOf()

    operator fun get(index: ShipAPI): Vector2f = velocities[index]?.accel ?: Vector2f()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        accelerationTracker = this

        Global.getCombatEngine().ships.forEach { ship ->
            val h = velocities[ship] ?: History()

            val currentAccel = (ship.velocity - h.previousVel) / dt
            h.accel = h.accelAverage.update(currentAccel)
            h.previousVel = ship.velocity.copy

            velocities[ship] = h
        }
    }
}
