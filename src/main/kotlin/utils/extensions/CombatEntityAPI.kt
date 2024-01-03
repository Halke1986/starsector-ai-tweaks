package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import org.lwjgl.util.vector.Vector2f

val CombatEntityAPI.radius: Float
    get() = this.aliveShield?.radius ?: this.collisionRadius

val CombatEntityAPI.aimLocation: Vector2f
    get() = this.aliveShield?.location ?: this.location

/** Hulks inherit ship's ShieldAPI, which returns outdated values.
 * aliveShield returns null for dead ships, to avoid this problem. */
val CombatEntityAPI.aliveShield: ShieldAPI?
    get() = if ((this as? ShipAPI)?.isAlive == true) this.shield else null

val CombatEntityAPI.isShip: Boolean
    get() = (this is ShipAPI) && !this.isFighter