package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.utils.extensions.ignoresFlares
import com.genir.aitweaks.utils.extensions.isPD
import com.genir.aitweaks.utils.extensions.isValidTarget
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

fun selectTarget(weapon: WeaponAPI, current: CombatEntityAPI?, maneuver: ShipAPI?): CombatEntityAPI? {
    if (weapon.isPD) selectMissile(weapon, current as? MissileAPI)?.let { return it }
    return selectShip(weapon, current as? ShipAPI, maneuver)
}

fun selectMissile(weapon: WeaponAPI, current: MissileAPI?): CombatEntityAPI? {
    // Try tracking current missile.
    if (current?.isValidTarget == true && canTrack(weapon, current)) return current

    // Find the closest enemy missile that can be tracked by the weapon.
    return closestEntityFinder<MissileAPI>(weapon.location, weapon.range, missileGrid()) {
        when {
            it.owner == weapon.ship.owner -> false
            it.isFlare && weapon.ignoresFlares -> false
            !canTrack(weapon, it) -> false
            else -> true
        }
    }
}

fun selectShip(weapon: WeaponAPI, current: ShipAPI?, maneuver: ShipAPI?): CombatEntityAPI? {
    // Prioritize maneuver target.
    if (maneuver?.isAlive == true && canTrack(weapon, maneuver)) return maneuver

    // Try tracking current target.
    if (current?.isAlive == true && canTrack(weapon, current)) return current

    // Find the closest enemy ship that can be tracked by the weapon.
    return closestEntityFinder<ShipAPI>(weapon.location, weapon.range, shipGrid()) {
        when {
            !it.isAlive -> false
            it.owner == weapon.ship.owner -> false
            it.isFighter && !weapon.isPD -> false
            !canTrack(weapon, it) -> false
            else -> true
        }
    }
}

fun firstAlongLineOfFire(weapon: WeaponAPI, maxRange: Float): ShipAPI? {
    return closestEntityFinder<ShipAPI>(weapon.location, maxRange, shipGrid()) {
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

fun <T> closestEntityFinder(location: Vector2f, maxRange: Float, grid: CollisionGridAPI, f: (T) -> Boolean): T? {
    var blocker: CombatEntityAPI? = null
    var upperBound = maxRange * maxRange + 1f

    val evaluateEntity = fun(entity: CombatEntityAPI) {
        val currentRange = (location - entity.location).lengthSquared()
        if (currentRange < upperBound) {
            if (f(entity as T)) {
                blocker = entity
                upperBound = currentRange
            }
        }
    }

    val searchRange = maxRange * 2.0f
    val entityIterator = grid.getCheckIterator(location, searchRange, searchRange)
    entityIterator.forEach { evaluateEntity(it as CombatEntityAPI) }

    return blocker as T
}

fun shipGrid(): CollisionGridAPI = Global.getCombatEngine().shipGrid

fun missileGrid(): CollisionGridAPI = Global.getCombatEngine().missileGrid