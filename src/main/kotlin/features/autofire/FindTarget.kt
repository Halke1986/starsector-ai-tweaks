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
    return closestEntityFinder<MissileAPI>(weapon, weapon.range, missileGrid()) {
        when {
            it.owner == weapon.ship.owner -> false
            it.isFlare && weapon.ignoresFlares -> false
            !canTrack(weapon, Target(it)) -> false
            else -> true
        }
    }
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
    return closestEntityFinder<ShipAPI>(weapon, weapon.range, shipGrid()) {
        when {
            it.isInert -> false
            it.owner == weapon.ship.owner -> false
            it.isFighter && !weapon.isAntiFtr -> false
            !canTrack(weapon, Target(it)) -> false
            else -> true
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> closestEntityFinder(weapon: WeaponAPI, range: Float, grid: CollisionGridAPI, f: (T) -> Boolean): T? {
    var closestEntity: CombatEntityAPI? = null
    var upperBound = range * range + 1f

    val evaluateEntity = fun(entity: CombatEntityAPI) {
        val entityRange = closestHitRange(weapon, Target(entity)) ?: return
        if (entityRange < upperBound && f(entity as T)) {
            closestEntity = entity
            upperBound = entityRange
        }
    }

    val searchRange = range * 2.0f
    val entityIterator = grid.getCheckIterator(weapon.location, searchRange, searchRange)
    entityIterator.forEach { evaluateEntity(it as CombatEntityAPI) }

    return closestEntity as T
}

private fun shipGrid(): CollisionGridAPI = Global.getCombatEngine().shipGrid

private fun missileGrid(): CollisionGridAPI = Global.getCombatEngine().missileGrid

data class Hit(val target: CombatEntityAPI, val range: Float, val shieldHit: Boolean) {
    override fun toString(): String {
        val name = when {
            target is MissileAPI -> "missile"
            (target as ShipAPI).isHulk -> "hulk"
            else -> target.name
        }

        return "($name $range $shieldHit)"
    }
}

fun firstAlongLineOfFire2(weapon: WeaponAPI, target: ShipAPI): Hit? = when {
    target == weapon.ship -> null
    target.isFighter -> null
    weapon.ship.isStationModule && target.isAlive && (target.isStation || target.isStationModule) -> null

    target.owner == weapon.ship.owner -> willHitShieldCautious(weapon, target).let {
        if (it) Hit(
            target, closestHitRange(weapon, Target(target))!!, true
        ) else null
    }

    target.isPhased -> null
    else -> analyzeHit(weapon, target)
}

fun closestEntityFinder2(weapon: WeaponAPI, range: Float): Hit? {
    var closestHit: Hit? = null
    var upperBound = range * range + 1f

    val evaluateEntity = fun(ship: ShipAPI) {
        val hit = firstAlongLineOfFire2(weapon, ship) ?: return
        if (hit.range < upperBound) {
            closestHit = hit
            upperBound = hit.range
        }
    }

    val searchRange = range * 2.0f
    val entityIterator = shipGrid().getCheckIterator(weapon.location, searchRange, searchRange)
    entityIterator.forEach { evaluateEntity(it as ShipAPI) }

    return closestHit
}