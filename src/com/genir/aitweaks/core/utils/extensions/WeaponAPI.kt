package com.genir.aitweaks.core.utils.extensions

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI
import com.genir.aitweaks.core.features.shipai.autofire.AutofireAI
import org.lazywizard.lazylib.MathUtils
import kotlin.math.abs

val WeaponAPI.isAntiArmor: Boolean
    get() = damageType == DamageType.HIGH_EXPLOSIVE || hasAIHint(USE_LESS_VS_SHIELDS)

val WeaponAPI.isPD: Boolean
    get() = hasAIHint(PD) || hasAIHint(PD_ONLY)

/** Same as WeaponAPI.isPD, except it will ignore weapons that
* were modified into PD by S-modded Integrated Point Defense AI. */
val WeaponAPI.isPDSpec: Boolean
    get() = spec.aiHints.contains(PD) || spec.aiHints.contains(PD_ONLY)

val WeaponAPI.isStrictlyAntiShield: Boolean
    get() = spec.hasTag("aitweaks_anti_shield")

val WeaponAPI.conserveAmmo: Boolean
    get() = usesAmmo() || isBurstBeam

val WeaponAPI.hasAmmoToSpare: Boolean
    get() = !usesAmmo() || ammoTracker.let { it.ammoPerSecond > 0 && (it.ammo + it.reloadSize > it.maxAmmo) }

val WeaponAPI.hasBestTargetLeading: Boolean
    get() = isPD && !hasAIHint(STRIKE) && ship.mutableStats.dynamic.getValue("pd_best_target_leading", 0f) >= 1f

val WeaponAPI.ignoresFlares: Boolean
    get() = hasAIHint(IGNORES_FLARES) || ship.mutableStats.dynamic.getValue("pd_ignores_flares", 0f) >= 1f

fun WeaponAPI.isAngleInArc(angle: Float): Boolean {
    val tolerance = 0.01f
    return abs(MathUtils.getShortestRotation(arcFacing, angle)) <= (arc + tolerance) / 2f
}

val WeaponAPI.isFrontFacing: Boolean
    get() = isAngleInArc(0f)

/** weapon arc facing in absolute coordinates, instead of ship coordinates */
val WeaponAPI.absoluteArcFacing: Float
    get() = MathUtils.clampAngle(arcFacing + ship.facing)

val WeaponAPI.totalRange: Float
    get() = range + projectileFadeRange * 0.5f

val WeaponAPI.timeToAttack: Float
    get() {
        val spec = spec as? ProjectileWeaponSpecAPI ?: return 0f

        return when {
            isInBurst || (isBurstBeam && isFiring) -> 0f
            cooldownRemaining != 0f -> cooldownRemaining + spec.chargeTime
            else -> spec.chargeTime * (1f - chargeLevel)
        }
    }

val WeaponAPI.autofirePlugin: AutofireAIPlugin?
    get() = ship.getWeaponGroupFor(this)?.getAutofirePlugin(this)

val WeaponAPI.customAI: AutofireAI?
    get() = autofirePlugin as? AutofireAI

val WeaponAPI.isBurstWeapon: Boolean
    get() = when {
        // Burst beams, excluding "continuous" burst beams like IR Autolance.
        isBeam && spec.burstDuration > 0f && cooldown > 0f -> true

        // Projectile weapons with bursts of more than one projectile.
        spec is ProjectileWeaponSpecAPI -> (spec as ProjectileWeaponSpecAPI).burstSize > 1

        else -> false
    }

/** Weapon is assumed to be in a firing sequence if it will
 * emit projectile or beam even after trigger is let go. */
val WeaponAPI.isInFiringSequence: Boolean
    get() = when {
        isBeam && !isBurstBeam -> false
        isInWarmup -> true // warmup
        chargeLevel == 1f && isBurstWeapon -> true // burst
        else -> false
    }

/** Warmup is the first phase of weapon firing sequence, preceding the first shot. */
val WeaponAPI.isInWarmup: Boolean
    get() = chargeLevel > 0f && chargeLevel < 1f && cooldownRemaining == 0f

val WeaponAPI.group: WeaponGroupAPI
    get() = this.ship.getWeaponGroupFor(this)

val WeaponAPI.target: CombatEntityAPI?
    get() = this.autofirePlugin?.let { it.targetShip ?: it.targetMissile }