package com.genir.aitweaks.core.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.extensions.isAutomated
import com.genir.aitweaks.core.state.State.Companion.state

/** Overriding automated ships AI and captain personality.
 * The override is done in BaseEveryFrameCombatPlugin because
 * placing it in BaseModPlugin and using PluginPick provides
 * no effect. */
class AutomatedShipAIManager : BaseEveryFrameCombatPlugin() {
    private val expectedPersonality: String

    init {
        val setting = state.config.aiPersonality
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
        automatedShips.filter { it.ai is BasicShipAI }.forEach { ship ->
            CustomAIManager().getAIForShip(ship)?.let { ship.shipAI = it }
        }

        // Replace only vanilla AI with incorrect personality
        // for automated ships in player fleet.
        automatedShips.forEach { ship ->
            when {
                ship.owner != 0 -> Unit

                // BasicShipAI needs configured personality override,
                // because it ignores captain personality setting.
                ship.ai is BasicShipAI -> {
                    (ship.ai as BasicShipAI).config.personalityOverride = expectedPersonality
                }

                // Custom AI (including BasicShipAI Wrapper) needs captain
                // personality change, because it ignores config personality
                // override.
                else -> {
                    if (ship.captain != null) ship.captain.setPersonality(expectedPersonality)
                    else (ship as Ship).setFallbackPersonalityId(expectedPersonality)
                }
            }
        }
    }
}
