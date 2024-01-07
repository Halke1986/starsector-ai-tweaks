package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.extensions.conserveAmmo
import com.genir.aitweaks.utils.extensions.isInert
import com.genir.aitweaks.utils.extensions.isPD
import com.genir.aitweaks.utils.extensions.isShip
import com.genir.aitweaks.utils.shieldUptime

const val holdFire = false
const val fire = true
const val notApplicable = fire

fun avoidPhased(weapon: WeaponAPI, hit: Hit): Boolean = when {
    (hit.target as? ShipAPI)?.isPhased != true -> fire

    weapon.conserveAmmo -> holdFire
    weapon.isPD -> fire
    weapon.isBeam -> fire

    else -> holdFire
}

fun avoidShields(weapon: WeaponAPI, hit: Hit): Boolean = when {
    !weapon.hasAIHint(WeaponAPI.AIHints.USE_LESS_VS_SHIELDS) -> notApplicable
    !hit.target.isShip -> holdFire

    hit.shieldHit && weapon.conserveAmmo -> holdFire // weapons strict about saving ammo
    hit.shieldHit && shieldUptime(hit.target.shield) > 0.8f -> holdFire // attack when shields flicker
    willHitBounds(weapon, hit.target as ShipAPI) == null -> holdFire // ensure hull is in range, not just shields

    else -> fire
}

fun avoidExposedHull(weapon: WeaponAPI, hit: Hit): Boolean = when {
    weapon.spec.primaryRoleStr != "Strictly Anti Shield" -> notApplicable
    !hit.target.isShip -> holdFire

    !hit.shieldHit || shieldUptime(hit.target.shield) < 0.8f -> holdFire // avoid shield flicker

    else -> fire
}

fun avoidWastingTorpedo(weapon: WeaponAPI, hit: Hit): Boolean = when {
    weapon.spec.primaryRoleStr != "Torpedo" -> notApplicable
    weapon.damageType != DamageType.HIGH_EXPLOSIVE -> notApplicable
    !hit.target.isShip -> holdFire

    hit.shieldHit -> holdFire
    (hit.target as ShipAPI).hullLevel < 0.15f -> holdFire // avoid almost dead ships
    hit.target.hullSpec.armorRating < 750f -> holdFire // avoid soft targets
    hit.target.phaseCloak != null -> holdFire // avoid phase targets
    willHitBounds(weapon, hit.target) == null -> holdFire // ensure hull is in range, not just shields

    else -> fire
}

//fun avoidFriendlyFire(weapon: WeaponAPI, target: CombatEntityAPI, hitRange: Float): Boolean {
//    val missile = target is MissileAPI
//    val fighter = (target as? ShipAPI)?.isFighter == true
//    val phased = (target as? ShipAPI)?.isPhased == true
//    val beam = weapon.isBeam || weapon.isBurstBeam
//    val fragPD = weapon.spec.damageType == DamageType.FRAGMENTATION && weapon.isPD
//    val firePassesTarget = (((missile || fighter) && !beam) || phased) && !fragPD
//
//    // Search for blockers behind target only for attacks that are
//    // predicted to pass through the target and be dangerous to friendlies.
//    val searchRange = if (firePassesTarget) weapon.range else hitRange
//    val blocker = firstAlongLineOfFire(weapon, target, searchRange) ?: return fire
//
//    val blockerAheadOfTarget = closestHitRange(weapon, Target(blocker))?.let { it < hitRange } ?: fire
//    val friendly = blocker.owner == weapon.ship.owner
//
//    return if (friendly || (blocker.isInert && blockerAheadOfTarget)) holdFire
//    else fire
//}

fun avoidFriendlyFire1(weapon: WeaponAPI, expected: Hit, actual: Hit): Boolean {
    if (actual.target !is ShipAPI) return fire

    val target = expected.target
    val missile = target is MissileAPI
    val fighter = (target as? ShipAPI)?.isFighter == true
    val phased = (target as? ShipAPI)?.isPhased == true
    val beam = weapon.isBeam || weapon.isBurstBeam
    val fragPD = weapon.spec.damageType == DamageType.FRAGMENTATION && weapon.isPD
    val firePassesTarget = (((missile || fighter) && !beam) || phased) && !fragPD
    val blockerAheadOfTarget = actual.range < expected.range

    // Search for blockers behind target only for attacks that are
    // predicted to pass through the target and be dangerous to friendlies.

    if (!firePassesTarget && !blockerAheadOfTarget) return fire

    val friendly = actual.target.owner == weapon.ship.owner

    return if (friendly || (actual.target.isInert && blockerAheadOfTarget)) holdFire
    else fire
}