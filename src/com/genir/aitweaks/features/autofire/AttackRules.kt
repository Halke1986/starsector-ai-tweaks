package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.features.autofire.extensions.*
import com.genir.aitweaks.utils.firingCycle
import com.genir.aitweaks.utils.shieldUptime
import kotlin.math.min

val fire = null

enum class HoldFire {
    AVOID_PHASED, AVOID_SHIELDS, AVOID_EXPOSED_HULL, AVOID_MISSING_HULL, AVOID_FF, NO_TARGET, STABILIZE_ON_TARGET, NO_HIT_EXPECTED, OUT_OF_RANGE, FORCE_OFF,
}

fun avoidPhased(weapon: WeaponAPI, hit: Hit): HoldFire? = when {
    (hit.target as? ShipAPI)?.isPhased != true -> fire

    weapon.conserveAmmo -> HoldFire.AVOID_PHASED
    weapon.isPD -> fire
    weapon.isBeam -> fire

    else -> HoldFire.AVOID_PHASED
}

fun avoidWrongDamageType(weapon: WeaponAPI, hit: Hit, params: Params): HoldFire? = when {
    !hit.target.isShip -> fire
    weapon.isStrictlyAntiShield -> avoidExposedHull(weapon, hit)
    weapon.hasAIHint(WeaponAPI.AIHints.USE_LESS_VS_SHIELDS) -> avoidShields(weapon, hit) ?: aimAtHull(
        weapon, hit, params
    )

    weapon.damageType == DamageType.HIGH_EXPLOSIVE -> aimAtHull(weapon, hit, params)
    else -> fire
}

/** Ensure projectile will not hit shields. */
fun avoidShields(weapon: WeaponAPI, hit: Hit): HoldFire? = when {
    !hit.target.isShip -> fire
    hit.shieldHit && weapon.conserveAmmo -> HoldFire.AVOID_SHIELDS
    hit.shieldHit && shieldUptime(hit.target.shield) > 0.8f -> HoldFire.AVOID_SHIELDS
    else -> fire
}

/** Ensure projectile will not hit exposed hull. */
fun avoidExposedHull(weapon: WeaponAPI, hit: Hit): HoldFire? = when {
    !hit.target.isShip -> fire
    !hit.shieldHit -> HoldFire.AVOID_EXPOSED_HULL
    shieldUptime(hit.target.shield) < min(
        0.8f, firingCycle(weapon).duration
    ) -> HoldFire.AVOID_EXPOSED_HULL // avoid shield flicker
    else -> fire
}

/** Ensure projectile will hit hull. Shields are ignored. */
fun aimAtHull(weapon: WeaponAPI, hit: Hit, params: Params): HoldFire? = when {
    !hit.target.isShip -> fire
    !hit.shieldHit -> fire // hit on hull was already predicted
    willHitBounds(
        weapon, hit.target as ShipAPI, params
    ).let { it == null || it > weapon.totalRange } -> HoldFire.AVOID_MISSING_HULL // ensure hull is in range, underneath the shields
    else -> fire
}

fun avoidFriendlyFire(weapon: WeaponAPI, expected: Hit, actual: Hit?): HoldFire? {
    if (actual == null || actual.target !is ShipAPI) return fire

    val target = expected.target
    val phased = (target as? ShipAPI)?.isPhased == true
    val beam = weapon.isBeam || weapon.isBurstBeam
    val fragPD = weapon.spec.damageType == DamageType.FRAGMENTATION && weapon.isPD
    val firePassesTarget = ((!target.isShip && !beam) || phased) && !fragPD

    val blockerAheadOfTarget = actual.range < expected.range
    if (!firePassesTarget && !blockerAheadOfTarget) return fire

    val friendly = actual.target.owner == weapon.ship.owner
    return if (friendly || (actual.target.isInert && blockerAheadOfTarget)) HoldFire.AVOID_FF
    else fire
}