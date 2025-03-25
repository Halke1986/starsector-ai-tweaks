package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.Preset.Companion.hulkSizeFactor
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedX
import com.genir.aitweaks.core.utils.getShortestRotation
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.sign

@Suppress("MemberVisibilityCanBePrivate")
class CollisionAvoidance(val ai: CustomShipAI) {
    private val ship: ShipAPI = ai.ship

    fun gatherSpeedLimits(dt: Float): List<EngineController.Limit> {
        val allObstacles = Global.getCombatEngine().ships.filter {
            when {
                it.root == ship.root -> false
                it.collisionClass != CollisionClass.SHIP -> false

                // Modules and drones count towards
                // their parent collision radius.
                it.isModule -> false
                it.isDrone -> false

                else -> true
            }
        }

        val allies = allObstacles.filter { it.owner == ship.owner && !it.root.isFrigate }
        val hulks = allObstacles.filter { it.owner == 100 && it.mass / ship.mass > hulkSizeFactor }

        // Calculate speed limits.
        val targetLimit = avoidManeuverTarget(dt)
        val collisionLimits = avoidCollisions(dt, allies + hulks)
        val borderLimit = avoidBorder()

        val limits: List<EngineController.Limit?> = listOf(targetLimit, borderLimit) + collisionLimits
        return limits.filterNotNull()
    }

    private fun avoidManeuverTarget(dt: Float): EngineController.Limit? {
        if (ai.ventModule.isBackingOff) {
            return null
        }

        val target: ShipAPI = ai.maneuverTarget ?: return null
        return vMaxToObstacle(dt, target, ai.attackRange * 0.9f)
    }

    private fun avoidCollisions(dt: Float, obstacles: List<ShipAPI>): List<EngineController.Limit?> {
        val shipPriority = ship.movementPriority

        return obstacles.map { obstacle ->
            val shipHasPriority = shipPriority > obstacle.movementPriority

            val distanceFactor = when {
                // Hulks have big collision radii. Don't be afraid to get close.
                obstacle.isHulk -> {
                    0.8f
                }

                // Allow tighter formations for backing off ships.
                ai.ventModule.isBackingOff || obstacle.aiFlags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF) -> {
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

            // Priority ships ignore collision risk until the collision is imminent.
            // This causes the ships to brake later, maintain higher speed, and push
            // through allied formations more aggressively.
            if (minDistance > 0 && shipHasPriority) {
                return@map null
            }

            vMaxToObstacle(dt, obstacle, minDistance)
        }
    }

    private fun avoidBorder(): EngineController.Limit? {
        ai.isAvoidingBorder = false

        val mapH = Global.getCombatEngine().mapHeight / 2f
        val mapW = Global.getCombatEngine().mapWidth / 2f
        val borderZone = Preset.borderNoGoZone + Preset.borderCornerRadius

        // Translate ship coordinates so that the ship appears to be
        // always near a map corner. That way we can use a circle
        // calculations to approximate a rectangle with rounded corners.
        val l = ship.location

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
        if (tgtLoc != null && !ai.ventModule.isBackingOff && (getShortestRotation(tgtLoc - ship.location, borderIntrusion)).length < 90f) {
            return null
        }

        ai.isAvoidingBorder = true

        // The closer the ship is to map edge, the stronger
        // the heading transformation away from the border.
        val avoidForce = (d / (Preset.borderNoGoZone - Preset.borderHardNoGoZone)).coerceAtMost(1f)
        return EngineController.Limit(borderIntrusion.facing, ship.maxSpeed * (1f - avoidForce), null)
    }

    /** Calculate maximum velocity that will not lead to collision with an obstacle. */
    fun vMaxToObstacle(dt: Float, obstacle: ShipAPI, minDistance: Float): EngineController.Limit {
        val toObstacle = obstacle.location - ship.location
        val toObstacleFacing = toObstacle.facing
        val r = (-toObstacleFacing).rotationMatrix

        val distance = toObstacle.rotatedX(r)
        val distanceLeft = distance - minDistance

        val decelShip = ship.collisionDeceleration(toObstacleFacing)
        val vMax = BasicEngineController.vMax(dt, abs(distanceLeft), decelShip) * distanceLeft.sign
        val vObstacle = obstacle.velocity.rotatedX(r)
        val speedLimit = vMax + vObstacle

        return EngineController.Limit(toObstacleFacing, speedLimit, obstacle)
    }

    /** Ship deceleration for collision avoidance purposes. */
    private fun ShipAPI.collisionDeceleration(collisionFacing: Direction): Float {
        val angleFromBow = (collisionFacing - facing.direction).length
        return when {
            angleFromBow < 30f -> deceleration
            angleFromBow < 150f -> strafeAcceleration
            else -> acceleration
        }
    }

    private val ShipAPI.movementPriority: Float
        get() {
            return when {
                this == Global.getCombatEngine().playerShip && isUnderManualControl -> 2f

                root.isStation -> 2f

                isHulk -> 2f

                engineController?.isFlamedOut == true -> 2f

                root.isFrigate -> -1f

                aiFlags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF) -> 1f

                else -> 0f
            }
        }
}
