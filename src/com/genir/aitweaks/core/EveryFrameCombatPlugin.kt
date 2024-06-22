package com.genir.aitweaks.core

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import lunalib.lunaSettings.LunaSettings

class EveryFrameCombatPlugin : BaseEveryFrameCombatPlugin() {
    private val plugins: List<BaseEveryFrameCombatPlugin>

    init {
        val plugins: MutableList<BaseEveryFrameCombatPlugin> = mutableListOf(
            com.genir.aitweaks.core.utils.AccelerationTracker(),
            com.genir.aitweaks.core.utils.TargetTracker(),
            com.genir.aitweaks.core.features.AutoOmniShields(),
            com.genir.aitweaks.core.features.AutomatedShipAIManager(),
            com.genir.aitweaks.core.features.FleetCohesion(),
            com.genir.aitweaks.core.features.lidar.AIManager(),
            com.genir.aitweaks.core.features.shipai.Guardian(),
            //com.genir.aitweaks.core.features.shipai.ai.AttackCoord(),
        )

        if (LunaSettings.getBoolean("aitweaks", "aitweaks_enable_devmode") == true) {
            plugins.add(com.genir.aitweaks.core.debug.DebugPlugin())
        }

        this.plugins = plugins
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        plugins.forEach { it.advance(dt, events) }
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        plugins.forEach { it.renderInUICoords(viewport) }
    }
}
