package com.genir.aitweaks.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.genir.aitweaks.features.shipai.assemblyShipAIClass
import com.genir.aitweaks.features.shipai.hasAIType
import com.genir.aitweaks.features.shipai.newAssemblyAI
import com.genir.aitweaks.features.shipai.shouldHaveAssemblyAI
import com.genir.aitweaks.utils.extensions.isAutomated
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
        automatedShips.forEach { ship ->
            if (ship.hasAIType(BasicShipAI::class.java) && shouldHaveAssemblyAI(ship))
                ship.shipAI = newAssemblyAI(ship)
        }

        // Replace only vanilla AI with incorrect personality
        // for automated ships in player fleet.
        automatedShips.forEach { ship ->
            when {
                ship.owner != 0 -> Unit

                // BasicShipAI needs configured personality override,
                // because it ignores captain personality for automated ships.
                ship.hasAIType(BasicShipAI::class.java) -> {
                    (ship.ai as BasicShipAI).config.personalityOverride = expectedPersonality
                }

                // AssemblyShipAI needs captain personality change,
                // because it ignores configured personality override.
                ship.hasAIType(assemblyShipAIClass()) -> {
                    ship.captain.setPersonality(expectedPersonality)
                }
            }
        }
    }
}
