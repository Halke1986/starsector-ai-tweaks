package com.genir.aitweaks.core.playerassist

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.campaign.CampaignEngine
import com.genir.aitweaks.core.playerassist.LeadingPipIntegration.overrideTargetingLeadIndicator
import com.genir.aitweaks.core.state.State
import com.genir.aitweaks.core.state.VanillaKeymap

class AimAssistManager : BaseEveryFrameCombatPlugin() {
    private var aiDrone: ShipAPI? = null

    var enableAimAssist: Boolean = false
    var strafeModeOn: Boolean = false

    private companion object {
        val statusKey = Object()
        const val ENABLE_AIM_ASSIST_KEY = "\$aitweaks_enableAimBot"
    }

    override fun advance(timeDelta: Float, events: MutableList<InputEventAPI>?) {
        val memory: MemoryAPI = CampaignEngine.getInstance().memoryWithoutUpdate
        enableAimAssist = memory.getBoolean(ENABLE_AIM_ASSIST_KEY)

        // Handle input.
        updateStrafeMode()
        events?.forEach {
            // Toggle the aim bot and persist the setting to memory.
            if (!it.isConsumed && it.isKeyDownEvent && it.eventValue == State.state.config.aimAssistKeybind) {
                enableAimAssist = !enableAimAssist
                memory.set(ENABLE_AIM_ASSIST_KEY, enableAimAssist)
            }
        }

        if (!enableAimAssist) return

        // Make a drone to hold player ship aim assist AI.
        if (aiDrone == null) {
            aiDrone = makeAIDrone(AimAssistAI(this))
            Global.getCombatEngine().addEntity(aiDrone)
        }

        overrideTargetingLeadIndicator(this)

        // Display status icon.
        val engine = Global.getCombatEngine()
        val icon = "graphics/icons/hullsys/interdictor_array.png"
        engine.maintainStatusForPlayerShip(statusKey, icon, "aim assist", "automatic target leading", false)
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
