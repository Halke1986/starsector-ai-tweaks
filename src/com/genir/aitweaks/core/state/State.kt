package com.genir.aitweaks.core.state

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.debug.DebugPlugin
import com.genir.aitweaks.core.features.FleetCohesion
import com.genir.aitweaks.core.utils.Bounds

class State : BaseEveryFrameCombatPlugin() {
    companion object {
        // Global combat state.
        var state: State = State()
    }

    init {
        // Register state to be used by plugins init method.
        state = this
    }

    val config: Config = Config()
    val bounds = Bounds()
    var frameCount: Int = 0
    private val debugPlugin = if (config.devMode) DebugPlugin() else null

    val fleetCohesion: Array<FleetCohesion> = arrayOf(FleetCohesion(0), FleetCohesion(1))
    val accelerationTracker: AccelerationTracker = AccelerationTracker()

    private val plugins: List<BaseEveryFrameCombatPlugin> = listOf(
        fleetCohesion[0],
        fleetCohesion[1],
        accelerationTracker,
        com.genir.aitweaks.core.features.AimAssistManager(),
        com.genir.aitweaks.core.features.AutoOmniShields(),
        com.genir.aitweaks.core.features.AutomatedShipAIManager(),
        com.genir.aitweaks.core.features.shipai.AttackCoord(),
        com.genir.aitweaks.core.features.shipai.AutofireManagerOverride(),
    )

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        // Make sure the active combat state instance is accessible via the
        // global variable. This is required because SS initializes multiple
        // instances of EveryFrameCombatPlugin per combat but uses only one
        // of them, not necessarily the latest.
        state = this

        frameCount++
        debugPlugin?.advance(dt, events)

        // Advance plugins only in combat.
        if (Global.getCurrentState() == GameState.COMBAT) {
            VanillaKeymap.advance()
            plugins.forEach { it.advance(dt, events) }
        }
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        debugPlugin?.renderInUICoords(viewport)
    }
}
