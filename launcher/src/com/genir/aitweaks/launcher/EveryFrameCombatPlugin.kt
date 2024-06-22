package com.genir.aitweaks.launcher

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI

class EveryFrameCombatPlugin : BaseEveryFrameCombatPlugin() {
    private val plugins: List<BaseEveryFrameCombatPlugin>

    init {
        // Rebuild core loader at the beginning of each battle;
        // used for aitweaks-core-dev-*.jar hot reload.
        coreLoader = newCoreLoader()

        val loadPlugin = fun(path: String): BaseEveryFrameCombatPlugin {
            return coreLoader.loadClass(path).newInstance() as BaseEveryFrameCombatPlugin
        }

        plugins = listOf(
            loadPlugin("com.genir.aitweaks.debug.DebugPlugin"),
            loadPlugin("com.genir.aitweaks.utils.AccelerationTracker"),
            loadPlugin("com.genir.aitweaks.utils.TargetTracker"),
            loadPlugin("com.genir.aitweaks.features.AutoOmniShields"),
            loadPlugin("com.genir.aitweaks.features.AutomatedShipAIManager"),
            loadPlugin("com.genir.aitweaks.features.FleetCohesion"),
            loadPlugin("com.genir.aitweaks.features.lidar.AIManager"),
            loadPlugin("com.genir.aitweaks.features.shipai.Guardian"),
//        loadPlugin("com.genir.aitweaks.features.shipai.ai.AttackCoord"),
        )
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        plugins.forEach { it.advance(dt, events) }
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        plugins.forEach { it.renderInUICoords(viewport) }
    }
}
