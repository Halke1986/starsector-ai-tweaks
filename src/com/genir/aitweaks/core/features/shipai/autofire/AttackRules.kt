package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.combat.DamageType.FRAGMENTATION
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.ANTI_FTR
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.USE_LESS_VS_SHIELDS
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize.LARGE
import com.genir.aitweaks.core.features.shipai.autofire.Hit.Type.HULL
import com.genir.aitweaks.core.features.shipai.autofire.Hit.Type.SHIELD
import com.genir.aitweaks.core.utils.extensions.*
import com.genir.aitweaks.core.utils.shieldUptime
import kotlin.math.min

val fire = null

enum class HoldFire {
    AVOID_PHASED,
    AVOID_SHIELDS,
    AVOID_EXPOSED_HULL,
    AVOID_FF,
    AVOID_FF_JUNK,
    AVOID_FF_INERT,
    NO_TARGET,
    NO_HIT_EXPECTED,
    STABILIZE_ON_TARGET,
    OUT_OF_RANGE,
    CONSERVE_AMMO,
    SAVE_FLUX
}

class AttackRules(private val weapon: WeaponAPI, private val hit: Hit, private val params: BallisticParams) {
    val shouldHoldFire = avoidPhased() ?: conservePDAmmo() ?: avoidWrongDamageType()

    private fun avoidPhased(): HoldFire? = when {
        (hit.target as? ShipAPI)?.isPhased != true -> fire

        weapon.conserveAmmo -> HoldFire.AVOID_PHASED
        weapon.isPD -> fire
        weapon.isBeam -> fire
        hit.target.fluxLevel > 0.9f -> fire

        else -> HoldFire.AVOID_PHASED
    }

    /** Do not waste PD ammo on ships, and non-PD ammo on fighters and missiles. */
    private fun conservePDAmmo(): HoldFire? = when {
        weapon.isPD != hit.target.isShip -> fire
        weapon.hasAIHint(ANTI_FTR) && (hit.target as? ShipAPI)?.isFighter == true -> fire
        !weapon.usesAmmo() -> fire
        !weapon.hasAmmoToSpare -> HoldFire.CONSERVE_AMMO
        else -> fire
    }

    private fun avoidWrongDamageType(): HoldFire? = when {
        !hit.target.isShip -> fire
        weapon.isStrictlyAntiShield -> avoidExposedHull()
        weapon.hasAIHint(USE_LESS_VS_SHIELDS) -> avoidShields()
        else -> fire
    }

    /** Ensure projectile will not hit shields. */
    private fun avoidShields(): HoldFire? = when {
        !hit.target.isShip -> fire
        hit.type == SHIELD && weapon.conserveAmmo -> HoldFire.AVOID_SHIELDS
        hit.type == SHIELD && shieldUptime(hit.target.shield) > 1.2f -> HoldFire.AVOID_SHIELDS
        else -> fire
    }

    /** Ensure projectile will not hit exposed hull. */
    private fun avoidExposedHull(): HoldFire? = when {
        !hit.target.isShip -> fire
        weapon.size == LARGE && (hit.target as ShipAPI).isFrigateShip -> fire
        weapon.ship.system?.let { it.specAPI.id == "lidararray" && it.isOn } == true -> fire
        hit.type == HULL -> HoldFire.AVOID_EXPOSED_HULL
        shieldUptime(hit.target.shield) < min(0.8f, weapon.firingCycle.duration) -> HoldFire.AVOID_EXPOSED_HULL // avoid shield flicker
        else -> fire
    }
}

/** Avoiding friendly fire works under the assumption that the provided
 * actual hit is the first non-fighter, non-phased ship or phased friendly
 * ship along the line of fire. */
fun avoidFriendlyFire(weapon: WeaponAPI, expected: Hit?, actual: Hit?): HoldFire? = when {
    actual == null -> fire
    expected != null && allowPDFriendlyFire(weapon, expected, actual) -> fire
    actual.target !is ShipAPI -> fire
    !actual.target.isAlive -> HoldFire.AVOID_FF_JUNK
    !actual.target.isHullDamageable -> HoldFire.AVOID_FF_INERT
    actual.target.owner != weapon.ship.owner -> fire
    else -> HoldFire.AVOID_FF
}

/** Allow friendly fire with in some cases of PD defense. */
fun allowPDFriendlyFire(weapon: WeaponAPI, expected: Hit, actual: Hit): Boolean = when {
    // Determine if there's a risk of friendly fire.
    actual.target.owner != weapon.ship.owner -> false
    actual.range < expected.range -> false

    // Only fragmentation and beam weapons are allowed to risk friendly fire.
    weapon.spec.damageType != FRAGMENTATION && !weapon.isPlainBeam -> false

    // Determine if weapon is performing PD defense.
    !weapon.isPD -> false
    !expected.target.isPDTarget -> false

    else -> true
}