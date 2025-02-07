package com.genir.aitweaks.core.state

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

/** Velocity tracker tracks ship velocity based on location change. */
class VelocityTracker : BaseEveryFrameCombatPlugin() {
    private val state: MutableMap<Int, LocationState> = mutableMapOf()

    data class LocationState(val prevFrameLocation: Vector2f, val velocity: Vector2f)

    fun get(ship: ShipAPI): Vector2f {
        return state[ship.hashCode()]?.velocity ?: ship.velocity
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        if (Global.getCombatEngine().isPaused) {
            return
        }

        Global.getCombatEngine().ships.forEach { ship ->
            val locationState = state[ship.hashCode()]

            if (locationState == null) {
                val newLocationState = LocationState(ship.location.copy, ship.velocity.copy)
                state[ship.hashCode()] = newLocationState
            } else {
                locationState.velocity.set((ship.location - locationState.prevFrameLocation) / dt)
                locationState.prevFrameLocation.set(ship.location)
            }

            Debug.drawLine(ship.location, ship.location + ship.velocity, Color.BLUE)
            Debug.drawLine(ship.location, ship.location + ship.absoluteVelocity, Color.YELLOW)
        }
    }
}
