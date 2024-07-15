package com.genir.aitweaks.core.combat.trackers

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.utils.extensions.attackTarget
import com.genir.aitweaks.core.utils.extensions.hasEscortAssignment
import com.genir.aitweaks.core.utils.extensions.isValidTarget

class TargetTracker : BaseEveryFrameCombatPlugin() {
    private val targets: MutableMap<ShipAPI, ShipAPI?> = mutableMapOf()

    operator fun get(index: ShipAPI): ShipAPI? = targets[index]

    /**
     * ShipAPI is inconsistent when returning maneuver target. It may return null
     * in some frames, even when the ship has a maneuver target. To avoid this problem,
     * last non-null maneuver target may be used.
     *
     * Even worse, when a ship is assigned an escort duty, maneuver target will always
     * be null. Then the autofire AI needs to discard the previous target, so it doesn't
     * get outdated.
     */
    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        Global.getCombatEngine().ships.filter { it.isAlive }.forEach { ship ->
            val newTarget = ship.attackTarget
            val prevTarget = targets[ship]

            targets[ship] = when {
                // Discard outdated attack target when ship has escort assignment.
                newTarget == null && prevTarget != null && ship.hasEscortAssignment -> null

                // Update attack target.
                newTarget != null -> newTarget

                // Previous target is no longer valid, discard it.
                prevTarget?.isValidTarget != true -> null

                // Continue to attack previous target.
                else -> prevTarget
            }
        }
    }
}