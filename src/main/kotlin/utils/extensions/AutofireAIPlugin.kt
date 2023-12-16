package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI

val AutofireAIPlugin.targetEntity: CombatEntityAPI?
    get() = this.targetShip ?: this.targetMissile
