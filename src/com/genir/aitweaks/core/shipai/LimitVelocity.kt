package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedX
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedY
import com.genir.aitweaks.core.utils.sqrt
import org.lwjgl.util.vector.Vector2f
import java.awt.Color.BLUE
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

data class Bound(val r: RotationMatrix, val speedLimit: Float, val pMin: Float, val pMax: Float)

fun limitVelocity2(dt: Float, ship: ShipAPI, toShipFacing: Direction, expectedVelocityRaw: Vector2f, limits: List<EngineController.Limit>): Vector2f {
    val rotationToShip = toShipFacing.rotationMatrix
    val expectedVelocity = expectedVelocityRaw.rotatedReverse(rotationToShip) / dt

    val bounds: List<Bound> = buildBounds(limits, expectedVelocity)
    if (bounds.isEmpty()) {
        return expectedVelocityRaw
    }

    bounds.forEach { bound ->
        val p1 = ship.location + Vector2f(bound.speedLimit, bound.pMin).rotatedReverse(bound.r)
        val p2 = ship.location + Vector2f(bound.speedLimit, bound.pMax).rotatedReverse(bound.r)

        Debug.drawLine(p1, p2, BLUE)
    }

    val overspeed = handleOngoingOverspeed(ship, bounds)
    if (overspeed != null) {
        return overspeed.rotated(toShipFacing.rotationMatrix) * dt
    }

    val avoid = handlePotentialOverspeed(bounds, expectedVelocity)
    if (avoid != null) {
        return avoid.rotated(toShipFacing.rotationMatrix) * dt
    }

    return expectedVelocityRaw
}

fun handleOngoingOverspeed(ship: ShipAPI, bounds: List<Bound>): Vector2f? {
    var closestPoint: Vector2f? = null
    var closestDist = Float.MAX_VALUE

    bounds.forEach { bound ->
        // Velocity does not exceed the bound.
        val vx = ship.velocity.rotatedX(bound.r)
        if (vx <= bound.speedLimit) {
            return@forEach
        }

        // Find the closest point on the boundary relative to the velocity vector.
        val vy = ship.velocity.rotatedY(bound.r)
        val closestLocal = Vector2f(bound.speedLimit, vy.coerceIn(bound.pMin, bound.pMax))
        val closest = closestLocal.rotatedReverse(bound.r)
        val dist = closest.lengthSquared

        if (dist < closestDist) {
            closestPoint = closest
            closestDist = dist
        }
    }

    // No ongoing overspeed found.
    if (closestPoint == null) {
        return null
    }

    val dv = if (closestDist <= 0f) {
        ship.velocity
    } else {
        ship.velocity - closestPoint!!
    }

    return ship.velocity - dv.resized(ship.maxSpeed)
}

fun handlePotentialOverspeed(bounds: List<Bound>, expectedVelocity: Vector2f): Vector2f? {
    val points: MutableList<Vector2f> = mutableListOf()
    bounds.forEach { bound ->
        // Velocity does not exceed the bound.
        val vx = expectedVelocity.rotatedX(bound.r)
        if (vx <= bound.speedLimit) {
            return@forEach
        }

        points.add(Vector2f(bound.speedLimit, bound.pMin).rotatedReverse(bound.r))
        points.add(Vector2f(bound.speedLimit, bound.pMax).rotatedReverse(bound.r))
    }

    // No relevant speed limits.
    if (points.isEmpty()) {
        return null
    }

    val expectedFacing = expectedVelocity.facing
    val best: Vector2f? = points.maxWithOrNull(compareBy {
        val angle = (it.facing - expectedFacing).length
        val angleWeight = (180f - angle) / 180f

        if (angle > 89f) {
            return@compareBy 0f
        }

        it.lengthSquared * angleWeight * angleWeight
    })

    return best ?: Vector2f()
}

fun buildBounds(limits: List<EngineController.Limit>, expectedVelocity: Vector2f): List<Bound> {
    val radius2 = expectedVelocity.lengthSquared
    val rawBounds: List<Bound> = limits.mapNotNull { limit ->
        val r = (-limit.direction).rotationMatrix
        val vLim2 = limit.speedLimit * limit.speedLimit
        val pMin: Float
        val pMax: Float

        when {
            // Limit is higher than ship max speed.
            limit.speedLimit > 0 && vLim2 > radius2 -> {
                return@mapNotNull null
            }

            // Negative limit higher than ship max speed.
            // It effectively has no common point with ship
            // max speed radius. Setting both points to zero
            // results in the shortest possible velocity vector.
            vLim2 > radius2 -> {
                pMin = 0f
                pMax = 0f
            }

            else -> {
                val h = sqrt(radius2 - vLim2)

                pMin = -h
                pMax = +h
            }
        }

        Bound(r, limit.speedLimit, pMin, pMax)
    }

    // Find enclosing velocity bounds.
    val bounds = rawBounds.mapNotNull { bound ->
        var pMin = bound.pMin
        var pMax = bound.pMax

        val p = Vector2f(bound.speedLimit, 0f).rotatedReverse(bound.r)
        val v = Vector2f(0f, 1f).rotatedReverse(bound.r)

        // Intersect bounds.
        rawBounds.forEach inner@{ other ->
            val vx = v.rotatedX(other.r)

            // Limits are perpendicular.
            if (vx == 0f) {
                return@inner
            }

            val px = p.rotatedX(other.r)
            val distance = (other.speedLimit - px) / vx

            if (vx > 0f) {
                pMax = min(pMax, distance)
            } else {
                pMin = max(pMin, distance)
            }
        }

        // Bound is entirely behind other bounds.
        if (pMin > pMax) {
            return@mapNotNull null
        }

        Bound(bound.r, bound.speedLimit, pMin, pMax)
    }

    return bounds
}

fun vMaxToObstacle2(dt: Float, ship: ShipAPI, obstacle: ShipAPI, minDistance: Float): EngineController.Limit? {
    val toObstacle = obstacle.location - ship.location
    val toObstacleFacing = toObstacle.facing
    val r = (-toObstacleFacing).rotationMatrix

    val distance = toObstacle.rotatedX(r)
    val distanceLeft = distance - minDistance

    val decelShip = ship.collisionDeceleration(toObstacleFacing)
    val vMax = BasicEngineController.vMax(dt, abs(distanceLeft), decelShip) * distanceLeft.sign

    val vShip = 0f//ship.velocity.rotatedX(r)
    val vObstacle = obstacle.velocity.rotatedX(r)
    val approachSpeed = vShip - vObstacle

//    Debug.print["vShip"] = "vShip $vShip"
//    Debug.print["vObstacle"] = "vObstacle $vObstacle"
//    Debug.print["approachSpeed"] = "approachSpeed $approachSpeed"
//    Debug.print["vMax"] = "vMax $vMax"

//    val lim = vMax - approachSpeed
//    Debug.drawLine(ship.location, ship.location + toObstacle.resized(lim), if (lim < 0f) BLUE else RED)

    return EngineController.Limit(toObstacleFacing, vMax - approachSpeed)
}

private fun ShipAPI.collisionDeceleration(collisionFacing: Direction): Float {
    val angleFromBow = (collisionFacing - facing.direction).length
    return when {
        angleFromBow < 30f -> deceleration
        angleFromBow < 150f -> strafeAcceleration
        else -> acceleration
    }
}
