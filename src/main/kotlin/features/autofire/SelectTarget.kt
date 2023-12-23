package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.closestShipFilter
import com.genir.aitweaks.utils.extensions.isValidTarget

fun selectTarget(weapon: WeaponAPI, current: CombatEntityAPI?, maneuver: ShipAPI?): FiringSolution? {
    // Prioritize maneuver target.
    val maneuverSolution = maneuver?.let { FiringSolution(weapon, it) }
    if (maneuverSolution?.canTrack == true) return maneuverSolution

    // Try tracking current target, if it's still valid.
    if (current != null && current is MissileAPI || (current as? ShipAPI).isValidTarget) {
        val currentSolution = FiringSolution(weapon, current!!)
        if (currentSolution.canTrack) return currentSolution
    }

    // Find the closest enemy ship that can be tracked by the weapon.
    var newSolution: FiringSolution? = null
    val evaluateShip = fun(ship: ShipAPI): Boolean {
        if (ship.isFighter || ship.isDrone || !ship.isAlive || ship.owner xor 1 != weapon.ship.owner) return false

        val shipSolution = FiringSolution(weapon, ship)
        if (shipSolution.canTrack) {
            newSolution = shipSolution
            return true
        }

        return false
    }

    closestShipFilter(weapon.location, weapon.range, evaluateShip)
    return newSolution
}

fun selectMissile(weapon: WeaponAPI, current: CombatEntityAPI?): FiringSolution? {

    // Try tracking current target, if it's still valid.
    if (current != null && current is MissileAPI || (current as? ShipAPI).isValidTarget) {
        val currentSolution = FiringSolution(weapon, current!!)
        if (currentSolution.canTrack) return currentSolution
    }

    // Find the closest enemy ship that can be tracked by the weapon.
    var newSolution: FiringSolution? = null
    val evaluateShip = fun(ship: ShipAPI): Boolean {
        if (ship.isFighter || ship.isDrone || !ship.isAlive || ship.owner xor 1 != weapon.ship.owner) return false

        val shipSolution = FiringSolution(weapon, ship)
        if (shipSolution.canTrack) {
            newSolution = shipSolution
            return true
        }

        return false
    }

    closestShipFilter(weapon.location, weapon.range, evaluateShip)
    return newSolution
}