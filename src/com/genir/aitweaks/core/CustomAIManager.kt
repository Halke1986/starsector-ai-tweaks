package com.genir.aitweaks.core

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority.MOD_SPECIFIC
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.CARRIER
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.COMBAT
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.genir.aitweaks.core.extensions.isAutomated
import com.genir.aitweaks.core.extensions.isModule
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.ExtendedShipAI
import com.genir.aitweaks.core.state.State.Companion.state

object CustomAIManager {
    fun pickShipAI(member: FleetMemberAPI?, ship: ShipAPI): PluginPick<ShipAIPlugin>? {
        when {
            Global.getCurrentState() != GameState.COMBAT -> return null

            ship.isFighter -> return null
        }

        // If a Custom AI is explicitly assigned to the ship via a hullmod,
        // prioritize it highly, potentially overriding AIs from other mods.
        if (shouldHaveCustomAIByHullmod(ship)) {
            return PluginPick(CustomShipAI(ship), MOD_SPECIFIC)
        }

        // Identify other mods or vanilla AI pick.
        val mods = Global.getSettings().modManager.enabledModPlugins
        val picks = mods.mapNotNull { it.pickShipAI(member, ship) }
        val sortedPicks = picks.sortedWith(compareBy { it.priority.ordinal })
        val otherPick: PluginPick<ShipAIPlugin>? = sortedPicks.lastOrNull()
        val otherAI: ShipAIPlugin? = otherPick?.plugin

        // Modify only the vanilla AI.
        if (otherAI != null && otherAI !is BasicShipAI) {
            return null
        }

        // Custom AI does not care about personality config.
        if (shouldHaveCustomAI(ship)) {
            return PluginPick(CustomShipAI(ship), MOD_SPECIFIC)
        }

        // Get personality config.
        // Note: In simulator, the configured personality is ignored for
        // player controlled ship without a captain assigned. Doctrine
        // defined personality is used instead.
        val config: ShipAIConfig = when {
            shouldHaveCustomPersonality(ship) -> {
                // Configure personality override for selected ships.
                val config = ShipAIConfig()
                config.personalityOverride = customPersonality()
                config
            }

            else -> {
                // Use vanilla defined config.
                otherAI?.config ?: ShipAIConfig()
            }
        }

        // All ships with vanilla AI should receive ExtendedShipAI,
        // unless CustomShipAI was already assigned.
        return PluginPick(ExtendedShipAI(ship, config), MOD_SPECIFIC)
    }

    /** Returns true is custom AI can control the given ship.
     * NOTE: this code needs to be synchronized with a copy in CustomShipAI hullmod. */
    private fun canHaveCustomAI(ship: ShipAPI): Boolean {
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
