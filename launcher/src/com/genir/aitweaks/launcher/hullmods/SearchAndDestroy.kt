package com.genir.aitweaks.launcher.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI

class SearchAndDestroy : BaseHullMod() {
    override fun showInRefitScreenModPickerFor(ship: ShipAPI): Boolean {
        return enableHullmods()
    }

    override fun isApplicableToShip(ship: ShipAPI): Boolean {
        return !ship.variant.hasHullMod("aitweaks_custom_ship_ai")
    }

    override fun getUnapplicableReason(ship: ShipAPI): String? {
        return when {
            ship.variant.hasHullMod("aitweaks_custom_ship_ai") -> "Ships with Custom AI always default to Search And Destroy."

            else -> null
        }
    }
}
