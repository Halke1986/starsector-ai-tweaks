package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.genir.aitweaks.utils.extensions.ignoresFlares
import com.genir.aitweaks.utils.extensions.isValidTarget
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

fun selectTarget(weapon: WeaponAPI, current: CombatEntityAPI?, maneuver: ShipAPI?): FiringSolution? {
    val trackMissiles = weapon.hasAIHint(PD) || weapon.hasAIHint(PD_ONLY) || weapon.hasAIHint(PD_ALSO)
    val trackFighters = trackMissiles && !weapon.hasAIHint(STRIKE)

    if (trackMissiles) {
        selectMissile(weapon, current as? MissileAPI)?.let { return it }
    }

    return selectShip(weapon, current as? ShipAPI, maneuver, trackFighters)
}

fun selectMissile(weapon: WeaponAPI, current: MissileAPI?): FiringSolution? {
    // Try tracking current missile.
    if (current?.isValidTarget == true) {
        trackingFiringSolution(weapon, current)?.let { return it }
    }

    // Find the closest enemy missile that can be tracked by the weapon.
    return closestMissileFinder(weapon.location, weapon.range) {
        when {
            it.owner == weapon.ship.owner -> null
            it.isFlare && weapon.ignoresFlares -> null
            else -> trackingFiringSolution(weapon, it)
        }
    }
}

fun selectShip(weapon: WeaponAPI, current: ShipAPI?, maneuver: ShipAPI?, trackFighters: Boolean): FiringSolution? {
    // Prioritize maneuver target.
    maneuver?.let { trackingFiringSolution(weapon, it) }?.let { return it }

    // Try tracking current target.
    if (current?.isValidTarget == true) {
        trackingFiringSolution(weapon, current)?.let { return it }
    }

    // Find the closest enemy ship that can be tracked by the weapon.
    return closestShipFinder(weapon.location, weapon.range) {
        when {
            !it.isAlive -> null
            it.isFighter && !trackFighters -> null
            it.owner == weapon.ship.owner -> null
            it.isPhased && !weapon.spec.isBeam -> null // only beams attack phased ships
            else -> trackingFiringSolution(weapon, it)
        }
    }
}

fun trackingFiringSolution(weapon: WeaponAPI, target: CombatEntityAPI): FiringSolution? =
    FiringSolution(weapon, target).let { if (it.canTrack) it else null }

fun firstAlongLineOfFire(hitSolver: HitSolver, range: Float): ShipAPI? =
    closestShipFinder(hitSolver.weapon.location, range) {
        when {
            it.isFighter -> null
            it.isDrone -> null
            it == hitSolver.weapon.ship -> null
            !hitSolver.willHit(it) -> null
            it.isHulk && !hitSolver.willHitBounds(it) -> null
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

    val searchRange = range * 2.0f
    val entityIterator = grid.getCheckIterator(location, searchRange, searchRange)
    entityIterator.forEach { evaluateEntity(it as CombatEntityAPI) }

    return closestFound
}