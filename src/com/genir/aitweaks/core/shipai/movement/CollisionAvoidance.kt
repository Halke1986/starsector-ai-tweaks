package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.Preset
import com.genir.aitweaks.core.shipai.Preset.Companion.enemyCollisionSizeFactor
import com.genir.aitweaks.core.shipai.Preset.Companion.hulkCollisionSizeFactor
import com.genir.aitweaks.core.shipai.movement.Movement.Companion.movement
import com.genir.aitweaks.core.utils.DEGREES_TO_RADIANS
import com.genir.aitweaks.core.utils.PI
import com.genir.aitweaks.core.utils.distanceToOrigin
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.LinearMotion
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedX
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

@Suppress("MemberVisibilityCanBePrivate")
class CollisionAvoidance(val ai: CustomShipAI) {
    private val movement: Movement = ai.ship.movement

    /** Limit allows to restrict velocity to not exceed
     * max speed in a direction along a given heading. */
    data class Limit(
        val direction: Direction,
        val speedLimit: Float,
        val obstacle: Movement?,
    ) {
        /**
         * Clamp expectedSpeed to maximum speed in which ship can travel
         * along the expectedHeading and not break the Limit.
         *
         * From right triangle, the equation for max speed is:
         * maxSpeed = speedLimit / cos( abs(limitFacing - velocityFacing) )
         *
         * To avoid using trigonometric functions, f(x) = 1/cos(x) is approximated as
         * g(t) = 1/t(x) + t(x)/5 where t(x) = PI/2 - x
         */
        fun clampSpeed(expectedHeading: Direction, expectedSpeed: Float): Float {
            val angleFromLimit = (expectedHeading - direction).length
            if (angleFromLimit >= 90f) {
                return expectedSpeed
            }

            val t = PI / 2f - angleFromLimit * DEGREES_TO_RADIANS
            val e = speedLimit * (1f / t + t / 5f)
            return min(max(0f, e), expectedSpeed)
        }
    }

    fun gatherSpeedLimits(dt: Float): List<Limit> {
        // Calculate speed limits.
        val collisionLimits = avoidCollisions(dt)
        val borderLimit = avoidBorder()
        val targetLimit = avoidManeuverTarget(dt)

        return (listOf(borderLimit, targetLimit) + collisionLimits).filterNotNull()
    }

    private fun avoidCollisions(dt: Float): List<Limit?> {
        val obstacles: Sequence<ShipAPI> = findRelevantObstacles()

        return obstacles.map { obstacle: ShipAPI ->
            val distanceFactor = when {
                // Hulks have big collision radii. Don't be afraid to get close.
                obstacle.isHulk -> {
                    0.8f
                }

                // Allow tighter formations for backing off ships.
                ai.ventModule.isBackingOff || obstacle.aiFlags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF) -> {
                    0.9f
                }

                // Do not respect vanilla AI.
                obstacle.root.isFrigate && obstacle.customShipAI == null -> {
                    0.8f
                }

                // Allow priority ships to squeeze between allies in formation.
                movement.ship.movementPriority > obstacle.movementPriority -> {
                    0.9f
                }

                // Leave some space between ships. This prevents blocking
                // line of fire and allows for formation flexibility.
                else -> {
                    1.4f
                }
            }

            val minDistance = (ai.stats.totalCollisionRadius + obstacle.totalCollisionRadius) * distanceFactor

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

    private fun avoidManeuverTarget(dt: Float): Limit? {
        val target: ShipAPI = ai.maneuverTarget ?: return null

        // Ship can approach the target when on an assignment on when backing off.
        val ignore: Boolean = when {
            // Target is too large to ignore.
            target.mass * enemyCollisionSizeFactor > movement.ship.mass -> {
                false
            }

            ai.ventModule.isBackingOff -> {
                true
            }

            ai.assignment.eliminate != null -> {
                true
            }

            ai.assignment.navigateTo != null && !ai.assignment.arrivedAt -> {
                true
            }

            else -> {
                false
            }
        }


        return vMaxToObstacle(dt, target.movement.linearMotion, ai.attackRange * 0.85f, target)
    }

    private fun avoidBorder(): Limit? {
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
        return Limit(borderIntrusion.facing, movement.maxSpeed * (1f - avoidForce), null)
    }

    /** Calculate maximum velocity that will not lead to collision with an obstacle. */
    fun vMaxToObstacle(dt: Float, obstacleMotion: LinearMotion, minDistance: Float, obstacle: ShipAPI?): Limit? {
        val toObstacle = obstacleMotion.position - movement.location
        val toObstacleFacing = toObstacle.facing
        val r = (-toObstacleFacing).rotationMatrix

        // If the ships maintain their current course, they will not collide.
        val predictedMinDistance = distanceToOrigin(toObstacle, obstacleMotion.velocity - movement.velocity)
        if (predictedMinDistance > minDistance * 1.5f) {
            return null
        }

        val distance = toObstacle.rotatedX(r)
        val distanceLeft = distance - minDistance

        val decelShip = movement.collisionDeceleration(toObstacleFacing)
        val vMax = BasicEngineController.vMax(dt, abs(distanceLeft), decelShip) * distanceLeft.sign
        val vObstacle = obstacleMotion.velocity.rotatedX(r)
        val speedLimit = vMax + vObstacle

        return Limit(toObstacleFacing, speedLimit, obstacle?.movement)
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
