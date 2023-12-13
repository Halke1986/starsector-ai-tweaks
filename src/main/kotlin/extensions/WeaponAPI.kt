package com.genir.aitweaks.extensions

import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.MathUtils
import kotlin.math.abs

val WeaponAPI.hasBestTargetLeading: Boolean
    get() = (this.hasAIHint(WeaponAPI.AIHints.PD) || this.hasAIHint(WeaponAPI.AIHints.PD_ONLY)) && !this.hasAIHint(
        WeaponAPI.AIHints.STRIKE
    ) && ship.mutableStats.dynamic.getValue("pd_best_target_leading", 0f) >= 1f

val WeaponAPI.firesForward: Boolean
    get() = abs(MathUtils.getShortestRotation(this.arcFacing, 0f)) <= this.arc / 2f

val WeaponAPI.isAimable: Boolean
    get() = !(this.hasAIHint(WeaponAPI.AIHints.DO_NOT_AIM))

val WeaponAPI.isAntiArmor: Boolean
    get() = this.damageType == DamageType.HIGH_EXPLOSIVE || this.hasAIHint(WeaponAPI.AIHints.USE_LESS_VS_SHIELDS)

val WeaponAPI.isAnyBeam: Boolean
    get() = this.isBeam || this.isBurstBeam
