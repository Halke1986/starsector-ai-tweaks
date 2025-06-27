package com.genir.aitweaks.launcher.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI

class FinisherBeamProtocol : BaseHullMod() {
    override fun showInRefitScreenModPickerFor(ship: ShipAPI): Boolean {
        return enableHullmods()
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? = when (index) {
        0 -> "Tachyon Lance"
        1 -> "Phase Lance"
        2 -> "High Intensity Laser"
        3 -> "autofire"
        else -> null
    }
}
