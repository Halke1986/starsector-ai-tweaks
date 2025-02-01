package com.genir.aitweaks.launcher.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.CARRIER
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.COMBAT

class CustomShipAI : BaseHullMod() {
    /** Returns true is custom AI can control the given ship. */
    override fun showInRefitScreenModPickerFor(ship: ShipAPI): Boolean {
        return canHaveCustomAI(ship)
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? = when (index) {
        0 -> "Work In Progress"
        else -> null
    }

    companion object {
        /** Returns true is custom AI can control the given ship.
         * NOTE: this code needs to be synchronized with a copy in CustomAIManager. */
        fun canHaveCustomAI(ship: ShipAPI): Boolean {
            return when {
                ship.owner == 0 && Global.getSettings().modManager.isModEnabled("aitweaksunlock") -> true

                ship.hullSpec.isPhase -> false
                ship.hullSpec.hints.contains(CARRIER) && !ship.hullSpec.hints.contains(COMBAT) -> false
                ship.isStation -> false
                ship.stationSlot != null && ship.parentStation != null -> false // isModule
                ship.isFrigate -> false
                ship.isFighter -> false

                else -> true
            }
        }
    }
}
