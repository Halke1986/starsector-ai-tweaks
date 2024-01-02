package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.utils.extensions.isPD
import com.genir.aitweaks.utils.shieldUptime

fun avoidPhased(weapon: WeaponAPI, target: ShipAPI?): Boolean = when {
    target == null -> false
    !target.isPhased -> false
    weapon.isBurstBeam -> true
    weapon.usesAmmo() -> true
    weapon.isPD -> false
    weapon.isBeam -> false
    else -> true
}

fun avoidShields(weapon: WeaponAPI, target: ShipAPI?): Boolean = when {
    !weapon.hasAIHint(WeaponAPI.AIHints.USE_LESS_VS_SHIELDS) -> false
    target == null -> false
    target.isFighter -> false
    weapon.isBurstBeam -> true
    weapon.usesAmmo() -> true
    else -> shieldUptime(target.shield) > 0.8f
}

fun avoidExposedHull(weapon: WeaponAPI, target: ShipAPI?): Boolean = when {
    weapon.spec.primaryRoleStr != "Strictly Anti Shield" -> false
    target == null -> false
    target.isFighter -> false
    else -> shieldUptime(target.shield) < 0.8f
}

fun avoidFriendlyFire(weapon: WeaponAPI, target: CombatEntityAPI, hitRange: Float): Boolean {
    val missile = target is MissileAPI
    val fighter = (target as? ShipAPI)?.isFighter == true
    val phased = (target as? ShipAPI)?.isPhased == true
    val beam = weapon.isBeam || weapon.isBurstBeam
    val fragPD = weapon.spec.damageType == DamageType.FRAGMENTATION && weapon.isPD
    val firePassesTarget = (((missile || fighter) && !beam) || phased) && !fragPD

    // Search for blockers behind target only for attacks that are
    // predicted to pass through the target and be dangerous to friendlies.
    val searchRange = if (firePassesTarget) weapon.range else hitRange
    val blocker = firstAlongLineOfFire(weapon, target, searchRange) ?: return false

    val blockerAheadOfTarget = closestHitRange(weapon, blocker)?.let { it < hitRange } ?: false
    val friendly = blocker.owner == weapon.ship.owner

    return friendly || (blocker.isHulk && blockerAheadOfTarget)
}