package com.genir.aitweaks.core.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.genir.aitweaks.core.combat.combatState
import com.genir.aitweaks.core.utils.extensions.hasCustomShipAI
import com.genir.aitweaks.core.utils.extensions.hasBasicShipAI
import com.genir.aitweaks.core.utils.extensions.isAutomated
import lunalib.lunaSettings.LunaSettings

/** Overriding automated ships AI and captain personality.
 * The override is done in BaseEveryFrameCombatPlugin because
 * placing it in BaseModPlugin and using PluginPick provides
 * no effect. */
class AutomatedShipAIManager : BaseEveryFrameCombatPlugin() {
    private val expectedPersonality: String

    init {
        val setting = LunaSettings.getString("aitweaks", "aitweaks_ai_core_personality")
        val allowed = listOf("timid", "cautious", "steady", "aggressive", "reckless")
        val default = "aggressive"

        expectedPersonality = if (setting != null && allowed.contains(setting)) setting
        else default
    }

    override fun advance(timeDelta: Float, events: MutableList<InputEventAPI>?) {
        val automatedShips = Global.getCombatEngine().ships.filter { it.isAutomated }

        // Replace vanilla AI for eligible ships. This should happen
        // in BaseModPlugin.pickShipAI, but vanilla overrides the choice
        // with high priority.
        automatedShips.filter { it.hasBasicShipAI }.forEach { ship ->
            combatState().customAIManager.getCustomAIForShip(ship)?.let { ship.shipAI = it }
        }

        // Replace only vanilla AI with incorrect personality
        // for automated ships in player fleet.
        automatedShips.forEach { ship ->
            when {
                ship.owner != 0 -> Unit

                // BasicShipAI needs configured personality override,
                // because it ignores captain personality for automated ships.
                ship.hasBasicShipAI -> {
                    (ship.ai as BasicShipAI).config.personalityOverride = expectedPersonality
                }

                // Custom AI needs captain personality change,
                // because it ignores configured personality override.
                ship.hasCustomShipAI -> {
                    ship.captain.setPersonality(expectedPersonality)
                }
            }
        }
    }
}
