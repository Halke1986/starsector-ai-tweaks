package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.timeAdjustedVelocity
import org.lwjgl.util.vector.Vector2f

/** Simplified representation of a circular, moving target. */
data class BallisticTarget(val location: Vector2f, val velocity: Vector2f, val radius: Float) {
    companion object {
        fun entity(entity: CombatEntityAPI): BallisticTarget {
            return BallisticTarget(entity.location, entity.timeAdjustedVelocity, entity.collisionRadius)
        }

        fun shield(ship: ShipAPI): BallisticTarget {
            return BallisticTarget(ship.shieldCenterEvenIfNoShield, ship.timeAdjustedVelocity, ship.shieldRadiusEvenIfNoShield)
        }
    }
}
