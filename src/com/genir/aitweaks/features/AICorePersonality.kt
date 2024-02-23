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
    private val personality = LunaSettings.getString("aitweaks", "aitweaks_ai_core_personality")

    override fun advance(timeDelta: Float, events: MutableList<InputEventAPI>?) {
        personality ?: return

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