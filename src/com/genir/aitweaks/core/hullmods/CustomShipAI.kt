package com.genir.aitweaks.core.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.CustomAIManager

class CustomShipAI : BaseHullMod() {
    override fun showInRefitScreenModPickerFor(ship: ShipAPI): Boolean {
        return CustomAIManager().canHaveCustomAI(ship)
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? = when (index) {
        0 -> "Work In Progress"
        else -> null
    }
}
