package com.genir.aitweaks.core.playerassist

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.state.Config.Companion.config

class ShieldAssistManager : BaseEveryFrameCombatPlugin() {
    private var aiDrone: ShipAPI? = null
    var ai: ShieldAssistAI? = null
    var enableShieldAssist = false

    companion object {
        const val ENABLE_SHIELD_ASSIST_KEY = "\$aitweaks_enable_shield_assist"
        const val RENDER_PLUGIN_KEY = "aitweaks_shield_assist_renderer"
    }

    override fun advance(timeDelta: Float, events: MutableList<InputEventAPI>?) {
        // Load shield assist setting from memory.
        val memory: MemoryAPI = Global.getSector().memoryWithoutUpdate
        enableShieldAssist = memory.getBoolean(ENABLE_SHIELD_ASSIST_KEY)

        // Handle input. The input needs to be handled even when the game
        // is paused, therefore the handler can't be placed in the AI class.
        events?.forEach {
            // Toggle the shield assist and persist the setting to memory.
            if (!it.isConsumed && it.isKeyDownEvent && it.eventValue == config.shieldAssistKeybind) {
                enableShieldAssist = !enableShieldAssist
                memory.set(ENABLE_SHIELD_ASSIST_KEY, enableShieldAssist)
            }
        }

        // Nothing to do, exit early.
        if (!enableShieldAssist) {
            return
        }

        // Make a drone to hold the player ship shield AI.
        var aiDrone = aiDrone
        if (aiDrone == null) {
            ai = ShieldAssistAI(this)
            aiDrone = makeAIDrone(ai!!, "aitweaks_shield_assist_drone")

            Global.getCombatEngine().addEntity(aiDrone)
            this.aiDrone = aiDrone
        }

        syncTimeWithPlayerShip(aiDrone)

        // Initialize shield assist rendering plugin.
        val engine = Global.getCombatEngine()
        if (!engine.customData.containsKey(RENDER_PLUGIN_KEY)) {
            engine.addLayeredRenderingPlugin(ShieldAssistIndicator(this))
            engine.customData[RENDER_PLUGIN_KEY] = true
        }
    }
}
