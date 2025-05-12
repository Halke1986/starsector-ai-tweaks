package com.genir.aitweaks.core.state

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.FleetCohesionAI
import com.genir.aitweaks.core.SearchAndDestroyManager
import com.genir.aitweaks.core.debug.DebugPlugin
import com.genir.aitweaks.core.debug.removeGrid
import com.genir.aitweaks.core.playerassist.AimAssistManager
import com.genir.aitweaks.core.playerassist.ShieldAssistManager
import com.genir.aitweaks.core.shipai.FleetSegmentation
import com.genir.aitweaks.core.shipai.coordinators.AttackCoordinator
import com.genir.aitweaks.core.shipai.coordinators.NavigationCoordinator
import com.genir.aitweaks.core.state.Config.Companion.config

class State : BaseEveryFrameCombatPlugin() {
    companion object {
        val state: State
            get() = stateSafe!!

        val stateSafe: State?
            get() = Global.getCombatEngine()?.customData?.get("aitweaks_combat_state") as? State
    }

    init {
        // Reload config at beginning of each battle.
        config = Config()
    }

    var frameCount: Int = 0
    var unpausedFrameCount: Int = 0
    val debugPlugin: DebugPlugin? = if (config.devMode) DebugPlugin() else null

    val fleetSegmentation: Array<FleetSegmentation> = arrayOf(FleetSegmentation(0), FleetSegmentation(1))
    val maneuverCoordinator: AttackCoordinator = AttackCoordinator()
    val navigateCoordinator: NavigationCoordinator = NavigationCoordinator()

    private val fleetCohesion: Array<FleetCohesionAI> = arrayOf(FleetCohesionAI(0), FleetCohesionAI(1))
    private val searchAndDestroy: SearchAndDestroyManager = SearchAndDestroyManager()

    private val plugins: List<BaseEveryFrameCombatPlugin> = listOf(
        fleetSegmentation[0],
        fleetSegmentation[1],
        fleetCohesion[0],
        fleetCohesion[1],
        searchAndDestroy,
        maneuverCoordinator,
        navigateCoordinator,
        Speedup(),
        AimAssistManager(),
        ShieldAssistManager(),
    )

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        debugPlugin?.advance(dt, events)

        if (frameCount == 0) {
            removeGrid()
        }

        // Advance plugins only in combat.
        if (Global.getCurrentState() == GameState.COMBAT) {
            VanillaKeymap.advance()
            plugins.forEach { it.advance(dt, events) }
        }

        frameCount++
        if (!Global.getCombatEngine().isPaused) {
            unpausedFrameCount++
        }
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        debugPlugin?.renderInUICoords(viewport)
    }
}
