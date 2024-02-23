package com.genir.aitweaks.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.utils.StarfarerAccess
import lunalib.lunaSettings.LunaSettings

/** Overriding AI core captains personality. The override is done
 * in BaseEveryFrameCombatPlugin because placing it in BaseModPlugin
 * and using PluginPick provided no effect. */
class AICorePersonality : BaseEveryFrameCombatPlugin() {
    private val personality = fun(): String {
        val setting = LunaSettings.getString("aitweaks", "aitweaks_ai_core_personality")
        val allowed = listOf("timid", "cautious", "steady", "aggressive", "reckless")
        return if (setting != null && allowed.contains(setting)) setting
        else "aggressive"
    }()

    override fun advance(timeDelta: Float, events: MutableList<InputEventAPI>?) {
        val ships = Global.getCombatEngine().ships
        ships.filter { it.owner == 0 && it?.captain?.isAICore == true && it.captain.personalityAPI.id != personality }.forEach {
            val config = ShipAIConfig()
            config.personalityOverride = personality
            it.captain.setPersonality(personality)
            val ai = Global.getSettings().createDefaultShipAI(it, config)
            StarfarerAccess.setShipAI(it as Ship, Ship.ShipAIWrapper(ai))
        }
    }
}