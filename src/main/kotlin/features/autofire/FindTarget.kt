package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.genir.aitweaks.utils.extensions.isValidTarget
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

fun selectTarget(weapon: WeaponAPI, current: CombatEntityAPI?, maneuver: ShipAPI?): FiringSolution? {
    val trackMissiles = weapon.hasAIHint(PD) || weapon.hasAIHint(PD_ONLY) || weapon.hasAIHint(PD_ALSO)
    val trackFighters = trackMissiles && !weapon.hasAIHint(STRIKE)

    if (trackMissiles) {
        val missileSolution = selectMissile(weapon, current as? MissileAPI)
        if (missileSolution?.canTrack == true) return missileSolution
    }

    return selectShip(weapon, current as? ShipAPI, maneuver, trackFighters)
}

fun selectMissile(weapon: WeaponAPI, current: MissileAPI?): FiringSolution? {
    // Try tracking current missile.
    if (current?.isValidTarget == true) {
        val currentSolution = FiringSolution(weapon, current)
        if (currentSolution.canTrack) return currentSolution
    }

    // Find the closest enemy missile that can be tracked by the weapon.
    return closestMissileFinder(weapon.location, weapon.range, fun(missile: MissileAPI): FiringSolution? {
        if (missile.owner xor 1 != weapon.ship.owner) return null

        val solution = FiringSolution(weapon, missile)
        return if (solution.canTrack) solution else null
    })
}

fun selectShip(
    weapon: WeaponAPI, current: ShipAPI?, maneuver: ShipAPI?, trackFighters: Boolean
): FiringSolution? {
    // Prioritize maneuver target.
    val maneuverSolution = maneuver?.let { FiringSolution(weapon, it) }
    if (maneuverSolution?.canTrack == true) return maneuverSolution

    // Try tracking current target.
    if (current?.isValidTarget == true) {
        val currentSolution = FiringSolution(weapon, current)
        if (currentSolution.canTrack) return currentSolution
    }

    // Find the closest enemy ship that can be tracked by the weapon.
    return closestShipFinder(weapon.location, weapon.range, fun(ship: ShipAPI): FiringSolution? {
        if (!ship.isAlive || ship.owner xor 1 != weapon.ship.owner) return null
        if (ship.isFighter && !trackFighters) return null

        val solution = FiringSolution(weapon, ship)
        return if (solution.canTrack) solution else null
    })
}

fun firstAlongLineOfFire(hitSolver: HitSolver, range: Float): ShipAPI? =
    closestShipFinder(hitSolver.weapon.location, range) {
        when {
            it.isFighter -> null
            it.isDrone -> null
            it == hitSolver.weapon.ship -> null
            !hitSolver.willHit(it) -> null
            else -> it
        }
    }

fun <T> closestShipFinder(location: Vector2f, range: Float, f: (ShipAPI) -> T): T? {
    return closestCombatEntityFinder(location, range, Global.getCombatEngine().shipGrid) { f(it as ShipAPI) }
}

fun <T> closestMissileFinder(location: Vector2f, range: Float, f: (MissileAPI) -> T): T? {
    return closestCombatEntityFinder(location, range, Global.getCombatEngine().missileGrid) { f(it as MissileAPI) }
}

fun <T> closestCombatEntityFinder(
    location: Vector2f, range: Float, grid: CollisionGridAPI, f: (CombatEntityAPI) -> T
): T? {
    var closestFound: T? = null
    var closestRange = range * range + 1f

    val evaluateEntity = fun(entity: CombatEntityAPI) {
        val currentRange = (location - entity.location).lengthSquared()
        if (currentRange < closestRange) {
            val found = f(entity)
            if (found != null) {
                closestFound = found
                closestRange = currentRange
            }
        }
    }

    val searchRange = range * 2.0f + 50.0f // Magic numbers based on vanilla autofire AI.
    val entityIterator = grid.getCheckIterator(location, searchRange, searchRange)
    entityIterator.forEach { evaluateEntity(it as CombatEntityAPI) }

    return closestFound
}