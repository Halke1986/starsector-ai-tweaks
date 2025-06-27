package com.genir.aitweaks.launcher.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.launcher.ShipAIPicker
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate

class CustomShipAI : BaseHullMod() {
    private val shipAIPicker: ShipAIPicker = coreLoader.loadClass("com.genir.aitweaks.core.shipai.ShipAIPicker").instantiate()

    /** Returns true if custom AI can control the given ship. */
    override fun showInRefitScreenModPickerFor(ship: ShipAPI): Boolean {
        return enableHullmods() && shipAIPicker.canHaveCustomAI(ship)
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? = when (index) {
        0 -> "Work In Progress"
        else -> null
    }
}
