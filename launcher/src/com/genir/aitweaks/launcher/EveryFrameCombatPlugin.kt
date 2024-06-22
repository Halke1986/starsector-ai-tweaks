package com.genir.aitweaks.launcher

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI

class EveryFrameCombatPlugin : BaseEveryFrameCombatPlugin() {
    private val corePlugin: BaseEveryFrameCombatPlugin

    init {
        // Rebuild core loader at the beginning of each battle;
        // used for aitweaks-core-dev-*.jar hot reload.
        CoreLoaderManager().reload()

        val corePluginClass = coreLoader.loadClass("com.genir.aitweaks.core.EveryFrameCombatPlugin")
        corePlugin = corePluginClass.newInstance() as BaseEveryFrameCombatPlugin
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        corePlugin.advance(dt, events)
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        corePlugin.renderInUICoords(viewport)
    }
}
