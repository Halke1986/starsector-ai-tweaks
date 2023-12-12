package com.genir.aitweaks.extensions

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI

val AutofireAIPlugin.targetEntity: CombatEntityAPI?
    get() = when {
        this.targetShip != null -> this.targetShip
        this.targetMissile != null -> this.targetMissile
        else -> null
    }

