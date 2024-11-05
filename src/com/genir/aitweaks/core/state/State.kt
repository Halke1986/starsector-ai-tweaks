package com.genir.aitweaks.core.state

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.features.FleetCohesion
import com.genir.aitweaks.core.features.shipai.CustomAIManager
import lunalib.lunaSettings.LunaSettings

// Global combat state.
var combatState: State = State()

class State : BaseEveryFrameCombatPlugin() {
    val customAIManager: CustomAIManager = CustomAIManager()
    var frameCount: Int = 0

    val fleetCohesion: Array<FleetCohesion> = arrayOf(FleetCohesion(0), FleetCohesion(1))
    val accelerationTracker: AccelerationTracker = AccelerationTracker()

    val devMode: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_devmode") ?: false
    val highlightCustomAI: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_highlight_custom_ai") ?: false
    val titleScreenFireIsOn: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_title_screen_fire") ?: false

    private val plugins: List<BaseEveryFrameCombatPlugin> = listOf(
        fleetCohesion[0],
        fleetCohesion[1],
        accelerationTracker,
        com.genir.aitweaks.core.features.AimAssist(),
        com.genir.aitweaks.core.features.AutoOmniShields(),
        com.genir.aitweaks.core.features.AutomatedShipAIManager(),
        com.genir.aitweaks.core.features.OverrideAutofire(),
        com.genir.aitweaks.core.features.shipai.AttackCoord(),
    )

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        combatState = this
        frameCount++

        // Advance plugins only in combat.
        if (Global.getCurrentState() == GameState.COMBAT)
            plugins.forEach { it.advance(dt, events) }
    }
}
