package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAssignmentType.RETREAT
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.CARRIER
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.COMBAT
import com.genir.aitweaks.core.combat.combatState
import com.genir.aitweaks.core.utils.extensions.allGroupedWeapons
import com.genir.aitweaks.core.utils.extensions.assignment
import com.genir.aitweaks.core.utils.extensions.isFrigateShip
import lunalib.lunaSettings.LunaSettings.getBoolean

class CustomAIManager {
    val customAIEnabled: Boolean = getBoolean("aitweaks", "aitweaks_enable_custom_ship_ai") ?: false

    fun getCustomAIForShip(ship: ShipAPI): ShipAIPlugin? {
        return when {
            shouldHaveCustomAI(ship) -> CustomShipAI(ship)

            shouldHaveWrapperAI(ship) -> WrapperShipAI(ship)

            else -> null
        }
    }

    private fun shouldHaveWrapperAI(ship: ShipAPI): Boolean {
        return when {
            !customAIEnabled -> false
            Global.getCurrentState() != GameState.COMBAT -> false

            !ship.isFrigateShip -> false

            ship.owner != 0 -> false

            else -> ship.allGroupedWeapons.any { WrapperShipAI.shouldAim(it) }
        }
    }

    /** Currently, custom AI is enabled only for selected ships. */
    private fun shouldHaveCustomAI(ship: ShipAPI): Boolean {
        return when {
            !customAIEnabled -> false
            Global.getCurrentState() != GameState.COMBAT -> false

            ship.hullSpec.isPhase -> false
            ship.hullSpec.hints.contains(CARRIER) && !ship.hullSpec.hints.contains(COMBAT) -> false
            ship.isStation -> false
            !ship.isDestroyer && !ship.isCruiser && !ship.isCapital -> false
            ship.assignment?.type == RETREAT -> false

            // Selected ships.
            ship.hullSpec.hullId.startsWith("guardian") -> true
            ship.hullSpec.hullId.startsWith("sr_melvillei") -> true
            ship.hullSpec.shipSystemId == "lidararray" -> true

            // Player
            ship.isAlly -> false
            ship.owner == 0 && combatState().devmode -> true

            else -> false
        }
    }
}
