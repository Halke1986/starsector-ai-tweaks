package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.Preset
import com.genir.aitweaks.core.shipai.Preset.Companion.enemyCollisionSizeFactor
import com.genir.aitweaks.core.shipai.Preset.Companion.hulkCollisionSizeFactor
import com.genir.aitweaks.core.shipai.movement.Movement.Companion.movement
import com.genir.aitweaks.core.utils.types.Direction
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
        val shipLimits = avoidShips(dt)
        val borderLimit = avoidBorder()
        val targetLimit = avoidManeuverTarget(dt)
        val missileLimits = avoidMissiles(dt)
        val asteroidLimits = avoidAsteroids(dt)

        return (listOf(borderLimit, targetLimit) + shipLimits + missileLimits + asteroidLimits).filterNotNull()
    }

    private fun avoidShips(dt: Float): List<SpeedLimit?> {
        return findRelevantShips().map { obstacle: ShipAPI ->
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

                // Do not respect vanilla AI frigates.
                obstacle.root.isFrigate && obstacle.customShipAI == null -> {
                    shieldDistance
                }

                // Allow tighter formations for backing off ships.
                ai.ventModule.isBackingOff -> {
                    10f + shieldDistance
                }

                // Allow priority ships to squeeze between allies in formation.
                // NOTE: This condition is asymmetric. Priority ship should approach other
                // ships beyond their collision avoidance range, forcing them to make way.
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

    private fun findRelevantShips(): Sequence<ShipAPI> {
        return Global.getCombatEngine().ships.asSequence().filter { obstacle ->
            when {
                // Same ship.
                obstacle.root == movement.ship.root -> false

                obstacle.isFighter -> false

                // Do not avoid ships exhibiting fighter-like behavior, unless the avoiding ship also exhibits fighter-like behavior.
                obstacle.collisionClass != CollisionClass.SHIP && movement.ship.collisionClass == CollisionClass.SHIP -> false

                // Modules and drones count towards
                // their parent collision radius.
                obstacle.isModule -> false
                obstacle.isDrone -> false

                // Large hulks.
                obstacle.isHulk && (obstacle.mass * hulkCollisionSizeFactor > movement.ship.mass) -> true

                // Allies.
                obstacle.owner == movement.ship.owner -> true

                else -> false
            }
        }
    }

    private val ShipAPI.movementPriority: Int
        get() {
            val ai = customShipAI

            return when {
                aiFlags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF) -> {
                    10
                }

                ai == null -> {
                    0
                }

                root.isFrigate -> {
                    1
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

    private fun avoidMissiles(dt: Float): List<SpeedLimit?> {
        val speedThreshold = movement.maxSpeed
        return Global.getCombatEngine().missiles.mapNotNull { missile: MissileAPI ->
            // Filter irrelevant missiles.
            when {
                !missile.isValidTarget -> return@mapNotNull null

                missile.owner == ai.ship.owner -> return@mapNotNull null

                // Always avoid mines.
                missile.isMine -> Unit

                // Smaller ships avoid all missiles, but only when backing off.
                (movement.ship.root.isFrigate || movement.ship.root.isDestroyer) && ai.ventModule.isBackingOff -> Unit

                else -> return@mapNotNull null
            }

            // Ignore missiles that are blocking the retreat vector.
            if (ai.ventModule.isBackingOff && !missile.isMine) {
                val toDestination: Vector2f = ai.maneuver.destination - movement.location
                val toMissile: Vector2f = missile.location - movement.location

                if ((toDestination.facing - toMissile.facing).length < 60f) {
                    return@mapNotNull null
                }
            }

            val missileMotion = LinearMotion(missile.location, missile.velocity)
            val missileRadius = maxOf(missile.mineExplosionRange, missile.collisionRadius * 5f, 150f)
            val minDistance = ai.stats.totalCollisionRadius + missileRadius

            val vMax = vMaxToObstacle(dt, missileMotion, minDistance, missile)
            if (vMax.speedLimit > speedThreshold) {
                return@mapNotNull null
            }

            return@mapNotNull vMax
        }
    }

    private fun avoidAsteroids(dt: Float): List<SpeedLimit?> {
        if (movement.ship.collisionClass != CollisionClass.SHIP) {
            return listOf()
        }

        val speedThreshold = movement.maxSpeed
        val massThreshold = movement.ship.mass * 1.5f
        return Global.getCombatEngine().asteroids.asSequence().mapNotNull { asteroid ->
            if (asteroid.mass < massThreshold) {
                return@mapNotNull null
            }

            val asteroidMotion = LinearMotion(asteroid.location, asteroid.velocity)
            val minDistance = movement.ship.shieldRadiusEvenIfNoShield + asteroid.collisionRadius + 30f
            val vMax = vMaxToObstacle(dt, asteroidMotion, minDistance, asteroid)
            if (vMax.speedLimit > speedThreshold) {
                return@mapNotNull null
            }

            return@mapNotNull vMax
        }.toList()
    }

    private fun avoidManeuverTarget(dt: Float): SpeedLimit? {
        val target: ShipAPI = ai.maneuverTarget?.root
            ?: return null

        // Ship can approach the target when backing off, unless the target is too large.
        if (ai.ventModule.isBackingOff && target.mass * enemyCollisionSizeFactor < movement.ship.mass) {
            return null
        }

        val collisionDistance: Float = ai.stats.totalCollisionRadius + target.totalCollisionRadius
        val distance: Float = maxOf(ai.attackRange * 0.85f, collisionDistance)

        return vMaxToObstacle(dt, target.movement.linearMotion, distance, target)
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
            if (!ai.ventModule.isBackingOff && (toTarget.facing - borderIntrusion.facing).length < 135f) {
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
    private fun vMaxToObstacle(dt: Float, obstacleMotion: LinearMotion, minDistance: Float, obstacle: CombatEntityAPI?): SpeedLimit {
        val toObstacle = obstacleMotion.position - movement.location
        val toObstacleFacing = toObstacle.facing
        val r = (-toObstacleFacing).rotationMatrix

        val distance = toObstacle.rotatedX(r)
        val distanceLeft = distance - minDistance

        // Obstacle is moving towards the ship. If the obstacle is an ally,
        // assume both ships will try to avoid the collision.
        val decelObstacle = if (obstacle is ShipAPI && obstacle.owner == ai.ship.owner) {
            collisionDeceleration(obstacle.movement, -toObstacleFacing)
        } else {
            0f
        }

        val decelShip = collisionDeceleration(movement, toObstacleFacing)
        val vMax = EngineController.vMax(dt, abs(distanceLeft), decelShip + decelObstacle) * distanceLeft.sign
        val vObstacle = obstacleMotion.velocity.rotatedX(r)
        val speedLimit = vMax + vObstacle

        return SpeedLimit(toObstacleFacing, speedLimit, obstacle)
    }

    /** Ship deceleration for collision avoidance purposes. */
    fun collisionDeceleration(movement: Movement, collisionFacing: Direction): Float {
        val angleFromBow = (collisionFacing - movement.facing).length

        // The returned value is not the true effective deceleration.
        // True effective deceleration would be the sum of all thrust vector projections
        // onto the collision vector. Instead, this method returns a weighted and capped
        // aggregate to prevent abrupt changes, which could otherwise cause unstable
        // anti-collision behavior.
        if (angleFromBow < 90f) {
            val decelWeight = 90f - angleFromBow
            val strafeWeight = angleFromBow
            return (movement.deceleration * decelWeight + strafeWeight * movement.strafeAcceleration) / 90f
        } else {
            val accelWeight = angleFromBow - 90f
            val strafeWeight = 180f - angleFromBow
            return (movement.acceleration * accelWeight + strafeWeight * movement.strafeAcceleration) / 90f
        }
    }
}
