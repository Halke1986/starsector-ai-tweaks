package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.USE_LESS_VS_SHIELDS

val WeaponAPI.isAntiArmor: Boolean
    get() = this.damageType == DamageType.HIGH_EXPLOSIVE || this.hasAIHint(USE_LESS_VS_SHIELDS)
