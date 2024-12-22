package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CollisionClass.*
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageType.*
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.fs.starfarer.api.combat.WeaponGroupAPI
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.fs.starfarer.api.loading.ProjectileSpecAPI
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI
import com.genir.aitweaks.core.features.shipai.Preset
import com.genir.aitweaks.core.features.shipai.autofire.AutofireAI
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.aitweaks.core.utils.absShortestRotation
import com.genir.aitweaks.core.utils.clampAngle
import kotlin.math.max

val WeaponAPI.isAntiArmor: Boolean
    get() = damageType == HIGH_EXPLOSIVE || hasAIHint(USE_LESS_VS_SHIELDS)

val WeaponAPI.isAntiShield: Boolean
    get() = damageType == KINETIC || isStrictlyAntiShield

val WeaponAPI.isStrictlyAntiShield: Boolean
    get() = spec.hasTag("aitweaks_anti_shield") && state.config.enableNeedlerFix

val WeaponAPI.isPD: Boolean
    get() = hasAIHint(PD) || hasAIHint(PD_ONLY)

/** Same as WeaponAPI.isPD, except it will ignore weapons that
 * were modified into PD by S-modded Integrated Point Defense AI. */
val WeaponAPI.isPDSpec: Boolean
    get() = spec.aiHints.contains(PD) || spec.aiHints.contains(PD_ONLY)

val WeaponAPI.isMissile: Boolean
    get() = type == WeaponAPI.WeaponType.MISSILE

val WeaponAPI.isUnguidedMissile: Boolean
    get() {
        val spec = spec.projectileSpec as? MissileSpecAPI ?: return false
        return spec.maneuverabilityDisplayName == "None"
    }

val WeaponAPI.isPlainBeam: Boolean
    get() = isBeam && !conserveAmmo

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
    return absShortestRotation(arcFacing, angle) <= (arc + tolerance) / 2f
}

val WeaponAPI.isFrontFacing: Boolean
    get() = isAngleInArc(0f)

/** weapon arc facing in absolute coordinates, instead of ship coordinates */
val WeaponAPI.absoluteArcFacing: Float
    get() = clampAngle(arcFacing + ship.facing)

val WeaponAPI.totalRange: Float
    get() = range + projectileFadeRange * 0.33f + barrelOffset

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

/** Warmup is the first phase of weapon firing sequence, preceding the first shot. */
val WeaponAPI.isInWarmup: Boolean
    get() = chargeLevel > 0f && chargeLevel < 1f && cooldownRemaining == 0f

/** Weapon is assumed to be in a firing sequence if it will
 * emit projectile or beam even after trigger is let go. */
val WeaponAPI.isInFiringSequence: Boolean
    get() = when {
        isBeam && !isBurstBeam -> false
        isInWarmup -> true // warmup
        chargeLevel == 1f && isBurstWeapon -> true // burst
        else -> false
    }

/** Similar to WeaponAPI.isFiring, except returns true for the entire firing cycle.
 * WeaponAPI.isFiring returns false between individual burst attacks. */
val WeaponAPI.isInFiringCycle: Boolean
    get() = chargeLevel != 0f || cooldownRemaining != 0f

val WeaponAPI.group: WeaponGroupAPI?
    get() = this.ship.getWeaponGroupFor(this)

val WeaponAPI.target: CombatEntityAPI?
    get() = autofirePlugin?.let { it.targetShip ?: it.targetMissile } ?: ship.shipTarget

val WeaponAPI.barrelOffset: Float
    get() {
        // Beams do have a defined barrel offset, but
        // the beam itself starts at the weapon center.
        if (isBeam) return 0f

        val offsets = if (slot.isHardpoint) spec.hardpointFireOffsets
        else spec.turretFireOffsets
        return offsets[0]?.x ?: 0f
    }

val WeaponAPI.effectiveDPS: Float
    get() = derivedStats.dps * if (damageType == FRAGMENTATION) 0.25f else 1f

/** The true projectile speed, which may differ from the value returned by vanilla WeaponAPI.projectileSpeed. */
val WeaponAPI.trueProjectileSpeed: Float
    get() = (spec.projectileSpec as? ProjectileSpecAPI)?.getMoveSpeed(ship.mutableStats, this) ?: projectileSpeed

/** Can the weapon shoot over allied ships. */
val WeaponAPI.noFF: Boolean
    get() = when (projectileCollisionClass) {
        MISSILE_NO_FF -> true
        PROJECTILE_NO_FF -> true
        RAY_FIGHTER -> true
        PROJECTILE_FIGHTER -> true
        else -> false
    }

/** Weapon range from the center of the ship. */
val WeaponAPI.slotRange: Float
    get() = range + barrelOffset + slot.location.x

val WeaponAPI.isOutOfAmmo: Boolean
    get() = usesAmmo() && ammo == 0 && ammoTracker.ammoPerSecond <= 0f

/** Total time required for the weapon to reload from empty to full ammunition. */
val WeaponAPI.totalReloadTime: Float
    get() {
        // Weapon does not use ammo.
        if (!usesAmmo()) return cooldown

        // Weapon is permanently out if ammo.
        val tracker = ammoTracker!!
        if (tracker.ammoPerSecond <= 0f) return Float.MAX_VALUE

        val reloadTime = tracker.maxAmmo / tracker.ammoPerSecond
        return max(reloadTime, cooldown)
    }

val WeaponAPI.totalReloadTimeRemaining: Float
    get() {
        // Weapon does not use ammo.
        if (!usesAmmo()) return cooldownRemaining

        // Weapon is permanently out if ammo.
        val tracker = ammoTracker!!
        if (tracker.ammoPerSecond <= 0f) return Float.MAX_VALUE

        val reloadTime = (tracker.maxAmmo - (tracker.ammo + tracker.reloadProgress * tracker.reloadSize)) / tracker.ammoPerSecond
        return max(reloadTime, cooldownRemaining)
    }

val WeaponAPI.isInLongReload: Boolean
    get() = when {
        isInFiringCycle -> false

        totalReloadTimeRemaining < 2f -> false
        totalReloadTime < Preset.weaponMaxReloadTime -> false
        ammo >= maxAmmo / 2 -> false

        else -> true
    }
