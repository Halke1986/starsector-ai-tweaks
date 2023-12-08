package com.genir.aitweaks

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

fun WeaponAPI.hasBestTargetLeading(): Boolean =
    (this.hasAIHint(WeaponAPI.AIHints.PD) || this.hasAIHint(WeaponAPI.AIHints.PD_ONLY)) &&
            !this.hasAIHint(WeaponAPI.AIHints.STRIKE) &&
            ship.mutableStats.dynamic.getValue("pd_best_target_leading", 0f) >= 1f

fun WeaponAPI.isAimable(): Boolean =
    !(this.hasAIHint(WeaponAPI.AIHints.DO_NOT_AIM))

fun WeaponAPI.firesForward(): Boolean =
    abs(MathUtils.getShortestRotation(this.arcFacing, 0f)) <= this.arc / 2f

fun ShipAPI.maneuverTarget(): ShipAPI? =
    this.aiFlags.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI

fun AutofireAIPlugin.getTargetEntity(): CombatEntityAPI? = when {
    this.targetShip != null -> this.targetShip
    this.targetMissile != null -> this.targetMissile
    else -> null
}

fun Float.radians(): Float = this * 0.017453292f
fun Float.degrees(): Float = this * 57.29578f

internal infix fun Vector2f.times(d: Float): Vector2f = Vector2f(d * x, d * y)


