package com.genir.aitweaks.core.combat

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.combat.trackers.AccelerationTracker
import com.genir.aitweaks.core.combat.trackers.TargetTracker
import com.genir.aitweaks.core.features.FleetCohesion
import lunalib.lunaSettings.LunaSettings

fun combatState(): State = combatState!!

// Global combat state.
private var combatState: State? = null

class State : BaseEveryFrameCombatPlugin() {
    val fleetCohesion: Array<FleetCohesion>?
    val accelerationTracker: AccelerationTracker = AccelerationTracker()
    val targetTracker: TargetTracker = TargetTracker()

    private val plugins: List<BaseEveryFrameCombatPlugin>

    init {
        combatState = this

        val plugins: MutableList<BaseEveryFrameCombatPlugin> = mutableListOf()

        // Fleet cohesion.
        if (LunaSettings.getBoolean("aitweaks", "aitweaks_enable_fleet_cohesion_ai") == true) {
            fleetCohesion = arrayOf(FleetCohesion(0), FleetCohesion(1))
            plugins.addAll(fleetCohesion)
        } else {
            fleetCohesion = null
        }

        // Trackers.
        plugins.addAll(listOf(
            accelerationTracker,
            targetTracker,
        ))

        // Features.
        plugins.addAll(listOf(
            com.genir.aitweaks.core.features.AutoOmniShields(),
            com.genir.aitweaks.core.features.AutomatedShipAIManager(),
            com.genir.aitweaks.core.features.lidar.AIManager(),
            com.genir.aitweaks.core.features.shipai.Guardian(),
            com.genir.aitweaks.core.features.shipai.ai.AttackCoord(),
        ))

        this.plugins = plugins
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        plugins.forEach { it.advance(dt, events) }
    }
}