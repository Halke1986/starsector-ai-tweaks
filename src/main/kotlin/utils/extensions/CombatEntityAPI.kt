package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import org.lwjgl.util.vector.Vector2f

val CombatEntityAPI.radius: Float
    get() = if (this.aimAtShield) this.shield.radius else this.collisionRadius

val CombatEntityAPI.aimLocation: Vector2f
    get() = if (this.aimAtShield) this.shield.location else this.location

val CombatEntityAPI.trueShield: ShieldAPI?
    get() = when {
        this !is ShipAPI -> null
        this.isHulk -> null
        else -> this.shield
    }

private val CombatEntityAPI.aimAtShield: Boolean
    get() = when {
        this !is ShipAPI -> false
        this.isHulk -> false // hulks inherit ship's ShieldAPI, but don't update it
        this.isStationModule -> false // station module location and shield location are significantly offset
        else -> this.shield != null
    }

val CombatEntityAPI.isShip: Boolean
    get() = (this is ShipAPI) && !this.isFighter