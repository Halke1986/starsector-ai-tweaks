package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.Preset
import com.genir.aitweaks.core.shipai.Preset.Companion.enemyCollisionSizeFactor
import com.genir.aitweaks.core.shipai.Preset.Companion.hulkCollisionSizeFactor
import com.genir.aitweaks.core.shipai.movement.Movement.Companion.movement
import com.genir.aitweaks.core.utils.distanceToOrigin
import com.genir.aitweaks.core.utils.types.LinearMotion
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedX
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.sign

@Suppress("MemberVisibilityCanBePrivate")
class CollisionAvoidance(val ai: CustomShipAI) {
    private val movement: Movement = ai.ship.movement

    fun gatherSpeedLimits(dt: Float): List<SpeedLimit> {
        // Calculate speed limits.
        val collisionLimits = avoidCollisions(dt)
        val borderLimit = avoidBorder()
        val targetLimit = avoidManeuverTarget(dt)
        val missileLimits = avoidMissiles(dt)

        return (listOf(borderLimit, targetLimit) + collisionLimits + missileLimits).filterNotNull()
    }

    private fun avoidCollisions(dt: Float): List<SpeedLimit?> {
        val obstacles: Sequence<ShipAPI> = findRelevantObstacles()

        return obstacles.map { obstacle: ShipAPI ->
            val collisionDistance: Float = ai.stats.totalCollisionRadius + obstacle.totalCollisionRadius
            val shieldDistance: Float = ai.ship.shieldRadiusEvenIfNoShield + obstacle.shieldRadiusEvenIfNoShield

            val minDistance: Float = when {
                // Don't get near stations, to account for their
                // complicated shape with multiple collision hazards.
                obstacle.isStation -> {
                    1.1f * collisionDistance
                }

                // Hulks have big collision radii. Don't be afraid to get close.
                obstacle.isHulk -> {
                    0.8f * collisionDistance
                }

                // Allow tighter formations for backing off ships.
                // NOTE: This condition is asymmetric. Backing off ship should approach other
                // ships beyond their collision avoidance range, forcing them to make way.
                ai.ventModule.isBackingOff -> {
                    10f + shieldDistance
                }

                // Do not respect vanilla AI frigates.
                obstacle.root.isFrigate && obstacle.customShipAI == null -> {
                    shieldDistance
                }

                // Allow priority ships to squeeze between allies in formation.
                movement.ship.movementPriority > obstacle.movementPriority -> {
                    10f + shieldDistance
                }

                // Leave some space between ships. This prevents blocking
                // line of fire and allows for formation flexibility.
                else -> {
                    1.4f * collisionDistance
                }
            }

            return@map vMaxToObstacle(dt, obstacle.movement.linearMotion, minDistance, obstacle)
        }.toList()
    }

    private fun findRelevantObstacles(): Sequence<ShipAPI> {
        return Global.getCombatEngine().ships.asSequence().filter {
            when {
                it.root == movement.ship.root -> false
                it.collisionClass != CollisionClass.SHIP -> false

                // Modules and drones count towards
                // their parent collision radius.
                it.isModule -> false
                it.isDrone -> false

                // Large hulks.
                it.isHulk && (it.mass * hulkCollisionSizeFactor > movement.ship.mass) -> true

                // Allies.
                it.owner == movement.ship.owner -> true

                else -> false
            }
        }
    }

    private fun avoidMissiles(dt: Float): List<SpeedLimit?> {
        val allMissiles: Sequence<MissileAPI> = Global.getCombatEngine().missiles.asSequence()

        val relevantMissiles: Sequence<MissileAPI> = allMissiles.filter { missile: MissileAPI ->
            when {
                // Avoid mines only.
                !missile.isMine -> false

                !missile.isValidTarget -> false

                else -> true
            }
        }

        return relevantMissiles.map { missile: MissileAPI ->
            val minDistance = ai.stats.totalCollisionRadius + missile.mineExplosionRange + 30f

            return@map vMaxToObstacle(dt, LinearMotion(missile.location, missile.velocity), minDistance, null)
        }.toList()
    }

