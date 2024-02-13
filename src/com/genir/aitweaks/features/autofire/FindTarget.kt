package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.ANTI_FTR
import com.genir.aitweaks.features.autofire.extensions.*
import org.lwjgl.util.vector.Vector2f

fun selectTarget(
    weapon: WeaponAPI, current: CombatEntityAPI?, shipTarget: ShipAPI?, params: Params
): CombatEntityAPI? {
    if (Global.getCurrentState() == GameState.TITLE) return selectAsteroid(weapon, current, params)

    if (weapon.isPD) {
        selectMissile(weapon, current, params)?.let { return it }
        selectFighter(weapon, current, params)?.let { return it }
    }

    return selectShip(weapon, current, shipTarget, params)
}

/** Shoot asteroids only when the weapon and asteroid are both in viewport.
 * Otherwise, it looks weird on the title screen. */
fun selectAsteroid(weapon: WeaponAPI, current: CombatEntityAPI?, params: Params): CombatEntityAPI? {
    val inViewport = { location: Vector2f -> Global.getCombatEngine().viewport.isNearViewport(location, 0f) }
    if (!inViewport(weapon.location)) return null
    return selectEntity<CombatAsteroidAPI>(weapon, current, params, asteroidGrid()) { inViewport(it.location) }
}

fun selectMissile(w: WeaponAPI, current: CombatEntityAPI?, params: Params): CombatEntityAPI? =
    selectEntity<MissileAPI>(w, current, params, missileGrid()) { !it.isFlare || !w.ignoresFlares }

fun selectFighter(weapon: WeaponAPI, current: CombatEntityAPI?, params: Params): CombatEntityAPI? =
    selectEntity<ShipAPI>(weapon, current, params, shipGrid()) { it.isFighter }

fun selectShip(weapon: WeaponAPI, current: CombatEntityAPI?, shipTarget: ShipAPI?, params: Params): CombatEntityAPI? {
    // Prioritize ship target. Non-PD hardpoint weapons track only ships target.
    if (shipTarget?.isValidTarget == true && ((weapon.slot.isHardpoint && !weapon.isPD) || canTrack(
            weapon, Target(shipTarget), params
        ))
    ) return shipTarget

    return selectEntity<ShipAPI>(weapon, current, params, shipGrid()) { !it.isFighter || weapon.hasAIHint(ANTI_FTR) }
}

fun firstShipAlongLineOfFire(weapon: WeaponAPI, target: CombatEntityAPI, params: Params): Hit? =
    closestEntityFinder(weapon, shipGrid()) {
        when {
            it !is ShipAPI -> null
            it == target -> null
            it == weapon.ship -> null
            it.isFighter -> null
            it.isAlive && weapon.ship.root == it.root -> null

            it.owner == weapon.ship.owner -> analyzeAllyHit(weapon, it, params)
            it.isPhased -> null
            else -> analyzeHit(weapon, it, params)
        }
    }

private inline fun <reified T : CombatEntityAPI> selectEntity(
    weapon: WeaponAPI,
    current: CombatEntityAPI?,
    params: Params,
    grid: CollisionGridAPI,
    crossinline isAcceptableTarget: (T) -> Boolean
): CombatEntityAPI? {
    // Try tracking current target.
    if (current is T && isAcceptableTarget(current) && canTrack(weapon, Target(current), params)) return current

    // Find the closest enemy entity that can be tracked by the weapon.
    return closestEntityFinder(weapon, grid) {
        when {
            it !is T -> null
            it.owner == weapon.ship.owner -> null
            !it.isValidTarget -> null
            !isAcceptableTarget(it) -> null
            !canTrack(weapon, Target(it), params) -> null
            else -> Hit(it, closestHitRange(weapon, Target(it), params)!!, false)
        }
    }?.target
}

private fun closestEntityFinder(weapon: WeaponAPI, grid: CollisionGridAPI, f: (CombatEntityAPI) -> Hit?): Hit? {
    var closestRange = weapon.totalRange
    var closestHit: Hit? = null

    val forEachFn = fun(entity: CombatEntityAPI) {
        val hit = f(entity) ?: return
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

private fun asteroidGrid(): CollisionGridAPI = Global.getCombatEngine().asteroidGrid
