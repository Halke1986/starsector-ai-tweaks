package com.genir.aitweaks.core.shipai.autofire.ballistics

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.timeAdjustedVelocity
import com.genir.aitweaks.core.utils.types.LinearMotion
import org.lwjgl.util.vector.Vector2f

/** Simplified representation of a circular, moving target. */
data class BallisticTarget(
    val location: Vector2f,
    val velocity: Vector2f,
    val radius: Float,
    val entity: CombatEntityAPI,
) {
    val linearMotion: LinearMotion = LinearMotion(
        position = location,
        velocity = velocity,
    )

    companion object {
        fun collisionRadius(entity: CombatEntityAPI): BallisticTarget {
            return BallisticTarget(
                entity.location,
                entity.timeAdjustedVelocity,
                entity.collisionRadius,
                entity,
            )
        }

        fun shieldRadius(ship: ShipAPI): BallisticTarget {
            return BallisticTarget(
                ship.shieldCenterEvenIfNoShield,
                ship.timeAdjustedVelocity,
                ship.shieldRadiusEvenIfNoShield,
                ship,
            )
        }
    }
}