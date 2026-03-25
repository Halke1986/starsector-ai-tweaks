package com.genir.aitweaks.launcher.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI

class Skirmisher : BaseHullMod() {
    override fun showInRefitScreenModPickerFor(ship: ShipAPI): Boolean {
        return enableHullmods()
    }

    override fun isApplicableToShip(ship: ShipAPI): Boolean {
        return when {
            ship.isStation -> false

            ship.stationSlot != null && ship.parentStation != null -> false

            ship.isFighter -> false

            ship.isFrigate -> false

            else -> true
        }
    }

    override fun getUnapplicableReason(ship: ShipAPI): String? {
        return when {
            ship.isStation -> "Can not be installed on stations."

            ship.stationSlot != null && ship.parentStation != null -> "Can not be installed on modules."

            ship.isFighter -> "Can not be installed on fighters."

            ship.isFrigate -> "Can not be installed on frigates, as they are always considered skirmishers."

            else -> null
        }
    }
}
