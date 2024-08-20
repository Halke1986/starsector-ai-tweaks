package com.genir.aitweaks.core.combat.trackers

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.combat.combatState
import com.genir.aitweaks.core.debug.drawLine
import com.genir.aitweaks.core.utils.div
import com.genir.aitweaks.core.utils.extensions.copy
import com.genir.aitweaks.core.utils.extensions.isValidTarget
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.resized
import com.genir.aitweaks.core.utils.times
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

/** Track ship actual velocity. The tracked velocity is smoothed,
 * so that twitching ships do not spoof weapon tracking. */
class VelocityTracker : BaseEveryFrameCombatPlugin() {
    private data class State(var location: Vector2f, var acceleration: Float, var velocity: Vector2f)

    private var state: MutableMap<ShipAPI, State> = mutableMapOf()

    operator fun get(entity: CombatEntityAPI): Vector2f {
        return (entity as? ShipAPI).let { state[it]?.velocity } ?: entity.velocity
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val ships = Global.getCombatEngine().ships.filter { it.isValidTarget && !it.isFighter }

        ships.forEach { ship ->
            // Initialize state for a new ship.
            if (!state.contains(ship))
                state[ship] = State(ship.location.copy, ship.acceleration, ship.velocity)

            val shipState = state[ship]!!

            // Update tracked location if acceleration changed.
            val acceleration = ship.acceleration * ship.mutableStats.timeMult.modifiedValue
            if (acceleration != shipState.acceleration) {
                val scale = acceleration / shipState.acceleration
                shipState.location = ship.location + (shipState.location - ship.location) * scale
                shipState.acceleration = acceleration
            }

            // Tracked location is free to move around the ship location.
            // The freedom is within a radius based on ship acceleration,
            // adjusted for ship time flow.
            val r = shipState.acceleration / 5f
            val p = shipState.location - ship.location
            val newLocation = ship.location + if (p.length > r) p.resized(r) else p

            shipState.velocity = (newLocation - shipState.location) / dt
            shipState.location = newLocation

            drawLine(ship.location, ship.location + shipState.velocity, Color.GREEN)
            drawLine(ship.location, ship.location + ship.velocity, Color.BLUE)
        }
    }

    companion object {
        val CombatEntityAPI.smoothedVelocity: Vector2f
            get() = combatState().velocityTracker[this]
    }
}
