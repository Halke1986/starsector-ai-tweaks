package com.genir.aitweaks.launcher.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import lunalib.lunaSettings.LunaSettings

class SearchAndDestroy : BaseHullMod() {
    override fun showInRefitScreenModPickerFor(ship: ShipAPI): Boolean {
        return enableHullmods()
    }

    override fun isApplicableToShip(ship: ShipAPI): Boolean {
        return getUnapplicableReason(ship) == null
    }

    override fun getUnapplicableReason(ship: ShipAPI): String? {
        return when {
            LunaSettings.getBoolean("aitweaks", "aitweaks_fleetwide_search_destroy") == true -> {
                "When Fleetwide Search & Destroy is enabled in LunaLib settings, all player ship default to Search & Destroy assignment."
            }

            else -> {
                null
            }
        }
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? = when (index) {
        0 -> "Search and Destroy"
        else -> null
    }
}
