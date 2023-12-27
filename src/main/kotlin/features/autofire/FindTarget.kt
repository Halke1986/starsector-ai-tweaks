package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.genir.aitweaks.utils.extensions.ignoresFlares
import com.genir.aitweaks.utils.extensions.isValidTarget
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

fun selectTarget(weapon: WeaponAPI, current: CombatEntityAPI?, maneuver: ShipAPI?): CombatEntityAPI? {
    val trackMissiles = weapon.hasAIHint(PD) || weapon.hasAIHint(PD_ONLY) || weapon.hasAIHint(PD_ALSO)
    val trackFighters = trackMissiles && !weapon.hasAIHint(STRIKE)

    if (trackMissiles) {
        selectMissile(weapon, current as? MissileAPI)?.let { return it }
    }

    return selectShip(weapon, current as? ShipAPI, maneuver, trackFighters)
}

fun selectMissile(weapon: WeaponAPI, current: MissileAPI?): CombatEntityAPI? {
    // Try tracking current missile.
    if (current?.isValidTarget == true && canTrack(weapon, current)) return current

    // Find the closest enemy missile that can be tracked by the weapon.
    return closestMissileFinder(weapon.location, weapon.range) {
        when {
            !it.isValidTarget -> false
            it.owner == weapon.ship.owner -> false
            it.isFlare && weapon.ignoresFlares -> false
            !canTrack(weapon, it) -> false
            else -> true
        }
    }
}

fun selectShip(weapon: WeaponAPI, current: ShipAPI?, maneuver: ShipAPI?, trackFighters: Boolean): CombatEntityAPI? {
    // Prioritize maneuver target.
    if (maneuver?.isValidTarget == true && canTrack(weapon, maneuver)) return maneuver

    // Try tracking current target.
    if (current?.isValidTarget == true && canTrack(weapon, current)) return current

    // Find the closest enemy ship that can be tracked by the weapon.
    return closestShipFinder(weapon.location, weapon.range) {
        when {
            !it.isValidTarget -> false
            it.owner == weapon.ship.owner -> false
            it.isFighter && !trackFighters -> false
            !canTrack(weapon, it) -> false
            else -> true
        }
    }
}

fun firstAlongLineOfFire(weapon: WeaponAPI, range: Float): ShipAPI? {
    return closestShipFinder(weapon.location, range) {
        when {
            it.isFighter -> false
            it.isDrone -> false
            it == weapon.ship -> false
            it.isHulk && !willHitBounds(weapon, it) -> false
            it.owner == weapon.ship.owner && !willSpreadHit(weapon, it) -> false
            it.owner != weapon.ship.owner && !willHit(weapon, it) -> false
            else -> true
        }
    }
}

fun closestShipFinder(location: Vector2f, range: Float, f: (ShipAPI) -> Boolean): ShipAPI? {
    return closestCombatEntityFinder(location, range, Global.getCombatEngine().shipGrid, f)
}

fun closestMissileFinder(location: Vector2f, range: Float, f: (MissileAPI) -> Boolean): MissileAPI? {
    return closestCombatEntityFinder(location, range, Global.getCombatEngine().missileGrid, f)
}

fun <T> closestCombatEntityFinder(location: Vector2f, range: Float, grid: CollisionGridAPI, f: (T) -> Boolean): T? {
    var closestFound: CombatEntityAPI? = null
    var closestRange = range * range + 1f

    val evaluateEntity = fun(entity: CombatEntityAPI) {
        val currentRange = (location - entity.location).lengthSquared()
        if (currentRange < closestRange) {
            if (f(entity as T)) {
                closestFound = entity
                closestRange = currentRange
            }
        }
    }

    val searchRange = range * 2.0f
    val entityIterator = grid.getCheckIterator(location, searchRange, searchRange)
    entityIterator.forEach { evaluateEntity(it as CombatEntityAPI) }

    return closestFound as T
}