package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI

val CombatEntityAPI.isShip: Boolean
    get() = (this is ShipAPI) && !this.isFighter
