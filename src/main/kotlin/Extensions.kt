package com.genir.aitweaks

import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

fun WeaponAPI.hasBestTargetLeading(): Boolean =
    (this.hasAIHint(WeaponAPI.AIHints.PD) || this.hasAIHint(WeaponAPI.AIHints.PD_ONLY)) &&
            !this.hasAIHint(WeaponAPI.AIHints.STRIKE) &&
            ship.mutableStats.dynamic.getValue("pd_best_target_leading", 0f) >= 1f

fun WeaponAPI.isAimable(): Boolean =
    this.spec?.trackingStr in setOf(null, "", "none", "NONE", "None") &&
            !(this.hasAIHint(WeaponAPI.AIHints.DO_NOT_AIM))

internal infix fun Vector2f.times(d: Float): Vector2f = Vector2f(d * x, d * y)