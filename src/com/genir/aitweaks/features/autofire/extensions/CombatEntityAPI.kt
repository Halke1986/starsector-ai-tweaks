package com.genir.aitweaks.features.autofire.extensions

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI

/** Is the entity a true ship, not a missile or fighter. */
val CombatEntityAPI.isShip: Boolean
    get() = (this is ShipAPI) && !this.isFighter
