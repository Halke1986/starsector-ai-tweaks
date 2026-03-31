package com.genir.aitweaks.launcher.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.*
import com.genir.aitweaks.launcher.ShipAIPicker
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate
import lunalib.lunaSettings.LunaSettings

class CustomShipAI : BaseHullMod() {
    private val shipAIPicker: ShipAIPicker = coreLoader.loadClass("com.genir.aitweaks.core.shipai.ShipAIPicker").instantiate()

    override fun showInRefitScreenModPickerFor(ship: ShipAPI): Boolean {
        return enableHullmods()
    }

    /** Returns true if custom AI can control the given ship. */
    override fun isApplicableToShip(ship: ShipAPI): Boolean {
        return getUnapplicableReason(ship) == null
    }

    override fun getUnapplicableReason(ship: ShipAPI): String? {
        return when {
            // Is phase.
            ship.hullSpec.isPhase || ship.hullSpec.hints.contains(PHASE) || ship.shield?.type == ShieldAPI.ShieldType.PHASE -> {
                "Can not be installed on phase ships."
            }

            ship.hullSpec.hints.contains(CARRIER) && !ship.hullSpec.hints.contains(COMBAT) -> {
                "Can not be installed on non-combat carriers."
            }

            ship.isStation -> {
                "Can not be installed on stations."
            }

            // Is module.
            ship.stationSlot != null && ship.parentStation != null -> {
                "Can not be installed on modules."
            }

            ship.hullSpec.engineSpec.maxSpeed == 0f -> {
                "Can not be installed on modules."
            }

            ship.isFighter -> {
                "Can not be installed on fighters."
            }

            LunaSettings.getBoolean("aitweaks", "aitweaks_fleetwide_custom_ai") == true -> {
                "When Fleetwide Custom AI is enabled in LunaLib settings, all player ships-except carriers and phase ships-use the Custom AI by default."
            }

            else -> null
        }
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? = when (index) {
        0 -> "Work In Progress"
        else -> null
    }
}
