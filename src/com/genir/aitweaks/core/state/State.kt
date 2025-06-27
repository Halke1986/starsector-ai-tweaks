package com.genir.aitweaks.core.state

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.debug.DebugPlugin
import com.genir.aitweaks.core.debug.removeGrid
import com.genir.aitweaks.core.playerassist.AimAssistManager
import com.genir.aitweaks.core.playerassist.ShieldAssistManager
import com.genir.aitweaks.core.shipai.global.GlobalAI
import com.genir.aitweaks.core.state.Config.Companion.config
import com.genir.aitweaks.core.utils.FrameTracker

class State : BaseEveryFrameCombatPlugin() {
    companion object {
        val state: State?
            get() = Global.getCombatEngine()?.customData?.get("aitweaks_combat_state") as? State
    }

    init {
        // Reload config at beginning of each battle.
        config = Config()
    }

    private val frameTracker: FrameTracker = FrameTracker()
    val debugPlugin: DebugPlugin? = if (config.devMode) DebugPlugin() else null
    val globalAI: GlobalAI? = if (config.useVanillaAI) null else GlobalAI()

    private val plugins: List<BaseEveryFrameCombatPlugin?> = listOf(
        globalAI,
        Speedup(),
        AimAssistManager(),
        ShieldAssistManager(),
    )

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        if (frameTracker.count == 0) {
            removeGrid()
        }

        debugPlugin?.advance(dt, events)

        // Advance plugins only in combat.
        if (Global.getCurrentState() == GameState.COMBAT) {
            VanillaKeymap.advance()
            plugins.forEach { it?.advance(dt, events) }
        }

        frameTracker.advance()
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        debugPlugin?.renderInUICoords(viewport)
    }
}
