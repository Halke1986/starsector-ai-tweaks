package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.times
import com.genir.aitweaks.utils.unitVector
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
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

// weapon arc facing in absolute coordinates, instead of ship coordinates
val WeaponAPI.absoluteArcFacing: Float
    get() = MathUtils.clampAngle(this.arcFacing + this.ship.facing)

// Projectile velocity vector in absolute coordinates. Unit vector for beam weapons.
val WeaponAPI.absoluteProjectileVelocity: Vector2f
    get() = if (this.isAnyBeam) unitVector(this.currAngle)
    else unitVector(this.currAngle) * this.projectileSpeed + this.ship.velocity