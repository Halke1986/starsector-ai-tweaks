package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.utils.closestShipFilter

fun firstAlongLineOfFire(hitSolver: HitSolver, range: Float): ShipAPI? =
    closestShipFilter(hitSolver.weapon.location, range) {
        when {
            it.isFighter -> false
            it.isDrone -> false
            it == hitSolver.weapon.ship -> false
            !hitSolver.willHit(it) -> false
            else -> true
        }
    }



