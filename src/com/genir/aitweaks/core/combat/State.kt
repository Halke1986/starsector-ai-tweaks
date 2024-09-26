package com.genir.aitweaks.core.combat

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.combat.trackers.AccelerationTracker
import com.genir.aitweaks.core.combat.trackers.ManeuverTargetTracker
import com.genir.aitweaks.core.features.FleetCohesion
import com.genir.aitweaks.core.features.shipai.CustomAIManager

fun combatState(): State = combatState

// Global combat state.
private var combatState: State = State()

class State : BaseEveryFrameCombatPlugin() {
    val customAIManager: CustomAIManager = CustomAIManager()

    val fleetCohesion: Array<FleetCohesion> = arrayOf(FleetCohesion(0), FleetCohesion(1))
    val accelerationTracker: AccelerationTracker = AccelerationTracker()
    val maneuverTargetTracker: ManeuverTargetTracker = ManeuverTargetTracker()

    var frameCount: Int = 0

    private val plugins: List<BaseEveryFrameCombatPlugin> = listOf(
        fleetCohesion[0],
        fleetCohesion[1],
        accelerationTracker,
        maneuverTargetTracker,
        com.genir.aitweaks.core.features.AimAssist(),
        com.genir.aitweaks.core.features.AutoOmniShields(),
        com.genir.aitweaks.core.features.AutomatedShipAIManager(),
        com.genir.aitweaks.core.features.shipai.AttackCoord(),
    )

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        combatState = this
        frameCount++

        plugins.forEach { it.advance(dt, events) }
    }
}