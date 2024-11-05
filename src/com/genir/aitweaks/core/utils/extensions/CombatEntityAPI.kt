package com.genir.aitweaks.core.utils.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAsteroidAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import org.lwjgl.util.vector.Vector2f

/** Is the entity a true ship, not a missile or fighter. */
val CombatEntityAPI.isShip: Boolean
    get() = (this is ShipAPI) && !isFighter

val CombatEntityAPI.isValidTarget: Boolean
    get() = when {
        isExpired -> false

        this is CombatAsteroidAPI -> true

        owner == 100 -> false

        this is ShipAPI -> isHullDamageable && isAlive

        this is MissileAPI -> Global.getCombatEngine().isMissileAlive(this)

        else -> false
    }

val CombatEntityAPI.isFighter: Boolean
    get() = (this is ShipAPI) && isFighter

val CombatEntityAPI.isSupportFighter: Boolean
    get() = (this is ShipAPI) && wing?.spec?.isSupport == true

val CombatEntityAPI.isPDTarget: Boolean
    get() = this is MissileAPI || (this is ShipAPI) && isFighter

val CombatEntityAPI.timeAdjustedVelocity: Vector2f
    get() = (this as? ShipAPI)?.timeAdjustedVelocity ?: velocity