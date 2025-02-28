package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.combat.DamageType.FRAGMENTATION
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.USE_LESS_VS_SHIELDS
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize.LARGE
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.autofire.Hit.Type.*
import com.genir.aitweaks.core.shipai.autofire.HoldFire.*
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
        hit.target.FluxLevel > 0.9f -> fire

        else -> AVOID_PHASED
    }

    /** Do not waste PD ammo on ships, and non-PD ammo on fighters and missiles. */
    private fun conservePDAmmo(): HoldFire? = when {
        weapon.isPD != hit.target.isShip -> fire
        weapon.isAntiFighter && (hit.target as? ShipAPI)?.isFighter == true -> fire
        !weapon.usesAmmo() -> fire
        !weapon.hasAmmoToSpare -> CONSERVE_AMMO
        else -> fire
    }

    private fun avoidWrongDamageType(): HoldFire? = when {
        !hit.target.isShip -> fire
        weapon.isStrictlyAntiShield -> avoidExposedHull()
        weapon.hasAIHint(USE_LESS_VS_SHIELDS) || weapon.isFinisherBeam -> avoidShields()
        else -> fire
    }

    /** Ensure projectile will not hit shields. */
    private fun avoidShields(): HoldFire? {
        return when {
            !hit.target.isShip -> fire

            hit.type != SHIELD -> fire

            weapon.usesAmmo() -> AVOID_SHIELDS

            weapon.damageType == FRAGMENTATION -> AVOID_SHIELDS

            // Try to burst through almost depleted shields.
            weapon.isBurstBeam && weapon.firingCycle.damage > (hit.target as ShipAPI).fluxLeft * 2f -> fire

            // Don't interrupt fire because of shield flicker.
            weapon.isInFiringCycle && shieldUptime(hit.target.shield) < 1.2f -> fire

            else -> AVOID_SHIELDS
        }
    }

    /** Ensure projectile will not hit exposed hull. */
    private fun avoidExposedHull(): HoldFire? = when {
        !hit.target.isShip -> fire

        weapon.size == LARGE && (hit.target as ShipAPI).root.isFrigate -> fire

        weapon.ship.system?.let { it.specAPI.id == "lidararray" && it.isOn } == true -> fire

        hit.type == HULL -> AVOID_EXPOSED_HULL

        // Avoid shield flicker.
        shieldUptime(hit.target.shield) < min(0.8f, weapon.firingCycle.duration) -> AVOID_EXPOSED_HULL

        else -> fire
    }
}

/** Avoiding friendly fire works under the assumption that the provided
 * actual hit is the first non-fighter, non-phased ship or phased friendly
 * ship along the line of fire. */
fun avoidFriendlyFire(weapon: WeaponAPI, expected: Hit, actual: Hit?): HoldFire? {
    return when {
        // There are no obstacles in the line of fire.
        actual == null -> {
            fire
        }

        // Weapon will hit an unidentified entity. Allow fire.
        actual.target !is ShipAPI -> {
            fire
        }

        // Weapon will hit a friendly ship.
        actual.target.owner == weapon.ship.owner -> when {

            // Obstacle is in front of the expected target. A case of true friendly fire.
            actual.range < expected.range -> {
                AVOID_FF
            }

            // Allow risk of friendly fire if PD weapon misses its target.
            allowPDFriendlyFire(weapon, expected) -> {
                fire
            }

            else -> {
                AVOID_FF
            }
        }

        // Weapon will hit inert or enemy ship.
        else -> when {

            // Beams transiting to a new target are allowed to hit inert targets.
            expected.type == ROTATE_BEAM -> {
                fire
            }

            // PD beam fire is allowed to hit inert targets.
            weapon.isPD && weapon.isPlainBeam && expected.target.isPDTarget -> {
                fire
            }

            !actual.target.isAlive -> {
                AVOID_FF_JUNK
            }

            !actual.target.isHullDamageable && actual.type != SHIELD -> {
                AVOID_FF_INERT
            }

            // Weapon will hit a damageable enemy ship. Allow fire.
            else -> {
                fire
            }
        }
    }
}

/** Allow friendly fire with in some cases of PD defense. */
fun allowPDFriendlyFire(weapon: WeaponAPI, expected: Hit): Boolean = when {
    // Determine if weapon is performing PD defense.
    !weapon.isPD -> false
    !expected.target.isPDTarget -> false

    // Only fragmentation and beam weapons are allowed to risk friendly fire.
    else -> weapon.spec.damageType == FRAGMENTATION || weapon.isPlainBeam
}
