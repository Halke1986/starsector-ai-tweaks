package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.utils.extensions.hasEscortAssignment
import com.genir.aitweaks.utils.extensions.isValidTarget
import com.genir.aitweaks.utils.extensions.trueShipTarget

class ShipTargetTracker(private val ship: ShipAPI) {
    var target: ShipAPI? = null

    /**
     * ShipAPI is inconsistent when returning maneuver target. It may return null
     * in some frames, even when the ship has a maneuver target. To avoid this problem,
     * last non-null maneuver target may be used.
     *
     * Even worse, when a ship is assigned an escort duty, maneuver target will always
     * be null. Then the autofire AI needs to drop the previous target, so it doesn't
     * get outdated.
     */
    fun advance() {
        val newTarget = ship.trueShipTarget

        target = when {
            newTarget == null && target != null && ship.hasEscortAssignment -> null
            newTarget != null -> newTarget
            target?.isValidTarget != true -> null
            else -> target
        }
    }
}


