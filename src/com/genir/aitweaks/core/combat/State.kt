package com.genir.aitweaks.core.combat

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.combat.trackers.AccelerationTracker
import com.genir.aitweaks.core.combat.trackers.ManeuverTargetTracker
import com.genir.aitweaks.core.combat.trackers.VelocityTracker
import com.genir.aitweaks.core.features.AimBot
import com.genir.aitweaks.core.features.FleetCohesion
import com.genir.aitweaks.core.features.shipai.CustomAIManager

fun combatState(): State = combatState!!

// Global combat state.
private var combatState: State? = null

class State : BaseEveryFrameCombatPlugin() {
    val customAIManager: CustomAIManager = CustomAIManager()

    val fleetCohesion: Array<FleetCohesion> = arrayOf(FleetCohesion(0), FleetCohesion(1))
    val aimBot: AimBot = AimBot()
    val velocityTracker: VelocityTracker = VelocityTracker()
    val accelerationTracker: AccelerationTracker = AccelerationTracker()
    val maneuverTargetTracker: ManeuverTargetTracker = ManeuverTargetTracker()

    private val plugins: List<BaseEveryFrameCombatPlugin> = listOf(
        fleetCohesion[0],
        fleetCohesion[1],
        aimBot,
        velocityTracker,
        accelerationTracker,
        maneuverTargetTracker,
        com.genir.aitweaks.core.features.AutoOmniShields(),
        com.genir.aitweaks.core.features.AutomatedShipAIManager(),
        com.genir.aitweaks.core.features.shipai.AttackCoord(),
    )

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        combatState = this

        plugins.forEach { it.advance(dt, events) }
    }
}