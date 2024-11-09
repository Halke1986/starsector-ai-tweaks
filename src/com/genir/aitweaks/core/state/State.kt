package com.genir.aitweaks.core.state

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.features.FleetCohesion

// Global combat state.
var state: State = State()

class State : BaseEveryFrameCombatPlugin() {
    init {
        // Register state to be used by plugins init method.
        state = this
    }

    val config: Config = Config()
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
        // Make sure the active combat state instance is accessible via the
        // global variable. This is required because SS initializes multiple
        // instances of EveryFrameCombatPlugin per combat but uses only one
        // of them, not necessarily the latest.
        state = this

        frameCount++

        // Advance plugins only in combat.
        if (Global.getCurrentState() == GameState.COMBAT)
            plugins.forEach { it.advance(dt, events) }
    }
}
