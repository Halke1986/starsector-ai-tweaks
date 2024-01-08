package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.utils.extensions.*

fun selectTarget(weapon: WeaponAPI, current: CombatEntityAPI?, maneuver: ShipAPI?): CombatEntityAPI? {
    if (weapon.isPD) selectMissile(weapon, current as? MissileAPI)?.let { return it }
    return selectShip(weapon, current as? ShipAPI, maneuver)
}

fun selectMissile(weapon: WeaponAPI, current: MissileAPI?): CombatEntityAPI? {
    // Try tracking current missile.
    if (current?.isValidTarget == true && canTrack(weapon, Target(current))) return current

    // Find the closest enemy missile that can be tracked by the weapon.
    return closestEntityFinder<MissileAPI>(weapon, missileGrid()) { it, _ ->
        when {
            it.owner == weapon.ship.owner -> null
            it.isFlare && weapon.ignoresFlares -> null
            !canTrack(weapon, Target(it)) -> null
            else -> Hit(it, closestHitRange(weapon, Target(it))!!, false)
        }
    }?.target
}

fun selectShip(weapon: WeaponAPI, current: ShipAPI?, maneuver: ShipAPI?): CombatEntityAPI? {
    // Prioritize maneuver target. Non-PD hardpoint weapons track only ships target.
    if (maneuver?.isAlive == true && ((weapon.slot.isHardpoint && !weapon.isPD) || canTrack(
            weapon, Target(maneuver)
        ))
    ) return maneuver

    // Try tracking current target.
    if (current?.isAlive == true && canTrack(weapon, Target(current))) return current

    // Find the closest enemy ship that can be tracked by the weapon.
    return closestEntityFinder<ShipAPI>(weapon, shipGrid()) { it, _ ->
        when {
            it.isInert -> null
            it.owner == weapon.ship.owner -> null
            it.isFighter && !weapon.isAntiFtr -> null
            !canTrack(weapon, Target(it)) -> null
            else -> Hit(it, closestHitRange(weapon, Target(it))!!, false)
        }
    }?.target
}

fun firstShipAlongLineOfFire(weapon: WeaponAPI, target: CombatEntityAPI): Hit? =
    closestEntityFinder<ShipAPI>(weapon, shipGrid()) { it, rangeLimit ->
        when {
            it == target -> null
            it == weapon.ship -> null
            it.isFighter -> null
            weapon.ship.isStationModule && it.isAlive && (it.isStation || it.isStationModule) -> null

            // Handle friendlies.
            it.owner == weapon.ship.owner -> if (willHitShieldCautious(weapon, it)) Hit(
                it, closestHitRange(weapon, Target(it))!!, true
            ) else null

            it.isPhased -> null
            else -> analyzeHit(weapon, it, rangeLimit)
        }
    }

@Suppress("UNCHECKED_CAST")
private fun <T> closestEntityFinder(weapon: WeaponAPI, grid: CollisionGridAPI, f: (T, Float) -> Hit?): Hit? {
    var closestRange = weapon.range
    var closestHit: Hit? = null

    val forEachFn = fun(entity: CombatEntityAPI) {
        val hit = f(entity as T, closestRange) ?: return
        if (hit.range < closestRange) {
            closestRange = hit.range
            closestHit = hit
        }
    }

    val searchRange = closestRange * 2.0f
    val entityIterator = grid.getCheckIterator(weapon.location, searchRange, searchRange)
    entityIterator.forEach { forEachFn(it as CombatEntityAPI) }

    return closestHit
}

private fun shipGrid(): CollisionGridAPI = Global.getCombatEngine().shipGrid

private fun missileGrid(): CollisionGridAPI = Global.getCombatEngine().missileGrid
