package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.Preset
import com.genir.aitweaks.core.shipai.Preset.Companion.hulkSizeFactor
import com.genir.aitweaks.core.shipai.movement.Kinematics.Companion.kinematics
import com.genir.aitweaks.core.utils.DEGREES_TO_RADIANS
import com.genir.aitweaks.core.utils.PI
import com.genir.aitweaks.core.utils.distanceToOrigin
import com.genir.aitweaks.core.utils.getShortestRotation
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotatedX
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

@Suppress("MemberVisibilityCanBePrivate")
class CollisionAvoidance(val ai: CustomShipAI) {
    private val kinematics: Kinematics = ai.ship.kinematics

    /** Limit allows to restrict velocity to not exceed
     * max speed in a direction along a given heading. */
    data class Limit(
        val direction: Direction,
        val speedLimit: Float,
        val obstacle: Kinematics?,
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
        val targetLimit = avoidManeuverTarget(dt)
        val collisionLimits = avoidCollisions(dt)
        val borderLimit = avoidBorder()

        return (listOf(targetLimit, borderLimit) + collisionLimits).filterNotNull()
    }

    private fun avoidManeuverTarget(dt: Float): Limit? {
        // Ship can approach the target when on an assignment on when backing off.
        when {
            ai.ventModule.isBackingOff -> return null

            ai.assignment.eliminate != null -> return null

            ai.assignment.navigateTo != null && !ai.assignment.arrivedAt -> return null
        }

        val target: ShipAPI = ai.maneuverTarget ?: return null
        return vMaxToObstacle(dt, target.kinematics, ai.attackRange * 0.9f)
    }

    private fun avoidCollisions(dt: Float): List<Limit?> {
        val obstacles: List<ShipAPI> = Global.getCombatEngine().ships.filter {
            when {
                it.root == kinematics.ship.root -> false
                it.collisionClass != CollisionClass.SHIP -> false

                // Modules and drones count towards
                // their parent collision radius.
                it.isModule -> false
                it.isDrone -> false

                // Ignore frigates. Let them move out of the way.
                it.isFrigate -> false

                // Large hulks.
                it.owner == 100 && it.mass / kinematics.ship.mass > hulkSizeFactor -> true

                // Allies
                it.owner == kinematics.ship.owner -> true

                else -> false
            }
        }

        val shipPriority = kinematics.ship.movementPriority

        return obstacles.map { obstacle ->
            val shipHasPriority = shipPriority > obstacle.movementPriority

            val distanceFactor = when {
                // Hulks have big collision radii. Don't be afraid to get close.
                obstacle.isHulk -> {
                    0.8f
                }

                // Allow tighter formations for backing off ships.
                ai.ventModule.isBackingOff || obstacle.aiFlags.hasFlag(AIFlags.BACKING_OFF) -> {
                    0.9f
                }

                // Allow priority ships to squeeze between allies in formation.
                shipHasPriority -> {
                    0.9f
                }

                // Leave some space between ships. This prevents blocking
                // line of fire and allows for formation flexibility.
                else -> {
                    1.4f
                }
            }

            val minDistance = (ai.stats.totalCollisionRadius + obstacle.totalCollisionRadius) * distanceFactor
            vMaxToObstacle(dt, obstacle.kinematics, minDistance)
        }
    }

    private fun avoidBorder(): Limit? {
        ai.isAvoidingBorder = false

        val mapH = Global.getCombatEngine().mapHeight / 2f
        val mapW = Global.getCombatEngine().mapWidth / 2f
        val borderZone = Preset.borderNoGoZone + Preset.borderCornerRadius

        // Translate ship coordinates so that the ship appears to be
        // always near a map corner. That way we can use a circle
        // calculations to approximate a rectangle with rounded corners.
        val l = kinematics.location

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
        val tgtLoc = ai.maneuverTarget?.location
        if (tgtLoc != null && !ai.ventModule.isBackingOff && (getShortestRotation(tgtLoc - kinematics.location, borderIntrusion)).length < 90f) {
            return null
        }

        ai.isAvoidingBorder = true

        // The closer the ship is to map edge, the stronger
        // the heading transformation away from the border.
        val avoidForce = (d / (Preset.borderNoGoZone - Preset.borderHardNoGoZone)).coerceAtMost(1f)
        return Limit(borderIntrusion.facing, kinematics.maxSpeed * (1f - avoidForce), null)
    }

    /** Calculate maximum velocity that will not lead to collision with an obstacle. */
    fun vMaxToObstacle(dt: Float, obstacle: Kinematics, minDistance: Float): Limit? {
        val toObstacle = obstacle.location - kinematics.location
        val toObstacleFacing = toObstacle.facing
        val r = (-toObstacleFacing).rotationMatrix

        // If the ships maintain their current course, they will not collide.
        val predictedMinDistance = distanceToOrigin(toObstacle, obstacle.velocity - kinematics.velocity)
        if (predictedMinDistance > minDistance * 1.5f) {
            return null
        }

        val distance = toObstacle.rotatedX(r)
        val distanceLeft = distance - minDistance

        val decelShip = kinematics.collisionDeceleration(toObstacleFacing)
        val vMax = BasicEngineController.vMax(dt, abs(distanceLeft), decelShip) * distanceLeft.sign
        val vObstacle = obstacle.velocity.rotatedX(r)
        val speedLimit = vMax + vObstacle

        return Limit(toObstacleFacing, speedLimit, obstacle)
    }

    companion object {
        val ShipAPI.movementPriority: Float
            get() {
                val ai = customShipAI

                return when {
                    root.isStation -> {
                        10f
                    }

                    isHulk -> {
                        10f
                    }

                    engineController?.isFlamedOut == true -> {
                        10f
                    }

                    this == Global.getCombatEngine().playerShip -> {
                        10f
                    }

                    this == Global.getCombatEngine().playerShip && isUnderManualControl -> {
                        3f
                    }

                    root.isFrigate -> {
                        -1f
                    }

                    aiFlags.hasFlag(AIFlags.BACKING_OFF) -> {
                        2f
                    }

                    ai == null -> {
                        0f
                    }

                    ai.assignment.eliminate != null -> {
                        1f
                    }

                    ai.assignment.navigateTo != null && !ai.assignment.arrivedAt -> {
                        1f
                    }

                    else -> {
                        0f
                    }
                }
            }
    }
}
