package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.DamageType
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

fun avoidFriendlyFire(weapon: WeaponAPI, expected: Hit, actual: Hit?): Boolean {
    if (actual == null || actual.target !is ShipAPI) return fire

    val target = expected.target
    val phased = (target as? ShipAPI)?.isPhased == true
    val beam = weapon.isBeam || weapon.isBurstBeam
    val fragPD = weapon.spec.damageType == DamageType.FRAGMENTATION && weapon.isPD
    val firePassesTarget = ((!target.isShip && !beam) || phased) && !fragPD

    val blockerAheadOfTarget = actual.range < expected.range
    if (!firePassesTarget && !blockerAheadOfTarget) return fire

    val friendly = actual.target.owner == weapon.ship.owner
    return if (friendly || (actual.target.isInert && blockerAheadOfTarget)) holdFire
    else fire
}