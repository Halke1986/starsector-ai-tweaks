package com.genir.aitweaks.core.state

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.features.CustomAIManager
import com.genir.aitweaks.core.features.FleetCohesion

// Global combat state.
var state: State = State()

class State : BaseEveryFrameCombatPlugin() {
    val config: Config = Config()
    val customAIManager: CustomAIManager = CustomAIManager()
    var frameCount: Int = 0

    val fleetCohesion: Array<FleetCohesion> = arrayOf(FleetCohesion(0), FleetCohesion(1))
    val accelerationTracker: AccelerationTracker = AccelerationTracker()

    private val plugins: List<BaseEveryFrameCombatPlugin> = listOf(
        fleetCohesion[0],
        fleetCohesion[1],
        accelerationTracker,
        com.genir.aitweaks.core.features.AimAssist(),
        com.genir.aitweaks.core.features.AutoOmniShields(),
        com.genir.aitweaks.core.features.AutomatedShipAIManager(),
        com.genir.aitweaks.core.features.OverrideAutofireManager(),
        com.genir.aitweaks.core.features.shipai.AttackCoord(),
    )

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        state = this
        frameCount++

        // Advance plugins only in combat.
        if (Global.getCurrentState() == GameState.COMBAT)
            plugins.forEach { it.advance(dt, events) }
    }
}
