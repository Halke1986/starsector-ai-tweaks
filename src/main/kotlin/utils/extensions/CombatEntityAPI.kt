package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.CombatEntityAPI
import org.lwjgl.util.vector.Vector2f

val CombatEntityAPI.radius: Float
    get() = this.shield?.radius ?: this.collisionRadius

val CombatEntityAPI.aimLocation: Vector2f
    get() = this.shield?.location ?: this.location