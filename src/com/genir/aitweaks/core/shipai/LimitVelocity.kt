package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedX
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedY
import com.genir.aitweaks.core.utils.crossProductZ
import com.genir.aitweaks.core.utils.solve
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

data class PV(val p: Vector2f, val v: Vector2f) {
    fun rotated(r: RotationMatrix): PV {
        return PV(p.rotated(r), v.rotated(r))
    }

    fun rotatedReverse(r: RotationMatrix): PV {
        return PV(p.rotatedReverse(r), v.rotatedReverse(r))
    }
}

data class Bound(val r: RotationMatrix, val speedLimit: Float, val pMin: Float, val pMax: Float)

fun limitVelocity2(dt: Float, ship: ShipAPI, toShipFacing: Direction, expectedVelocityRaw: Vector2f, limits: List<EngineController.Limit>): Vector2f {
    val rotationToShip = toShipFacing.rotationMatrix
    val expectedVelocity = expectedVelocityRaw.rotatedReverse(rotationToShip) / dt

    val bounds: List<Bound> = buildBounds(limits, expectedVelocity)
    if (bounds.isEmpty()) {
        return expectedVelocityRaw
    }

//    bounds.forEach { bound ->
//        val p1 = ship.location + Vector2f(bound.speedLimit, bound.pMin).rotatedReverse(bound.r)
//        val p2 = ship.location + Vector2f(bound.speedLimit, bound.pMax).rotatedReverse(bound.r)
//
//        Debug.drawLine(p1, p2, BLUE)
//    }

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
    data class RawBound(val limit: EngineController.Limit, val r: RotationMatrix, val pv: PV)

    val radius = expectedVelocity.length
    val rawBounds: List<RawBound> = limits.map { limit ->
        val r = (-limit.direction).rotationMatrix
        val p = Vector2f(limit.speedLimit.coerceAtLeast(0f), 0f) // TODO handle negative limits
        val v = Vector2f(0f, 1f)
        RawBound(limit, r, PV(p, v).rotatedReverse(r))
    }

    val bounds = rawBounds.mapNotNull { bound ->
        var min = -Float.MAX_VALUE
        var max = Float.MAX_VALUE

        // Intersect bounds.
        rawBounds.forEach inner@{ other ->
            val (distance, direction) = intersection(bound.pv, other.pv) ?: return@inner

            if (direction < 0f) {
                min = max(min, distance)
            } else {
                max = min(max, distance)
            }
        }

        // Bound is entirely behind other bounds.
        if (min > max) {
            return@mapNotNull null
        }

        // Coerce the bound to the enclosing radius.
        val pv = bound.pv
        val withinVMax = solve(pv.p, pv.v, radius) ?: return@mapNotNull null // TODO handle negative limits beyond vMax

        // Bound is entirely outside the enclosing radius.
        if (max < withinVMax.x1 || min > withinVMax.x2) {
            return@mapNotNull null
        }

        // Start and end points of the bound line.
        val pMin = pv.p + pv.v * min.coerceIn(withinVMax.x1, withinVMax.x2)
        val pMax = pv.p + pv.v * max.coerceIn(withinVMax.x1, withinVMax.x2)

        Bound(bound.r, bound.limit.speedLimit, pMin.rotatedY(bound.r), pMax.rotatedY(bound.r))
    }

    return bounds
}

fun intersection(v: PV, w: PV): Pair<Float, Float>? {
    val direction = crossProductZ(v.v, w.v)
    if (direction == 0f) {
        // Vectors are perpendicular.
        return null
    }

    val offset = crossProductZ(w.v, v.p - w.p)
    return Pair(offset / direction, direction)
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
