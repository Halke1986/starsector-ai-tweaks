package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedX
import com.genir.aitweaks.core.utils.crossProductZ
import com.genir.aitweaks.core.utils.solve
import org.lwjgl.util.vector.Vector2f
import java.awt.Color.RED
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

data class PV(val p: Vector2f, val v: Vector2f) {
    fun rotated(r: RotationMatrix): PV {
        return PV(p.rotated(r), v.rotated(r))
    }
}

fun limitVelocity2(dt: Float, ship: ShipAPI, toShipFacing: Direction, expectedVelocityRaw: Vector2f, limits: List<EngineController.Limit>): Vector2f {
    if (limits.isEmpty()) {
        return expectedVelocityRaw
    }

    val vMax = expectedVelocityRaw.length / dt

    val expectedPV: PV = run {
        val r = (expectedVelocityRaw.facing - toShipFacing).rotationMatrix
        val p = Vector2f(0f, 0f)
        val v = Vector2f(vMax, 0f)
        PV(p, v).rotated(r)
    }

    val walls: List<PV> = limits.map { limit ->
        val r = limit.direction.rotationMatrix
        val p = Vector2f(limit.speedLimit.coerceAtLeast(0f), 0f) // TODO handle negative limits
        val v = Vector2f(0f, 1f)
        PV(p, v).rotated(r)
    }

//    walls.forEach {
//        Debug.drawLine(ship.location, ship.location + it.p, RED)
//        Debug.drawLine(ship.location + it.p + it.v * 100f, ship.location + it.p, GREEN)
//    }
//
//    Debug.drawLine(ship.location, ship.location + expectedPV.v, YELLOW)

    var lowestLimit: Float = Float.MAX_VALUE
    var lowestLimitDirection: Float = 0f
    var lowestWall: PV = PV(Vector2f(), Vector2f())
    var lowestIdx = 0

    walls.forEachIndexed { idx, wall ->
//        val direction = dotProduct(expectedPV.v, wall.v) / vMax
        val (distance, direction) = intersection(expectedPV, wall) ?: return@forEachIndexed

//        Debug.print["dir"] = "dir ${direction / vMax}"

//        if (distance < 0f) {
//            return@forEachIndexed // TODO handle negative limits
//        }

        if (direction < 0f) {
            return@forEachIndexed // TODO handle negative limits
        }

        if (distance < lowestLimit) {
            lowestLimit = distance
            lowestLimitDirection = direction / vMax
            lowestWall = wall
            lowestIdx = idx
        }
    }

//    Debug.print["lim"] = "$lowestLimit ${abs(lowestLimitDirection - 1f)}"

    // No relevant limit found.
    if (lowestLimit >= 1) {
        return expectedVelocityRaw
    }


    val actualPV = PV(Vector2f(), ship.velocity)
    val overspeed = run {
        val (distance, direction) = intersection(actualPV, lowestWall) ?: return@run false

        if (distance < 0f) {
            return@run false // TODO handle negative limits
        }

        if (direction < 0f) {
            return@run false // TODO handle negative limits
        }

//        Debug.print["over"] = distance

        distance< 1
    }

    if (overspeed) {
        Debug.drawCollisionRadius(ship, RED)

        val v = (ship.velocity - limits[lowestIdx].direction.unitVector.resized(ship.maxSpeed))
//        return Pair(v.rotated(toShipFacing.rotationMatrix) * dt , overspeed)
        return v.rotated(toShipFacing.rotationMatrix) * dt
    }

//    Debug.drawLine(ship.location, ship.location + limits[lowestIdx].direction.unitVector * limits[lowestIdx].speedLimit, WHITE)

    Debug.print[ship] = "${ship.name} $lowestLimit $lowestLimitDirection"

    if (abs(lowestLimitDirection - 1f) < 0.005f) {
//        if (overspeed) {
//            val vmax = ship.maxSpeed * dt
//            return Pair(ship.velocity.rotated(toShipFacing.rotationMatrix) * dt - expectedVelocityRaw.resized(vmax), overspeed)
//        }

        return expectedVelocityRaw * lowestLimit
    }

//    if (overspeed < 1f) {
//        Debug.print["vover"] = overspeed
//
//        return Pair((ship.velocity * overspeed).rotated(toShipFacing.rotationMatrix) * dt, true)
//    }

    val points: MutableList<Vector2f> = mutableListOf()

    walls.forEach { limit ->
        var min = -Float.MAX_VALUE
        var max = Float.MAX_VALUE

        walls.forEach inner@{ other ->
            val (distance, direction) = intersection(limit, other) ?: return@inner

            if (direction < 0f) {
                min = max(min, distance)
            } else {
                max = min(max, distance)
            }
        }

        // No possibility of movement along the limit.
        if (min > max) {
            return@forEach
        }

        // Coerce movement along the limit to vMax.
        val withinVMax = solve(limit.p, limit.v, vMax) ?: return@forEach // TODO handle negative limits beyond vMax

        min = min.coerceIn(withinVMax.x1, withinVMax.x2)
        max = max.coerceIn(withinVMax.x1, withinVMax.x2)

        val pMin = limit.p + limit.v * min.coerceAtLeast(-500f)
        val pMax = limit.p + limit.v * max.coerceAtMost(+500f)

//        Debug.drawLine(ship.location + pMax, ship.location + pMin, BLUE)

        points.add(pMin)
        points.add(pMax)
    }

    val expectedFacing = expectedPV.v.facing

//    points.forEachIndexed { idx, p ->
//        val angle = (p.facing - expectedFacing).length
//        val angleWeight = (180f - angle) / 180f
//
//        val score = p.lengthSquared * angleWeight * angleWeight
//
////        Debug.print[idx] = "$angle $angleWeight ${p.length} $score"
//    }

    val best: Vector2f? = points.maxWithOrNull(compareBy {
        val angle = (it.facing - expectedFacing).length
        val angleWeight = (180f - angle) / 180f

        if (angle > 89f) {
            return@compareBy 0f
        }

        it.lengthSquared * angleWeight * angleWeight
    })

    if (best == null) {
        return Vector2f()// TODO what to return here?
    }

//    Debug.drawCircle(ship.location, vMax, YELLOW)

//    Debug.drawLine(ship.location, ship.location + best, PINK)
//    Debug.drawLine(ship.location, ship.location + expectedPV.v, MAGENTA)

//    return best.rotated(toShipFacing.rotationMatrix) * dt//  expectedVelocityRaw * lowestLimit


    return best.rotated(toShipFacing.rotationMatrix) * dt
}

fun intersection(v: PV, w: PV): Pair<Float, Float>? {
    val direction = crossProductZ(v.v, w.v)
    if (direction == 0f) {
        // Limits are perpendicular.
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