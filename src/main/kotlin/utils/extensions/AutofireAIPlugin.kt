package com.genir.aitweaks.extensions

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI

val AutofireAIPlugin.targetEntity: CombatEntityAPI?
    get() = this.targetShip ?: this.targetMissile
