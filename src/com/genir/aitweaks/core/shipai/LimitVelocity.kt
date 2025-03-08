package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedX
import org.lwjgl.util.vector.Vector2f
import java.awt.Color.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

data class PV(val p: Vector2f, val v: Vector2f) {
    fun rotated(r: RotationMatrix): PV {
        return PV(p.rotated(r), v.rotated(r))
    }
}

data class Bound(val limit: EngineController.Limit, val pv: PV, val length: Float)

fun limitVelocity2(dt: Float, ship: ShipAPI, toShipFacing: Direction, expectedVelocityRaw: Vector2f, limits: List<EngineController.Limit>): Vector2f {
    val vMax = expectedVelocityRaw.length / dt
    val bounds: List<Bound> = buildBounds(ship, limits, vMax)
    if (bounds.isEmpty()) {
        return expectedVelocityRaw
    }

    bounds.forEach { bound ->
        val p1 = ship.location + bound.pv.p
        val p2 = ship.location + bound.pv.p + bound.pv.v * bound.length

        Debug.drawLine(p1, p2, BLUE)
    }

    val overspeed = handleOngoingOverspeed(ship, bounds)
    if (overspeed != null) {
        return overspeed.rotated(toShipFacing.rotationMatrix) * dt
    }

    return expectedVelocityRaw

//
//    val expectedPV: PV = run {
//        val r = (expectedVelocityRaw.facing - toShipFacing).rotationMatrix
//        val p = Vector2f(0f, 0f)
//        val v = Vector2f(vMax, 0f)
//        PV(p, v).rotated(r)
//    }
//
//
////    walls.forEach {
////        Debug.drawLine(ship.location, ship.location + it.p, RED)
////        Debug.drawLine(ship.location + it.p + it.v * 100f, ship.location + it.p, GREEN)
////    }
////
////    Debug.drawLine(ship.location, ship.location + expectedPV.v, YELLOW)
//
//    var lowestLimit: Float = Float.MAX_VALUE
//    var lowestLimitDirection: Float = 0f
//
//    walls.forEachIndexed { idx, wall ->
////        val direction = dotProduct(expectedPV.v, wall.v) / vMax
//        val (distance, direction) = intersection(expectedPV, wall) ?: return@forEachIndexed
//
////        Debug.print["dir"] = "dir ${direction / vMax}"
//
////        if (distance < 0f) {
////            return@forEachIndexed // TODO handle negative limits
////        }
//
//        if (direction < 0f) {
//            return@forEachIndexed // TODO handle negative limits
//        }
//
//        if (distance < lowestLimit) {
//            lowestLimit = distance
//            lowestLimitDirection = direction / vMax
//        }
//    }
//
////    Debug.print["lim"] = "$lowestLimit ${abs(lowestLimitDirection - 1f)}"
//
//    // No relevant limit found.
//    if (lowestLimit >= 1) {
//        return expectedVelocityRaw
//    }
//
//
////    Debug.drawLine(ship.location, ship.location + limits[lowestIdx].direction.unitVector * limits[lowestIdx].speedLimit, WHITE)
//
////    Debug.print[ship] = "${ship.name} $lowestLimit $lowestLimitDirection"
//
//    if (abs(lowestLimitDirection - 1f) < 0.005f) {
////        if (overspeed) {
////            val vmax = ship.maxSpeed * dt
////            return Pair(ship.velocity.rotated(toShipFacing.rotationMatrix) * dt - expectedVelocityRaw.resized(vmax), overspeed)
////        }
//
//        return expectedVelocityRaw * lowestLimit
//    }
//
//    val points: MutableList<Vector2f> = mutableListOf()
//
//
//    val expectedFacing = expectedPV.v.facing
//
//    val best: Vector2f? = points.maxWithOrNull(compareBy {
//        val angle = (it.facing - expectedFacing).length
//        val angleWeight = (180f - angle) / 180f
//
//        if (angle > 89f) {
//            return@compareBy 0f
//        }
//
//        it.lengthSquared * angleWeight * angleWeight
//    })
//
//    if (best == null) {
//        return Vector2f()// TODO what to return here?
//    }
//
////    Debug.drawCircle(ship.location, vMax, YELLOW)
//
////    Debug.drawLine(ship.location, ship.location + best, PINK)
////    Debug.drawLine(ship.location, ship.location + expectedPV.v, MAGENTA)
//
////    return best.rotated(toShipFacing.rotationMatrix) * dt//  expectedVelocityRaw * lowestLimit
//
//
//    return best.rotated(toShipFacing.rotationMatrix) * dt
}

fun handleOngoingOverspeed(ship: ShipAPI, bounds: List<Bound>): Vector2f? {
    val velocityPV = PV(Vector2f(), ship.velocity)

    var closestPoint: Vector2f? = null
    var closestDist = Float.MAX_VALUE

    bounds.forEach { bound ->
        val (distance, direction) = intersection(velocityPV, bound.pv) ?: run {
            // Special case for degenerate bound.


            log("${ship.name} no intersection $velocityPV ${bound.pv}")
            return@forEach
        }

        log("${ship.name} $distance $direction")

        when {
            // TODO handle negative limits
            direction < 0 -> return@forEach

            // Velocity does not exceed the movement bound.
            distance >= 1 -> return@forEach
        }

        // Find the closest point on the boundary relative to the velocity vector.
        val p = bound.pv.p - ship.velocity
        val v = bound.pv.v
        val t = timeToOrigin(p, v)

        val closest = bound.pv.p + v * t.coerceIn(0f, bound.length)
        val dist = closest.lengthSquared

        Debug.drawLine(ship.location, ship.location + closest, YELLOW)
        Debug.drawLine(ship.location + ship.velocity, ship.location + closest, GREEN)

//        Debug.print["dist"] = "dist ${sqrt(dist)}"

        if (dist < closestDist) {
            closestPoint = closest
            closestDist = dist
        }
    }

    // No ongoing overspeed found.
    if (closestPoint == null) {
        return null
    }

    Debug.drawCollisionRadius(ship, RED)

    val dv = if (closestDist <= 0f) {
        ship.velocity
    } else {
        ship.velocity - closestPoint!!
    }

    return ship.velocity - dv.resized(ship.maxSpeed)
}

fun buildBounds(ship: ShipAPI, limits: List<EngineController.Limit>, radius: Float): List<Bound> {
    val rawBounds: List<Bound> = limits.map { limit ->
        val r = limit.direction.rotationMatrix
        val p = Vector2f(limit.speedLimit.coerceAtLeast(0f), 0f) // TODO handle negative limits
        val v = Vector2f(0f, 1f)
        Bound(limit, PV(p, v).rotated(r), 0f)
    }

    log("${ship.name} BUILD ${limits.size}")

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
            log("${ship.name} drop 1")
            return@mapNotNull null
        }

        // Coerce the bound to the enclosing radius.
        val pv = bound.pv
        val withinVMax = solve(pv.p, pv.v, radius)
            ?: run {
                log("${ship.name} drop 2")
                return@mapNotNull null // TODO handle negative limits beyond vMax
            }

        // Bound is entirely outside the enclosing radius.
        if (max < withinVMax.x1 || min > withinVMax.x2) {
            log("${ship.name} drop 3")
            return@mapNotNull null
        }

        min = min.coerceIn(withinVMax.x1, withinVMax.x2)
        max = max.coerceIn(withinVMax.x1, withinVMax.x2)

        val pMin = pv.p + pv.v * min
//        val pMax = pv.p + pv.v * max

        // Set new starting point ang length.
        Bound(bound.limit, PV(pMin, bound.pv.v), max - min)
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