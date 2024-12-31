package com.genir.aitweaks.core.state

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.FleetCohesionAI
import com.genir.aitweaks.core.FleetSegmentation
import com.genir.aitweaks.core.debug.DebugPlugin
import com.genir.aitweaks.core.debug.removeGrid
import com.genir.aitweaks.core.playerassist.AimAssistManager
import com.genir.aitweaks.core.playerassist.AutoOmniShields
import com.genir.aitweaks.core.utils.Bounds

class State : BaseEveryFrameCombatPlugin() {
    companion object {
        // Global combat state.
        val state: State
            get() = stateValue!!

        private var stateValue: State? = null
    }

    init {
        // Register state to be used by plugins init method.
        stateValue = this
    }

    val config: Config = Config()
    val bounds = Bounds()
    var frameCount: Int = 0
    val debugPlugin: DebugPlugin? = if (config.devMode) DebugPlugin() else null

    val fleetSegmentation: Array<FleetSegmentation> = arrayOf(FleetSegmentation(0), FleetSegmentation(1))
    val fleetCohesion: Array<FleetCohesionAI> = arrayOf(FleetCohesionAI(0), FleetCohesionAI(1))
    val accelerationTracker: AccelerationTracker = AccelerationTracker()

    private val plugins: List<BaseEveryFrameCombatPlugin> = listOf(
        fleetSegmentation[0],
        fleetSegmentation[1],
        fleetCohesion[0],
        fleetCohesion[1],
        accelerationTracker,
        AimAssistManager(),
        AutoOmniShields(),
        com.genir.aitweaks.core.AutomatedShipAIManager(),
        com.genir.aitweaks.core.shipai.AttackCoordinator(),
        com.genir.aitweaks.core.shipai.AutofireManagerOverride(),
    )

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        // Make sure the active combat state instance is accessible via the
        // global variable. This is required because SS initializes multiple
        // instances of EveryFrameCombatPlugin per combat but uses only one
        // of them, not necessarily the latest.
        stateValue = this

        debugPlugin?.advance(dt, events)

        if (frameCount == 0) removeGrid()

        // Advance plugins only in combat.
        if (Global.getCurrentState() == GameState.COMBAT) {
            VanillaKeymap.advance()
            plugins.forEach { it.advance(dt, events) }
        }

        frameCount++
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        debugPlugin?.renderInUICoords(viewport)
    }
}
