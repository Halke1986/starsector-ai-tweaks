package com.genir.aitweaks.core

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.CARRIER
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.COMBAT
import com.genir.aitweaks.core.extensions.isBig
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.state.State.Companion.state

class CustomAIManager {
    fun getAIForShip(ship: ShipAPI): ShipAIPlugin? {
        return when {
            Global.getCurrentState() != GameState.COMBAT -> null

            shouldHaveCustomAI(ship) -> CustomShipAI(ship)
            shouldHaveWrapperAI(ship) -> WrapperShipAI(ship)

            else -> null
        }
    }

    /** Returns true is custom AI can control the given ship. */
    fun canHaveCustomAI(ship: ShipAPI): Boolean {
        return when {
            ship.hullSpec.isPhase -> false
            ship.hullSpec.hints.contains(CARRIER) && !ship.hullSpec.hints.contains(COMBAT) -> false
            ship.isStation -> false
            !ship.isBig -> false

            else -> true
        }
    }

    private fun shouldHaveWrapperAI(ship: ShipAPI): Boolean {
        // Install wrapper AI in frigate ships, but not station modules.
        // Check for number of engines, as isModule method may return
        // incorrect result when the ship is being initialized.
        return ship.engineController.shipEngines.isNotEmpty() && ship.isFrigate
    }

    private fun shouldHaveCustomAI(ship: ShipAPI): Boolean {
        return when {
            !canHaveCustomAI(ship) -> false

            // Custom AI Hullmod takes priority.
            ship.variant.hasHullMod("aitweaks_custom_ship_ai") -> true

            // Non-player ships can have custom AI by default, without the hullmod.
            ship.owner == 1 || ship.isAlly -> when {
                ship.hullSpec.hullId.startsWith("guardian") -> true
                ship.hullSpec.hullId.startsWith("sr_melvillei") -> true
                ship.hullSpec.shipSystemId == "lidararray" -> true

                // Simulator in devmode.
                Global.getCombatEngine().isSimulation && state.config.devMode -> true

                else -> false
            }

            else -> false
        }
    }
}
