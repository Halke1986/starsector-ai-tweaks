package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.combat.trackers.VelocityTracker.Companion.smoothedVelocity
import org.lwjgl.util.vector.Vector2f

/** Simplified representation of a circular, moving target. */
data class BallisticTarget(val velocity: Vector2f, val location: Vector2f, val radius: Float) {
    companion object {
        fun entity(entity: CombatEntityAPI): BallisticTarget {
            return BallisticTarget(entity.smoothedVelocity, entity.location, entity.collisionRadius)
        }

        fun shield(ship: ShipAPI): BallisticTarget {
            return BallisticTarget(ship.smoothedVelocity, ship.shieldCenterEvenIfNoShield, ship.shieldRadiusEvenIfNoShield)
        }
    }
}
