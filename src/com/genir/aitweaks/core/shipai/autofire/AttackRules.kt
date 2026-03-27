package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.combat.DamageType.FRAGMENTATION
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.PD_ALSO
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize.LARGE
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.shipai.autofire.HoldFire.*
import com.genir.aitweaks.core.shipai.autofire.ballistics.BallisticParams
import com.genir.aitweaks.core.shipai.autofire.ballistics.Hit
import com.genir.aitweaks.core.shipai.autofire.ballistics.Hit.Type.*
import com.genir.aitweaks.core.state.Config.Companion.config
import com.genir.aitweaks.core.utils.shieldUptime

enum class HoldFire {
    FIRE,
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

class AttackRules(private val weapon: WeaponHandle, private val hit: Hit, private val params: BallisticParams) {
    fun shouldHoldFire(): HoldFire {
        val reasonPhased = avoidPhased()
        if (reasonPhased != FIRE) {
            return reasonPhased
        }

        val reasonAmmo = conservePDAmmo()
        if (reasonAmmo != FIRE) {
            return reasonAmmo
        }

        return avoidWrongDamageType()
    }

    private fun avoidPhased(): HoldFire = when {
        (hit.target as? ShipAPI)?.isPhased != true -> FIRE

        weapon.conserveAmmo -> AVOID_PHASED
        weapon.isPD -> FIRE
        weapon.isBeam -> FIRE
        hit.target.FluxLevel > 0.9f -> FIRE

        else -> AVOID_PHASED
    }

    /** Do not waste PD ammo on ships, and non-PD ammo on fighters and missiles. */
    private fun conservePDAmmo(): HoldFire = when {
        !weapon.usesAmmo() -> FIRE
        weapon.hasAmmoToSpare -> FIRE

        weapon.isAntiFighter && hit.target.isFighter -> FIRE
        weapon.isPD && hit.target.isPDTarget -> FIRE
        (!weapon.isPD || weapon.hasAIHint(PD_ALSO)) && hit.target.isShip -> FIRE

        else -> CONSERVE_AMMO
    }

    private fun avoidWrongDamageType(): HoldFire = when {
        !hit.target.isShip -> FIRE

        weapon.isStrictlyAntiShield -> avoidExposedHull()

        weapon.isStrictlyAntiArmor -> avoidShields()

        else -> FIRE
    }

    /** Ensure projectile will not hit shields. */
    private fun avoidShields(): HoldFire {
        return when {
            !hit.target.isShip -> FIRE

            hit.type != SHIELD -> FIRE

            weapon.usesAmmo() -> when {
                // Voidblaster on Assault Units in incursion mode
                // (and other weapons in similar circumstances)
                // should be allowed to use spare ammo on shields in all cases.
                weapon.ammoRegenMultiplier > 3f && weapon.hasAmmoToSpare -> FIRE

                config.strictUseLessVSShields -> AVOID_SHIELDS

                weapon.hasAmmoToSpare -> FIRE

                else -> AVOID_SHIELDS
            }

            // Try to burst through almost depleted shields.
            weapon.isBurstBeam && weapon.firingCycle.damage > (hit.target as ShipAPI).fluxLeft * 2f -> FIRE

            // Don't interrupt fire because of shield flicker.
            weapon.isInFiringCycle && shieldUptime(hit.target.shield) < 1.2f -> FIRE

            else -> AVOID_SHIELDS
        }
    }

    /** Ensure projectile will not hit exposed hull. */
    private fun avoidExposedHull(): HoldFire = when {
        !hit.target.isShip -> FIRE

        weapon.size == LARGE && (hit.target as ShipAPI).root.isFrigate -> FIRE

        weapon.ship.system?.let { it.specAPI.id == "lidararray" && it.isOn } == true -> FIRE

        hit.type == HULL -> AVOID_EXPOSED_HULL

        // Avoid shield flicker.
        shieldUptime(hit.target.shield) < minOf(0.8f, weapon.firingCycle.duration) -> AVOID_EXPOSED_HULL

        else -> FIRE
    }
}

/** Avoiding friendly fire works under the assumption that the provided
 * actual hit is the first non-fighter, non-phased ship or phased friendly
 * ship along the line of fire. */
fun avoidFriendlyFire(weapon: WeaponHandle, expected: Hit, actual: Hit?): HoldFire {
    return when {
        // There are no obstacles in the line of fire.
        actual == null -> {
            FIRE
        }

        // Weapon will hit an unidentified entity. Allow fire.
        actual.target !is ShipAPI -> {
            FIRE
        }

        // Weapon will hit a friendly ship.
        actual.target.owner == weapon.ship.owner -> when {

            // Obstacle is in front of the expected target. A case of true friendly fire.
            actual.range < expected.range -> {
                AVOID_FF
            }

            // Allow risk of friendly fire if PD weapon misses its target.
            allowPDFriendlyFire(weapon, expected) -> {
                FIRE
            }

            else -> {
                AVOID_FF
            }
        }

        // Obstacle is behind the expected target. This case may happen
        // during PD fire, when a high likelihood of a miss is assumed.
        expected.range < actual.range -> {
            FIRE
        }

        // Weapon will hit inert or enemy ship.
        else -> when {

            // Beams transiting to a new target are allowed to hit inert targets.
            expected.type == ROTATE_BEAM -> {
                FIRE
            }

            !actual.target.isAlive -> {
                AVOID_FF_JUNK
            }

            !actual.target.isHullDamageable && actual.type != SHIELD -> {
                AVOID_FF_INERT
            }

            // Weapon will hit a damageable enemy ship. Allow fire.
            else -> {
                FIRE
            }
        }
    }
}

/** Allow friendly fire with in some cases of PD defense. */
fun allowPDFriendlyFire(weapon: WeaponHandle, expected: Hit): Boolean {
    return when {
        // Determine if weapon is performing PD defense.
        !weapon.isPD -> false

        !expected.target.isPDTarget -> false

        // Only fragmentation and beam weapons are allowed to risk friendly fire.
        else -> weapon.spec?.damageType == FRAGMENTATION || weapon.isPlainBeam
    }
}
