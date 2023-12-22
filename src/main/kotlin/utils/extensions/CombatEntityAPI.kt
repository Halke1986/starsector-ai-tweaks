package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.CombatEntityAPI

val CombatEntityAPI.radius: Float
    get() = this.shield?.radius ?: this.collisionRadius