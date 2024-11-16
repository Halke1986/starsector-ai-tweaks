package com.genir.aitweaks.core.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.campaign.CampaignEngine
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.aitweaks.core.state.VanillaKeymap.Action.SHIP_STRAFE_KEY
import com.genir.aitweaks.core.state.VanillaKeymap.isKeyDown
import com.genir.aitweaks.core.state.VanillaKeymap.isKeyDownEvent

class AimAssistManager : BaseEveryFrameCombatPlugin() {
    private var aiDrone: ShipAPI? = null

    var strafeModeOn: Boolean = false

    private companion object {
        val statusKey = Object()
    }

    override fun advance(timeDelta: Float, events: MutableList<InputEventAPI>?) {
        if (aiDrone == null) {
            aiDrone = makeAIDrone()
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
            if (!it.isConsumed && it.isKeyDownEvent && it.eventValue == state.config.aimAssistKeybind) {
                memory.set("\$aitweaks_enableAimBot", !enableAimAssist)
            }
        }

        updateStrafeMode()
    }

    private fun updateStrafeMode() {
        if (Global.getSettings().isStrafeKeyAToggle) {
            if (isKeyDownEvent(SHIP_STRAFE_KEY)) strafeModeOn = !strafeModeOn
        } else {
            strafeModeOn = isKeyDown(SHIP_STRAFE_KEY)
            if (Global.getSettings().isAutoTurnMode) strafeModeOn = !strafeModeOn
        }
    }

    private fun makeAIDrone(): ShipAPI {
        val spec = Global.getSettings().getHullSpec("dem_drone")
        val v = Global.getSettings().createEmptyVariant("dem_drone", spec)
        val aiDrone = Global.getCombatEngine().createFXDrone(v)

        aiDrone.owner = 0
        aiDrone.mutableStats.hullDamageTakenMult.modifyMult("aitweaks_ai_drone", 0f) // so it's non-targetable
        aiDrone.isDrone = true
        aiDrone.collisionClass = CollisionClass.NONE

        aiDrone.location.y = -1e7f
        aiDrone.shipAI = AimAssistAI(this)

        return aiDrone
    }
}
