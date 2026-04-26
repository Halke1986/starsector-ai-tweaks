package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.shipai.autofire.ballistics.BallisticParams
import com.genir.aitweaks.core.shipai.autofire.ballistics.BallisticTarget
import com.genir.aitweaks.core.shipai.global.TargetTracker
import com.genir.aitweaks.core.shipai.movement.Movement.Companion.movement
import com.genir.aitweaks.core.utils.types.Arc

class ObstacleList(private val weapon: WeaponHandle, radius: Float, targetTracker: TargetTracker, private val params: BallisticParams) {
    private data class Obstacle(val arc: Arc, val dist: Float, val origin: CombatEntityAPI)

    private val obstacles: List<Obstacle> = run {
        val ships: List<ShipAPI> = targetTracker.getObstacles().filter {
            when {
                // Same ship.
                it.root == weapon.ship.root -> false

                (it.location - weapon.location).length > radius -> false

                // Weapon fires over allies.
                weapon.noFF && it.owner == weapon.ship.owner -> false

                else -> true
            }
        }

        val obstacles: List<Obstacle> = ships.map { ship ->
            val target = BallisticTarget(ship.location, ship.movement.velocity, ship.boundsRadius * 0.8f, ship)

            return@map Obstacle(
                arc = weapon.ballistics.interceptArc(target, params),
                dist = weapon.ballistics.intercept(target, params).length,
                origin = ship,
            )
        }

        return@run obstacles
    }

    fun isOccluded(target: CombatEntityAPI): Boolean {
        val ballisticTarget = BallisticTarget.collisionRadius(target)
        val intercept = weapon.ballistics.intercept(ballisticTarget, params)

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