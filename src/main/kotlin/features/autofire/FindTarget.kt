package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.features.autofire.extensions.*

fun selectTarget(weapon: WeaponAPI, current: CombatEntityAPI?, shipTarget: ShipAPI?): CombatEntityAPI? {
    if (weapon.isPD) selectMissile(weapon, current as? MissileAPI)?.let { return it }
    return selectShip(weapon, current as? ShipAPI, shipTarget)
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

fun selectShip(weapon: WeaponAPI, current: ShipAPI?, shipTarget: ShipAPI?): CombatEntityAPI? {
    // Prioritize ship target. Non-PD hardpoint weapons track only ships target.
    if (shipTarget?.isAlive == true && ((weapon.slot.isHardpoint && !weapon.isPD) || canTrack(
            weapon, Target(shipTarget)
        ))
    ) return shipTarget

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
            weapon.ship.isStationModule && it.isAlive && (weapon.ship.parentStation.let { p -> p == it || p == it.parentStation }) -> null

            it.owner == weapon.ship.owner -> analyzeAllyHit(weapon, it)
            it.isPhased -> null
            else -> analyzeHit(weapon, it, rangeLimit)
        }
    }

data class Hit(val target: CombatEntityAPI, val range: Float, val shieldHit: Boolean) {
    override fun toString(): String {
        val name = when {
            target is MissileAPI -> "missile"
            (target as ShipAPI).isHulk -> "hulk"
            target.name == null -> target.hullSpec.hullId
            else -> target.name
        }

        return "($name $range $shieldHit)"
    }
}

/** Analyzes the potential collision between projectile and target. Null if no collision. */
fun analyzeHit(weapon: WeaponAPI, target: CombatEntityAPI, rangeLimit: Float): Hit? {
    val targetCircumference = if (hasShield(target)) targetShield(target as ShipAPI) else Target(target)
    val range = willHitCircumference(weapon, targetCircumference) ?: return null
    if (range > rangeLimit) return null

    // Simple circumference collision is enough for missiles and fighters.
    if (!target.isShip) return Hit(target, range, false)

    // Check shield hit.
    if (hasShield(target)) {
        val shieldRange = willHitShield(weapon, target as ShipAPI)
        if (shieldRange != null) return if (shieldRange <= rangeLimit) Hit(target, shieldRange, true) else null
    }

    // Check bounds hit.
    val boundsRange = willHitBounds(weapon, target as ShipAPI)
    return if (boundsRange != null && boundsRange <= rangeLimit) Hit(target, boundsRange, false) else null
}

fun analyzeAllyHit(weapon: WeaponAPI, ally: ShipAPI): Hit? = when {
    weapon.projectileCollisionClass == CollisionClass.PROJECTILE_FIGHTER -> null
    weapon.projectileCollisionClass == CollisionClass.RAY_FIGHTER -> null
    !willHitShieldCautious(weapon, ally) -> null
    else -> Hit(ally, closestHitRange(weapon, targetShield(ally))!!, false)
}

/** Workaround for hulks retaining outdated ShieldAPI */
fun hasShield(target: CombatEntityAPI): Boolean = target.isShip && !(target as ShipAPI).isHulk

@Suppress("UNCHECKED_CAST")
private fun <T> closestEntityFinder(weapon: WeaponAPI, grid: CollisionGridAPI, f: (T, Float) -> Hit?): Hit? {
    var closestRange = weapon.totalRange
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

