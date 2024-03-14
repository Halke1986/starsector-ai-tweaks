package com.genir.aitweaks.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.utils.extensions.AIPersonality
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
        val shipsToUpdate = Global.getCombatEngine().ships.filter {
            it.owner == 0 && it.ai != null && it.isAutomated && it.AIPersonality != personalityPreset
        }

        shipsToUpdate.forEach {
            val config = ShipAIConfig()
            config.personalityOverride = personalityPreset
            (it as Ship).ai = BasicShipAI(it, config)
        }
    }
}
