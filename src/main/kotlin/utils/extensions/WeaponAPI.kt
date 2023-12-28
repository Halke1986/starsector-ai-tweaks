package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import org.lazywizard.lazylib.MathUtils
import kotlin.math.abs

val WeaponAPI.isPD: Boolean
    get() = this.hasAIHint(PD) || this.hasAIHint(PD_ONLY)

val WeaponAPI.hasBestTargetLeading: Boolean
    get() = this.isPD && !this.hasAIHint(WeaponAPI.AIHints.STRIKE) && ship.mutableStats.dynamic.getValue(
        "pd_best_target_leading", 0f
    ) >= 1f

val WeaponAPI.ignoresFlares: Boolean
    get() = this.hasAIHint(IGNORES_FLARES) || ship.mutableStats.dynamic.getValue("pd_ignores_flares", 0f) >= 1f

val WeaponAPI.frontFacing: Boolean
    get() = abs(MathUtils.getShortestRotation(this.arcFacing, 0f)) <= this.arc / 2f

val WeaponAPI.isAimable: Boolean
    get() = !(this.hasAIHint(DO_NOT_AIM))

val WeaponAPI.isAntiArmor: Boolean
    get() = this.damageType == DamageType.HIGH_EXPLOSIVE || this.hasAIHint(USE_LESS_VS_SHIELDS)

/** weapon arc facing in absolute coordinates, instead of ship coordinates */
val WeaponAPI.absoluteArcFacing: Float
    get() = MathUtils.clampAngle(this.arcFacing + this.ship.facing)
