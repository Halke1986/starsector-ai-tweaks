package com.genir.aitweaks.launcher.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import lunalib.lunaSettings.LunaSettings

class SearchAndDestroy : BaseHullMod() {
    override fun showInRefitScreenModPickerFor(ship: ShipAPI): Boolean {
        return enableHullmods()
    }

    override fun isApplicableToShip(ship: ShipAPI): Boolean {
        return !fleetwideEanbled()
    }

    override fun getUnapplicableReason(ship: ShipAPI): String? {
        return when {
            fleetwideEanbled() -> "Fleetwide Search & Destroy is selected in LunaLib settings. All ship default to Search & Destroy assignment."

            else -> null
        }
    }

    private fun fleetwideEanbled(): Boolean {
        return LunaSettings.getBoolean("aitweaks", "aitweaks_fleetwide_search_destroy") == true
    }
}
