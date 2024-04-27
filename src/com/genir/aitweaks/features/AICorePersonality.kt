package com.genir.aitweaks.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.features.maneuver.newVanillaAI
import com.genir.aitweaks.utils.ai.AIPersonality
import com.genir.aitweaks.utils.ai.hasVanillaAI
import com.genir.aitweaks.utils.extensions.isAutomated
import lunalib.lunaSettings.LunaSettings

/** Overriding AI core captains personality. The override is done
 * in BaseEveryFrameCombatPlugin because placing it in BaseModPlugin
 * and using PluginPick provides no effect. */
class AICorePersonality : BaseEveryFrameCombatPlugin() {
    private val personalityPreset = fun(): String {
        val setting = LunaSettings.getString("aitweaks", "aitweaks_ai_core_personality")
        val allowed = listOf("timid", "cautious", "steady", "aggressive", "reckless")
        return if (setting != null && allowed.contains(setting)) setting
        else "aggressive"
    }()

    override fun advance(timeDelta: Float, events: MutableList<InputEventAPI>?) {
        // Replace only vanilla AI with incorrect personality.
        val shipsToUpdate = Global.getCombatEngine().ships.filter {
            it.owner == 0 && it.ai != null && it.isAutomated && it.hasVanillaAI && it.AIPersonality != personalityPreset
        }

        shipsToUpdate.forEach {
            // AssemblyShipAI needs captain personality change,
            // because configured personality override is ignored.
            it.captain.setPersonality(personalityPreset)

            // BasicShipAI needs configured personality override,
            // because captain personality is ignored for automated ships.
            val config = ShipAIConfig()
            config.personalityOverride = personalityPreset
            it.shipAI = newVanillaAI(it, config)
        }
    }
}
