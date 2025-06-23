package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
import com.genir.aitweaks.core.utils.types.LinearMotion
import org.lazywizard.lazylib.ext.isZeroVector
import org.lwjgl.util.vector.Vector2f

val DamagingProjectileAPI.Velocity: Vector2f
    get() {
        // Workaround for vanilla bug where projectile velocity returns
        // zero vector in the frame the energy projectile was spawned.
        if (elapsed == 0f && velocity.isZeroVector()) {
            val shipVelocity = weapon?.ship?.velocity ?: Vector2f()
            return shipVelocity + facing.direction.unitVector * moveSpeed
        }

        return velocity
    }

val DamagingProjectileAPI.linearMotion: LinearMotion
    get() = LinearMotion(
        position = location,
        velocity = Velocity
    )
