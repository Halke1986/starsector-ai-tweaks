package com.genir.aitweaks.features.autofire.extensions

import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import org.lazywizard.lazylib.MathUtils
import kotlin.math.abs

val WeaponAPI.isPD: Boolean
    get() = this.hasAIHint(PD) || this.hasAIHint(PD_ONLY)

val WeaponAPI.isAntiFtr: Boolean
    get() = this.isPD || this.hasAIHint(ANTI_FTR)

val WeaponAPI.conserveAmmo: Boolean
    get() = this.usesAmmo() || this.isBurstBeam

val WeaponAPI.hasBestTargetLeading: Boolean
    get() = this.isPD && !this.hasAIHint(WeaponAPI.AIHints.STRIKE) && ship.mutableStats.dynamic.getValue(
        "pd_best_target_leading", 0f
    ) >= 1f

val WeaponAPI.ignoresFlares: Boolean
    get() = this.hasAIHint(IGNORES_FLARES) || ship.mutableStats.dynamic.getValue("pd_ignores_flares", 0f) >= 1f

val WeaponAPI.frontFacing: Boolean
    get() = abs(MathUtils.getShortestRotation(this.arcFacing, 0f)) <= this.arc / 2f

/** weapon arc facing in absolute coordinates, instead of ship coordinates */
val WeaponAPI.absoluteArcFacing: Float
    get() = MathUtils.clampAngle(this.arcFacing + this.ship.facing)

val WeaponAPI.totalRange: Float
    get() = this.range + this.projectileFadeRange * 0.5f
