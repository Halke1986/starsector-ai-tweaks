package com.genir.aitweaks.core.features.shipai.autofire.ballistics

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.utils.extensions.timeAdjustedVelocity
import org.lwjgl.util.vector.Vector2f

/** Simplified representation of a circular, moving target. */
data class Target(val location: Vector2f, val velocity: Vector2f, val radius: Float) {
    companion object {
        fun entity(entity: CombatEntityAPI): Target {
            return Target(entity.location, entity.timeAdjustedVelocity, entity.collisionRadius)
        }

        fun shield(ship: ShipAPI): Target {
            return Target(ship.shieldCenterEvenIfNoShield, ship.timeAdjustedVelocity, ship.shieldRadiusEvenIfNoShield)
        }
    }
}
