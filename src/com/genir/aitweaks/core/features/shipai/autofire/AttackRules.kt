package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.combat.DamageType.FRAGMENTATION
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.ANTI_FTR
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.USE_LESS_VS_SHIELDS
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize.LARGE
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.features.shipai.autofire.Hit.Type.*
import com.genir.aitweaks.core.features.shipai.autofire.HoldFire.*
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

        weapon.conserveAmmo -> AVOID_PHASED
        weapon.isPD -> fire
        weapon.isBeam -> fire
        hit.target.fluxLevel > 0.9f -> fire

        else -> AVOID_PHASED
    }

    /** Do not waste PD ammo on ships, and non-PD ammo on fighters and missiles. */
    private fun conservePDAmmo(): HoldFire? = when {
        weapon.isPD != hit.target.isShip -> fire
        weapon.hasAIHint(ANTI_FTR) && (hit.target as? ShipAPI)?.isFighter == true -> fire
        !weapon.usesAmmo() -> fire
        !weapon.hasAmmoToSpare -> CONSERVE_AMMO
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
        hit.type == SHIELD && weapon.conserveAmmo -> AVOID_SHIELDS
        hit.type == SHIELD && shieldUptime(hit.target.shield) > 1.2f -> AVOID_SHIELDS
        else -> fire
    }

    /** Ensure projectile will not hit exposed hull. */
    private fun avoidExposedHull(): HoldFire? = when {
        !hit.target.isShip -> fire
        weapon.size == LARGE && (hit.target as ShipAPI).isFrigateShip -> fire
        weapon.ship.system?.let { it.specAPI.id == "lidararray" && it.isOn } == true -> fire
        hit.type == HULL -> AVOID_EXPOSED_HULL
        shieldUptime(hit.target.shield) < min(0.8f, weapon.firingCycle.duration) -> AVOID_EXPOSED_HULL // avoid shield flicker
        else -> fire
    }
}

/** Avoiding friendly fire works under the assumption that the provided
 * actual hit is the first non-fighter, non-phased ship or phased friendly
 * ship along the line of fire. */
fun avoidFriendlyFire(weapon: WeaponAPI, expected: Hit, actual: Hit?): HoldFire? = when {
    // There are no obstacles in the line of fire.
    actual == null -> fire

    // Weapon will hit an unidentified entity. Allow fire.
    actual.target !is ShipAPI -> fire

    // Obstacle is in front of the expected target.
    actual.range < expected.range -> when {

        // True friendly fire.
        actual.target.owner == weapon.ship.owner -> AVOID_FF

        // Beams transiting to a new target are allowed to hit inert targets.
        expected.type == ROTATE_BEAM -> fire

        // PD beam fire is allowed to hit inert targets.
        weapon.isPD && weapon.isPlainBeam && expected.target.isPDTarget -> fire

        !actual.target.isAlive -> AVOID_FF_JUNK

        !actual.target.isHullDamageable -> AVOID_FF_INERT

        // Weapon will hit other non-friendly entity. Allow fire.
        else -> fire
    }

    // Obstacle is behind the expected target. This happens in case of PD fire.
    actual.target.owner == weapon.ship.owner -> when {

        // Allow risk of friendly fire if PD weapon misses its target.
        allowPDFriendlyFire(weapon, expected) -> fire

        else -> AVOID_FF
    }

    // Weapon may hit other non-friendly entity if PD weapon misses its target. Allow fire.
    else -> fire
}

/** Allow friendly fire with in some cases of PD defense. */
fun allowPDFriendlyFire(weapon: WeaponAPI, expected: Hit): Boolean = when {
    // Determine if weapon is performing PD defense.
    !weapon.isPD -> false
    !expected.target.isPDTarget -> false

    // Only fragmentation and beam weapons are allowed to risk friendly fire.
    else -> weapon.spec.damageType == FRAGMENTATION || weapon.isPlainBeam
}
