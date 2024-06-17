package com.genir.aitweaks

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI

class AITweaksEveryFramePlugin : BaseEveryFrameCombatPlugin() {
    private val plugins: List<BaseEveryFrameCombatPlugin> = listOf(
        com.genir.aitweaks.debug.DebugPlugin(),
        com.genir.aitweaks.utils.AccelerationTracker(),
        com.genir.aitweaks.utils.TargetTracker(),
        com.genir.aitweaks.features.AutoOmniShields(),
        com.genir.aitweaks.features.AutomatedShipAIManager(),
        com.genir.aitweaks.features.FleetCohesion(),
        com.genir.aitweaks.features.lidar.AIManager(),
        com.genir.aitweaks.features.shipai.Guardian(),
//        com.genir.aitweaks.features.shipai.ai.AttackCoord(),
    )

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        plugins.forEach { it.advance(dt, events) }
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        plugins.forEach { it.renderInUICoords(viewport) }
    }
}
