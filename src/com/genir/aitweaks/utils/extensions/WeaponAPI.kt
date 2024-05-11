package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI
import com.genir.aitweaks.features.autofire.AutofireAI
import com.genir.aitweaks.utils.attack.FiringCycle
import com.genir.aitweaks.utils.attack.firingCycle
import org.lazywizard.lazylib.MathUtils
import kotlin.math.abs

val WeaponAPI.isAntiArmor: Boolean
    get() = damageType == DamageType.HIGH_EXPLOSIVE || hasAIHint(USE_LESS_VS_SHIELDS)

val WeaponAPI.isPD: Boolean
    get() = hasAIHint(PD) || hasAIHint(PD_ONLY)

val WeaponAPI.isStrictlyAntiShield: Boolean
    get() = spec.hasTag("aitweaks_anti_shield")

val WeaponAPI.conserveAmmo: Boolean
    get() = usesAmmo() || isBurstBeam

val WeaponAPI.hasAmmoToSpare: Boolean
    get() = !usesAmmo() || ammoTracker.let { it.reloadSize > 0 && (it.ammo + it.reloadSize > it.maxAmmo) }

val WeaponAPI.hasBestTargetLeading: Boolean
    get() = isPD && !hasAIHint(STRIKE) && ship.mutableStats.dynamic.getValue("pd_best_target_leading", 0f) >= 1f

val WeaponAPI.ignoresFlares: Boolean
    get() = hasAIHint(IGNORES_FLARES) || ship.mutableStats.dynamic.getValue("pd_ignores_flares", 0f) >= 1f

val WeaponAPI.frontFacing: Boolean
    get() = abs(MathUtils.getShortestRotation(arcFacing, 0f)) <= arc / 2f

/** weapon arc facing in absolute coordinates, instead of ship coordinates */
val WeaponAPI.absoluteArcFacing: Float
    get() = MathUtils.clampAngle(arcFacing + ship.facing)

val WeaponAPI.totalRange: Float
    get() = range + projectileFadeRange * 0.5f

val WeaponAPI.firingCycle: FiringCycle
    get() = firingCycle(this)

val WeaponAPI.timeToAttack: Float
    get() {
        val spec = spec as? ProjectileWeaponSpecAPI ?: return 0f

        return when {
            trueIsInBurst -> 0f
            cooldownRemaining != 0f -> cooldownRemaining + spec.chargeTime
            else -> spec.chargeTime * (1f - chargeLevel)
        }
    }

val WeaponAPI.autofirePlugin: AutofireAIPlugin?
    get() = ship.getWeaponGroupFor(this)?.getAutofirePlugin(this)

val WeaponAPI.autofireAI: AutofireAI?
    get() = autofirePlugin as? AutofireAI

val WeaponAPI.trueIsInBurst: Boolean
    get() = isInBurst || (isBurstBeam && isFiring)

val WeaponAPI.group: WeaponGroupAPI
    get() = this.ship.getWeaponGroupFor(this)

val WeaponAPI.target: CombatEntityAPI?
    get() = this.autofirePlugin?.let { it.targetShip ?: it.targetMissile }