package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI

/** Is the entity a true ship, not a missile or fighter. */
val CombatEntityAPI.isShip: Boolean
    get() = (this is ShipAPI) && !this.isFighter

val CombatEntityAPI.isValidTarget: Boolean
    get() = if (this is ShipAPI) this.isAlive && this.isHullDamageable && !this.isExpired
    else !this.isExpired
