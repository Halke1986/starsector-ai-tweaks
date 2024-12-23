package com.genir.aitweaks.core.playerassist

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.campaign.CampaignEngine
import com.genir.aitweaks.core.state.State
import com.genir.aitweaks.core.state.VanillaKeymap
import com.genir.aitweaks.core.utils.makeAIDrone

class AimAssistManager : BaseEveryFrameCombatPlugin() {
    private var aiDrone: ShipAPI? = null

    var strafeModeOn: Boolean = false

    private companion object {
        val statusKey = Object()
    }

    override fun advance(timeDelta: Float, events: MutableList<InputEventAPI>?) {
        if (aiDrone == null) {
            aiDrone = makeAIDrone(AimAssistAI(this))
            Global.getCombatEngine().addEntity(aiDrone)
        }

        val memory: MemoryAPI = CampaignEngine.getInstance().memoryWithoutUpdate
        val enableAimAssist = memory.getBoolean("\$aitweaks_enableAimBot")

        // Display status icon.
        if (enableAimAssist) {
            val engine = Global.getCombatEngine()
            val icon = "graphics/icons/hullsys/interdictor_array.png"
            engine.maintainStatusForPlayerShip(statusKey, icon, "aim assist", "automatic target leading", false)
        }

        // Handle input.
        events?.forEach {
            // Toggle the aim bot and persist the setting to memory.
            if (!it.isConsumed && it.isKeyDownEvent && it.eventValue == State.state.config.aimAssistKeybind) {
                memory.set("\$aitweaks_enableAimBot", !enableAimAssist)
            }
        }

        updateStrafeMode()
    }

    /** Check if player activated the strafe mode. This method needs
     * to run in AimAssistManager as opposed to AimAssistAI, because
     * AI is not advancing when simulation is paused. */
    private fun updateStrafeMode() {
        if (Global.getSettings().isStrafeKeyAToggle) {
            if (VanillaKeymap.isKeyDownEvent(VanillaKeymap.PlayerAction.SHIP_STRAFE_KEY)) strafeModeOn = !strafeModeOn
        } else {
            strafeModeOn = VanillaKeymap.isKeyDown(VanillaKeymap.PlayerAction.SHIP_STRAFE_KEY)
            if (Global.getSettings().isAutoTurnMode) strafeModeOn = !strafeModeOn
        }
    }
}