package com.genir.aitweaks.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.utils.extensions.hasEscortAssignment
import com.genir.aitweaks.utils.extensions.isValidTarget
import com.genir.aitweaks.utils.extensions.trueShipTarget

var targetTracker: TargetTracker = TargetTracker()

class TargetTracker : BaseEveryFrameCombatPlugin() {
    private val targets: MutableMap<ShipAPI, ShipAPI?> = mutableMapOf()

    operator fun get(index: ShipAPI): ShipAPI? = targets[index]

    /**
     * ShipAPI is inconsistent when returning maneuver target. It may return null
     * in some frames, even when the ship has a maneuver target. To avoid this problem,
     * last non-null maneuver target may be used.
     *
     * Even worse, when a ship is assigned an escort duty, maneuver target will always
     * be null. Then the autofire AI needs to drop the previous target, so it doesn't
     * get outdated.
     */
    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        targetTracker = this

        Global.getCombatEngine().ships.filter { it.isAlive }.forEach { ship ->
            val newTarget = ship.trueShipTarget
            val prevTarget = targets[ship]

            targets[ship] = when {
                newTarget == null && prevTarget != null && ship.hasEscortAssignment -> null
                newTarget != null -> newTarget
                prevTarget?.isValidTarget != true -> null
                else -> prevTarget
            }
        }
    }
}