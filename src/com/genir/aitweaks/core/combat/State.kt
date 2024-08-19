package com.genir.aitweaks.core.combat

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.combat.trackers.AccelerationTracker
import com.genir.aitweaks.core.combat.trackers.ManeuverTargetTracker
import com.genir.aitweaks.core.features.FleetCohesion
import com.genir.aitweaks.core.features.shipai.CustomAIManager
import lunalib.lunaSettings.LunaSettings

fun combatState(): State = combatState!!

// Global combat state.
private var combatState: State? = null

class State : BaseEveryFrameCombatPlugin() {
    val customAIManager: CustomAIManager = CustomAIManager()

    val fleetCohesion: Array<FleetCohesion>?
    val accelerationTracker: AccelerationTracker = AccelerationTracker()
    val maneuverTargetTracker: ManeuverTargetTracker = ManeuverTargetTracker()

    private val plugins: List<BaseEveryFrameCombatPlugin>

    init {
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
            maneuverTargetTracker,
        ))

        // Features.
        plugins.addAll(listOf(
            com.genir.aitweaks.core.features.AimAssist(),
            com.genir.aitweaks.core.features.AutoOmniShields(),
            com.genir.aitweaks.core.features.AutomatedShipAIManager(),
            com.genir.aitweaks.core.features.shipai.AttackCoord(),
        ))

        this.plugins = plugins
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        combatState = this

        plugins.forEach { it.advance(dt, events) }
    }
}