    private fun avoidManeuverTarget(dt: Float): SpeedLimit? {
        val target: ShipAPI = ai.maneuverTarget ?: return null

        // Ship can approach the target when backing off, unless the target is too large.
        if (ai.ventModule.isBackingOff && target.mass * enemyCollisionSizeFactor < movement.ship.mass) {
            return null
        }

        return vMaxToObstacle(dt, target.movement.linearMotion, ai.attackRange * 0.85f, target)
    }

    private fun avoidBorder(): SpeedLimit? {
        ai.isAvoidingBorder = false

        val mapH = Global.getCombatEngine().mapHeight / 2f
        val mapW = Global.getCombatEngine().mapWidth / 2f
        val borderZone = Preset.borderNoGoZone + Preset.borderCornerRadius

        // Translate ship coordinates so that the ship appears to be
        // always near a map corner. That way we can use a circle
        // calculations to approximate a rectangle with rounded corners.
        val l = movement.location

        val borderIntrusion = Vector2f(
            if (l.x > 0) {
                (l.x - mapW + borderZone).coerceAtLeast(0f)
            } else {
                (l.x + mapW - borderZone).coerceAtMost(0f)
            },
            if (l.y > 0) {
                (l.y - mapH + borderZone).coerceAtLeast(0f)
            } else {
                (l.y + mapH - borderZone).coerceAtMost(0f)
            },
        )

        // Distance into the border zone.
        val d = (borderIntrusion.length - Preset.borderCornerRadius).coerceAtLeast(0f)

        // Ship is far from border, no avoidance required.
        if (d == 0f) {
            return null
        }

        // Allow chasing targets into the border zone.
        val target = ai.maneuverTarget
        if (target != null) {
            val toTarget = target.location - movement.location
            if (!ai.ventModule.isBackingOff && (toTarget.facing - borderIntrusion.facing).length < 90f) {
                return null
            }
        }

        ai.isAvoidingBorder = true

        // The closer the ship is to map edge, the stronger
        // the heading transformation away from the border.
        val avoidForce = (d / (Preset.borderNoGoZone - Preset.borderHardNoGoZone)).coerceAtMost(1f)
        return SpeedLimit(borderIntrusion.facing, movement.maxSpeed * (1f - avoidForce), null)
    }

    /** Calculate maximum velocity that will not lead to collision with an obstacle. */
    fun vMaxToObstacle(dt: Float, obstacleMotion: LinearMotion, minDistance: Float, obstacle: ShipAPI?): SpeedLimit? {
        val toObstacle = obstacleMotion.position - movement.location
        val toObstacleFacing = toObstacle.facing
        val r = (-toObstacleFacing).rotationMatrix

        val distance = toObstacle.rotatedX(r)
        val distanceLeft = distance - minDistance

        // Obstacle is moving towards the ship. If the obstacle is a friendly,
        // non flamed out ship, assume both ship will try to avoid the collision.
        val decelObstacle = if (obstacle?.owner == ai.ship.owner && !obstacle.engineController.isFlamedOut) {
            movement.collisionDeceleration(-toObstacleFacing)
        } else {
            0f
        }

        val decelShip = movement.collisionDeceleration(toObstacleFacing)
        val vMax = EngineController.vMax(dt, abs(distanceLeft), decelShip + decelObstacle) * distanceLeft.sign
        val vObstacle = obstacleMotion.velocity.rotatedX(r)
        val speedLimit = vMax + vObstacle

        return SpeedLimit(toObstacleFacing, speedLimit, obstacle?.movement)
    }

    companion object {
        val ShipAPI.movementPriority: Int
            get() {
                val ai = customShipAI

                return when {
                    root.isStation -> {
                        10
                    }

                    isHulk -> {
                        10
                    }

                    engineController?.isFlamedOut == true -> {
                        10
                    }

                    this == Global.getCombatEngine().playerShip && isUnderManualControl -> {
                        5
                    }

                    aiFlags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF) -> {
                        2
                    }

                    ai == null -> {
                        0
                    }

                    ai.assignment.eliminate != null -> {
                        1
                    }

                    ai.assignment.navigateTo != null && !ai.assignment.arrivedAt -> {
                        1
                    }

                    else -> {
                        0
                    }
                }
            }
    }
}
