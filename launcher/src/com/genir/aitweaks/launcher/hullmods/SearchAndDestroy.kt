package com.genir.aitweaks.launcher.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI

class SearchAndDestroy : BaseHullMod() {
    override fun showInRefitScreenModPickerFor(ship: ShipAPI): Boolean {
        // This hullmod id DEPRECATED.
        return false
    }
}
