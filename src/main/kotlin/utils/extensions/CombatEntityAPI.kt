package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import org.lwjgl.util.vector.Vector2f

/** Entity radius may be either approximated by shield radius or collision radius. */
val CombatEntityAPI.aimRadius: Float
    get() = if (this.approximateByShield) (this as ShipAPI).shieldRadiusEvenIfNoShield else this.collisionRadius

/** Entity location may be either approximated by its shield location or true location. */
val CombatEntityAPI.aimLocation: Vector2f
    get() = if (this.approximateByShield) (this as ShipAPI).shieldCenterEvenIfNoShield else this.location

val CombatEntityAPI.trueShield: ShieldAPI?
    get() = when {
        this !is ShipAPI -> null
        this.isHulk -> null // hulks inherit ship's ShieldAPI, but don't update it
        else -> this.shield
    }

val CombatEntityAPI.isShip: Boolean
    get() = (this is ShipAPI) && !this.isFighter

private val CombatEntityAPI.approximateByShield: Boolean
    get() = when {
        this !is ShipAPI -> false
        this.isHulk -> false // hulks inherit ship's ShieldAPI, but don't update it
        this.isStationModule -> false // station module location and shield location are significantly offset
        else -> true
    }