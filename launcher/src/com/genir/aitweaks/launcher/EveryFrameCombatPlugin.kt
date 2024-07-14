package com.genir.aitweaks.launcher

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import lunalib.lunaSettings.LunaSettings

class EveryFrameCombatPlugin : BaseEveryFrameCombatPlugin() {
    private val combatState: BaseEveryFrameCombatPlugin
    private val debugPlugin: BaseEveryFrameCombatPlugin?

    init {
        // Rebuild core loader at the beginning of each battle;
        // used for aitweaks-core-dev-*.jar hot reload.
        CoreLoaderManager().reload()

        // Combat state.
        val combatStateClass = coreLoader.loadClass("com.genir.aitweaks.core.combat.State")
        combatState = combatStateClass.newInstance() as BaseEveryFrameCombatPlugin

        // Debug plugin.
        if (LunaSettings.getBoolean("aitweaks", "aitweaks_enable_devmode") == true) {
            val debugPluginClass = coreLoader.loadClass("com.genir.aitweaks.core.debug.DebugPlugin")
            debugPlugin = debugPluginClass.newInstance() as BaseEveryFrameCombatPlugin
        } else {
            debugPlugin = null
        }
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        combatState.advance(dt, events)
        debugPlugin?.advance(dt, events)
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        debugPlugin?.renderInUICoords(viewport)
    }
}
