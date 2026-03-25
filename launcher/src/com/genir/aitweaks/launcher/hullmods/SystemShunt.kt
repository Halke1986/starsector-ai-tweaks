package com.genir.aitweaks.launcher.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand

/** Disable ship system, except for manually piloted ship. */
class SystemShunt : BaseHullMod() {
    override fun showInRefitScreenModPickerFor(ship: ShipAPI): Boolean {
        return enableHullmods()
    }

    override fun isApplicableToShip(ship: ShipAPI): Boolean {
        return ship.system != null
    }

    override fun getUnapplicableReason(ship: ShipAPI): String? {
        return when {
            ship.system == null -> "Can not be installed on ships with no system."

            else -> null
        }
    }

    override fun advanceInCombat(ship: ShipAPI, dt: Float) {
        if (ship != Global.getCombatEngine().playerShip || !Global.getCombatEngine().isUIAutopilotOn) {
            ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM)
        }
    }
}
