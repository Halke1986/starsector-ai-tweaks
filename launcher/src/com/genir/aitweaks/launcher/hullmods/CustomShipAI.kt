package com.genir.aitweaks.launcher.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.launcher.ShipAIPicker
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate

class CustomShipAI : BaseHullMod() {
    private val shipAIPicker: ShipAIPicker = coreLoader.loadClass("com.genir.aitweaks.core.shipai.ShipAIPicker").instantiate()

    override fun showInRefitScreenModPickerFor(ship: ShipAPI): Boolean {
        return enableHullmods()
    }

    /** Returns true if custom AI can control the given ship. */
    override fun isApplicableToShip(ship: ShipAPI): Boolean {
        return shipAIPicker.canHaveCustomAI(ship)
    }

    override fun getUnapplicableReason(ship: ShipAPI): String? {
        return shipAIPicker.getUnapplicableReason(ship)
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? = when (index) {
        0 -> "Work In Progress"
        1 -> "Search and Destroy"
        else -> null
    }
}
