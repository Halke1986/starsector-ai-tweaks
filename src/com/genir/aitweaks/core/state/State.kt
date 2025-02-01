package com.genir.aitweaks.core.state

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.FleetCohesionAI
import com.genir.aitweaks.core.debug.DebugPlugin
import com.genir.aitweaks.core.debug.removeGrid
import com.genir.aitweaks.core.playerassist.AimAssistManager
import com.genir.aitweaks.core.playerassist.AutoOmniShields
import com.genir.aitweaks.core.shipai.AttackCoordinator
import com.genir.aitweaks.core.shipai.FleetSegmentation
import com.genir.aitweaks.core.utils.Bounds
import java.lang.ref.WeakReference

class State : BaseEveryFrameCombatPlugin() {
    companion object {
        // Store the state instance as a weak reference to allow it
        // to be deallocated after combat ends. This prevents the
        // state from persisting across game reloads, which could
        // trigger a Starsector memory leak warning.
        private var stateValue: WeakReference<State>? = null

        // Global combat state. It remains accessible throughout combat
        // despite being stored as a weak reference. This works because
        // the state is an instance of EveryFrameCombatPlugin, which is
        // referenced by the game's Combat Engine.
        val state: State
            get() = stateValue!!.get()!!
    }

    init {
        // Register state to be used by plugins init method.
        stateValue = WeakReference(this)
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
        AttackCoordinator(),
    )

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        // Make sure the active combat state instance is accessible via the
        // global variable. This is required because SS initializes multiple
        // instances of EveryFrameCombatPlugin per combat but uses only one
        // of them, not necessarily the latest.
        stateValue = WeakReference(this)

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
