package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.DamageType.FRAGMENTATION
import com.fs.starfarer.api.combat.DamageType.HIGH_EXPLOSIVE
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.ANTI_FTR
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.USE_LESS_VS_SHIELDS
import com.genir.aitweaks.utils.extensions.*
import com.genir.aitweaks.utils.firingCycle
import com.genir.aitweaks.utils.shieldUptime
import kotlin.math.min

val fire = null

enum class HoldFire {
    AVOID_PHASED,
    AVOID_SHIELDS,
    AVOID_EXPOSED_HULL,
    AVOID_MISSING_HULL,
    AVOID_FF,
    AVOID_FF_JUNK,
    AVOID_FF_INERT,
    NO_TARGET,
    STABILIZE_ON_TARGET,
    NO_HIT_EXPECTED,
    OUT_OF_RANGE,
    FORCE_OFF,
    CONSERVE_AMMO
}

class AttackRules(private val weapon: WeaponAPI, private val hit: Hit, private val params: Params) {
    val shouldHoldFire = avoidPhased() ?: conserveAmmo() ?: avoidWrongDamageType()

    private fun avoidPhased(): HoldFire? = when {
        (hit.target as? ShipAPI)?.isPhased != true -> fire

        weapon.conserveAmmo -> HoldFire.AVOID_PHASED
        weapon.isPD -> fire
        weapon.isBeam -> fire

        else -> HoldFire.AVOID_PHASED
    }

    private fun conserveAmmo(): HoldFire? = when {
        weapon.isPD != hit.target.isShip -> fire
        weapon.hasAIHint(ANTI_FTR) && (hit.target as? ShipAPI)?.isFighter == true -> fire
        !weapon.usesAmmo() -> fire
        weapon.ammoTracker.maxAmmo - weapon.ammoTracker.ammo >= weapon.ammoTracker.reloadSize -> HoldFire.CONSERVE_AMMO
        else -> fire
    }

    private fun avoidWrongDamageType(): HoldFire? = when {
        !hit.target.isShip -> fire
        weapon.isStrictlyAntiShield -> avoidExposedHull()
        weapon.hasAIHint(USE_LESS_VS_SHIELDS) -> avoidShields() ?: aimAtHull()
        weapon.damageType == HIGH_EXPLOSIVE -> aimAtHull()
        else -> fire
    }

    /** Ensure projectile will not hit shields. */
    private fun avoidShields(): HoldFire? = when {
        !hit.target.isShip -> fire
        hit.shieldHit && weapon.conserveAmmo -> HoldFire.AVOID_SHIELDS
        hit.shieldHit && shieldUptime(hit.target.shield) > 0.8f -> HoldFire.AVOID_SHIELDS
        else -> fire
    }

    /** Ensure projectile will not hit exposed hull. */
    private fun avoidExposedHull(): HoldFire? = when {
        !hit.target.isShip -> fire
        !hit.shieldHit -> HoldFire.AVOID_EXPOSED_HULL
        shieldUptime(hit.target.shield) < min(0.8f, firingCycle(weapon).duration) -> HoldFire.AVOID_EXPOSED_HULL // avoid shield flicker
        else -> fire
    }

    /** Ensure projectile will hit hull. Shields are ignored. */
    private fun aimAtHull(): HoldFire? = when {
        !hit.target.isShip -> fire
        !hit.shieldHit -> fire // hit on hull was already predicted
        willHitBounds(weapon, hit.target as ShipAPI, params).let { it == null || it > weapon.totalRange } -> HoldFire.AVOID_MISSING_HULL // ensure hull is in range, underneath the shields
        else -> fire
    }
}

/** Avoiding friendly fire works under the assumption that the provided
 * actual hit is the first non-fighter, non-phased ship or phased friendly
 * ship along the line of fire. */
fun avoidFriendlyFire(weapon: WeaponAPI, expected: Hit, actual: Hit?): HoldFire? = when {
    actual == null -> fire
    allowPDFriendlyFire(weapon, expected, actual) -> fire
    actual.target !is ShipAPI -> fire
    !actual.target.isAlive -> HoldFire.AVOID_FF_JUNK
    actual.target.isVastBulk -> HoldFire.AVOID_FF_INERT
    actual.target.owner != weapon.ship.owner -> fire
    else -> HoldFire.AVOID_FF
}

/** Allow friendly fire with fragmentation PD when attacking missiles and fighters */
fun allowPDFriendlyFire(weapon: WeaponAPI, expected: Hit, actual: Hit): Boolean = when {
    actual.target.owner != weapon.ship.owner -> false
    actual.range < expected.range -> false
    !weapon.isPD -> false
    weapon.spec.damageType != FRAGMENTATION -> false

    expected.target !is ShipAPI -> true
    else -> expected.target.isFighter
}