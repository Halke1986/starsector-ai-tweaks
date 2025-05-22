package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.boundsRadius
import com.genir.aitweaks.core.extensions.facing
import com.genir.aitweaks.core.extensions.length
import com.genir.aitweaks.core.extensions.root
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.utils.Grid
import com.genir.aitweaks.core.utils.types.Arc

class ObstacleList(private val weapon: WeaponHandle, radius: Float, private val params: BallisticParams) {
    private data class Obstacle(val arc: Arc, val dist: Float, val origin: CombatEntityAPI)

    private val obstacles: List<Obstacle> = run {
        val ships: Sequence<ShipAPI> = Grid.ships(weapon.location, radius).filter {
            when {
                // Same ship.
                it.root == weapon.ship.root -> false

                it.isFighter -> false

                // Weapon fires over allies.
                weapon.noFF && it.owner == weapon.ship.owner -> false

                else -> true
            }
        }

        val obstacles: Sequence<Obstacle> = ships.map { ship ->
            val target = BallisticTarget(ship.location, ship.velocity, ship.boundsRadius * 0.8f)

            return@map Obstacle(
                arc = interceptArc(weapon, target, params),
                dist = intercept(weapon, target, params).length,
                origin = ship,
            )
        }

        return@run obstacles.toList()
    }

    fun isOccluded(target: CombatEntityAPI): Boolean {
        val ballisticTarget = BallisticTarget.collisionRadius(target)
        val intercept = intercept(weapon, ballisticTarget, params)

        return obstacles.any { obstacle ->
            when {
                obstacle.origin == target -> false

                !obstacle.arc.contains(intercept.facing) -> false

                obstacle.dist > intercept.length -> false

                else -> true
            }
        }
    }
}