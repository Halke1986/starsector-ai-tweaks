package com.genir.aitweaks.core.features

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAssignmentType.RETREAT
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.CARRIER
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.COMBAT
import com.genir.aitweaks.core.extensions.assignment
import com.genir.aitweaks.core.extensions.isBig
import com.genir.aitweaks.core.features.shipai.CustomShipAI
import com.genir.aitweaks.core.state.State.Companion.state

class CustomAIManager {
    fun getAIForShip(ship: ShipAPI): ShipAIPlugin? {
        return when {
            !state.config.enableCustomAI -> null
            Global.getCurrentState() != GameState.COMBAT -> null

            shouldHaveCustomAI(ship) -> CustomShipAI(ship)
            shouldHaveWrapperAI(ship) -> WrapperShipAI(ship)

            else -> null
        }
    }

    private fun shouldHaveWrapperAI(ship: ShipAPI): Boolean {
        // Install wrapper AI in frigate ships, but not station modules.
        // Check for number of engines, as isModule method may return
        // incorrect result when the ship is being initialized.
        return ship.engineController.shipEngines.isNotEmpty() && ship.isFrigate
    }

    /** Currently, custom AI is enabled only for selected ships. */
    private fun shouldHaveCustomAI(ship: ShipAPI): Boolean {
        return when {
            ship.hullSpec.isPhase -> false
            ship.hullSpec.hints.contains(CARRIER) && !ship.hullSpec.hints.contains(COMBAT) -> false
            ship.isStation -> false
            !ship.isBig -> false
            ship.assignment?.type == RETREAT -> false

            // Selected ships.
            ship.hullSpec.hullId.startsWith("guardian") -> true
            ship.hullSpec.hullId.startsWith("sr_melvillei") -> true
            ship.hullSpec.shipSystemId == "lidararray" -> true

            // Player
            ship.isAlly -> false
            ship.owner == 0 && state.config.devMode -> true

            else -> false
        }
    }
}
