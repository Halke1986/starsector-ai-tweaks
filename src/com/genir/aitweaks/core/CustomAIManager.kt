package com.genir.aitweaks.core

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority.*
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.CARRIER
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.COMBAT
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.extensions.isAutomated
import com.genir.aitweaks.core.extensions.isModule
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.state.State.Companion.state

object CustomAIManager {
    fun pickShipAI(member: FleetMemberAPI?, ship: ShipAPI): PluginPick<ShipAIPlugin>? {
        if (Global.getCurrentState() != GameState.COMBAT) {
            return null
        }

        if (ship.isFighter) {
            return null
        }

        // If a Custom AI is explicitly assigned to the ship via a hullmod,
        // prioritize it highly, potentially overriding AIs from other mods.
        if (shouldHaveCustomAIByHullmod(ship)) {
            return PluginPick(CustomShipAI(ship), MOD_SPECIFIC)
        }

        val useCustom: Boolean = shouldHaveCustomAI(ship)
        val useWrapper: Boolean = shouldHaveWrapperAI(ship)
        val usePersonality: Boolean = shouldHaveCustomPersonality(ship)

        // Nothing to override, return.
        if (!useCustom && !useWrapper && !usePersonality) {
            return null
        }

        // Identify other mods or vanilla AI pick.
        val mods = Global.getSettings().modManager.enabledModPlugins
        val picks = mods.mapNotNull { it.pickShipAI(member, ship) }
        val sortedPicks = picks.sortedWith(compareBy { it.priority.ordinal })
        val otherPick: PluginPick<ShipAIPlugin>? = sortedPicks.lastOrNull()

        // Do not override AI selected by a mod other than AI Tweaks.
        when (otherPick?.priority) {
            MOD_GENERAL, MOD_SET, MOD_SPECIFIC, HIGHEST -> return null

            else -> Unit
        }

        // Custom AI does not care about personality config.
        if (useCustom) {
            return PluginPick(CustomShipAI(ship), MOD_SPECIFIC)
        }

        // Get personality config.
        val config: ShipAIConfig = when {
            usePersonality -> {
                // Configure personality override for selected ships.
                val config = ShipAIConfig()
                config.personalityOverride = customPersonality()
                config
            }

            else -> {
                // Use vanilla defined config, if possible.
                (otherPick?.plugin as? BasicShipAI)?.config ?: ShipAIConfig()
            }
        }

        if (useWrapper) {
            return PluginPick(WrapperShipAI(ship, config), MOD_SPECIFIC)
        }

        // Note: In simulator, the configured personality is ignored for player controlled ship
        // without a captain assigned. Doctrine defined personality is used instead.
        return PluginPick(BasicShipAI(ship as Ship, config), MOD_SPECIFIC)
    }

    /** Returns true is custom AI can control the given ship. */
    fun canHaveCustomAI(ship: ShipAPI): Boolean {
        return when {
            ship.owner == 0 && Global.getSettings().modManager.isModEnabled("aitweaksunlock") -> true

            ship.hullSpec.isPhase -> false
            ship.hullSpec.hints.contains(CARRIER) && !ship.hullSpec.hints.contains(COMBAT) -> false
            ship.isStation -> false
            ship.isModule -> false
            ship.isFrigate -> false
            ship.isFighter -> false

            else -> true
        }
    }

    /** Is the Custom AI assigned to ship explicitly via a hullmod. */
    private fun shouldHaveCustomAIByHullmod(ship: ShipAPI): Boolean {
        return canHaveCustomAI(ship) && ship.variant.hasHullMod("aitweaks_custom_ship_ai")
    }

    private fun shouldHaveCustomAI(ship: ShipAPI): Boolean {
        return when {
            !canHaveCustomAI(ship) -> false

            // Non-player ships can have custom AI by default, without the hullmod.
            ship.owner == 1 || ship.isAlly -> when {
                ship.hullSpec.hullId.startsWith("guardian") -> true
                ship.hullSpec.hullId.startsWith("sr_melvillei") -> true
                ship.hullSpec.shipSystemId == "lidararray" -> true

                // Simulator.
                state.config.enableSimulatorCustomAI && Global.getCombatEngine().isSimulation -> true

                else -> false
            }

            else -> false
        }
    }

    private fun shouldHaveWrapperAI(ship: ShipAPI): Boolean {
        // Install wrapper AI in frigate ships, but not station modules.
        // Check for number of engines, as isModule method may return
        // incorrect result when the ship is being initialized.
        return ship.engineController.shipEngines.isNotEmpty() && ship.isFrigate
    }

    private fun shouldHaveCustomPersonality(ship: ShipAPI): Boolean {
        return when {
            !ship.isAutomated -> false

            ship.owner != 0 || ship.isAlly -> false

            state.config.aiPersonality?.lowercase() == "vanilla" -> false

            else -> true
        }
    }

    private fun customPersonality(): String {
        return when (val personality = state.config.aiPersonality?.lowercase()) {
            "timid", "cautious", "steady", "aggressive", "reckless" -> personality

            // Default.
            else -> "aggressive"
        }
    }
}
