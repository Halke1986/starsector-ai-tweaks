package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.core.utils.Bounds
import org.lwjgl.util.vector.Vector2f

val CombatEntityAPI.isValidTarget: Boolean
    get() = when {
        isExpired -> false

        this is CombatAsteroidAPI -> true

        owner == 100 -> false

        this is ShipAPI -> {
            isHullDamageable && isAlive
        }

        this is MissileAPI -> {
            collisionClass != CollisionClass.NONE && Global.getCombatEngine().isMissileAlive(this)
        }

        else -> false
    }

/** Is the entity a true ship, not a missile or fighter. */
val CombatEntityAPI.isShip: Boolean
    get() = (this is ShipAPI) && !isFighter

val CombatEntityAPI.isFighter: Boolean
    get() = (this is ShipAPI) && isFighter

val CombatEntityAPI.isSupportFighter: Boolean
    get() = (this is ShipAPI) && wing?.spec?.isSupport == true

val CombatEntityAPI.isMissile: Boolean
    get() = this is MissileAPI

val CombatEntityAPI.isPDTarget: Boolean
    get() = isMissile || isFighter

val CombatEntityAPI.timeAdjustedVelocity: Vector2f
    get() = (this as? ShipAPI)?.timeAdjustedVelocity ?: velocity

/** True if otherEntity is hostile towards this entity. */
fun CombatEntityAPI.isHostile(otherEntity: CombatEntityAPI): Boolean {
    return owner xor otherEntity.owner == 1
}

val CombatEntityAPI.boundsRadius: Float
    get() = Bounds.radius(this)

val CombatEntityAPI.hasShield: Boolean
    get() = when {
        !isShip -> false

        // Workaround for hulks retaining outdated ShieldAPI.
        (this as ShipAPI).isHulk -> false

        shield == null -> false

        else -> true
    }
