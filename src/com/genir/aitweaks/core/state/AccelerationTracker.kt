package com.genir.aitweaks.core.state

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.copy
import com.genir.aitweaks.core.extensions.minus
import com.genir.aitweaks.core.utils.RollingAverageVector
import com.genir.aitweaks.core.utils.div
import org.lwjgl.util.vector.Vector2f

class AccelerationTracker : BaseEveryFrameCombatPlugin() {
    private data class History(
        var previousVel: Vector2f = Vector2f(),
        val accelAverage: RollingAverageVector = RollingAverageVector(4),
        var accel: Vector2f = Vector2f(),
    )

    private val velocities: MutableMap<ShipAPI, History> = mutableMapOf()

    operator fun get(index: ShipAPI): Vector2f = velocities[index]?.accel ?: Vector2f()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        Global.getCombatEngine().ships.forEach { ship ->
            val h = velocities[ship] ?: History()

            val currentAccel = (ship.velocity - h.previousVel) / dt
            h.accel = h.accelAverage.update(currentAccel)
            h.previousVel = ship.velocity.copy

            velocities[ship] = h
        }
    }
}
