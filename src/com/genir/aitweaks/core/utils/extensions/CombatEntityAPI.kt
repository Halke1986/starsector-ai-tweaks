package com.genir.aitweaks.core.utils.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI

/** Is the entity a true ship, not a missile or fighter. */
val CombatEntityAPI.isShip: Boolean
    get() = (this is ShipAPI) && !isFighter

val CombatEntityAPI.isValidTarget: Boolean
    get() = when {
        owner == 100 -> false

        isExpired -> false

        this is ShipAPI -> isHullDamageable && isAlive

        this is MissileAPI -> Global.getCombatEngine().isMissileAlive(this)

        else -> false
    }

val CombatEntityAPI.isSupportFighter: Boolean
    get() = (this is ShipAPI) && wing?.spec?.isSupport == true