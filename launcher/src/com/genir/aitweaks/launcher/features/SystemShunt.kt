package com.genir.aitweaks.launcher.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand

/** Disable ship system, except for manually piloted ship. */
class SystemShunt : BaseHullMod() {
    override fun advanceInCombat(ship: ShipAPI, dt: Float) {
        if (ship != Global.getCombatEngine().playerShip || !Global.getCombatEngine().isUIAutopilotOn) {
            ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM)
        }
    }
}
