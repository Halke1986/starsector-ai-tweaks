package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.crossProductZ
import org.lwjgl.util.vector.Vector2f
import kotlin.math.sqrt

data class LimitData(val l: EngineController.Limit, val r: RotationMatrix, val h: Float, val p: Vector2f, val v: Vector2f)

fun limitVelocity2(dt: Float, ship: ShipAPI, toShipFacing: Direction, expectedVelocity: Vector2f, limits: List<EngineController.Limit>): Vector2f {
    val l = expectedVelocity.length
    val relevantLimits = limits.filter { it.speedLimit < l }


    // No relevant speed limits found, move ahead.
    if (relevantLimits.isEmpty()) {
        return expectedVelocity
    }

    val toVelocityFrameOfReference = toShipFacing + 90f - expectedVelocity.facing
    val floor = listOf(EngineController.Limit((-90f).direction, 0f))
    val localLimits = floor + relevantLimits.map { EngineController.Limit(it.direction + toVelocityFrameOfReference, it.speedLimit * dt) }

    val l2 = expectedVelocity.lengthSquared

    val ds: Array<LimitData> = Array(localLimits.size) { i ->
        val r = localLimits[i].direction.rotationMatrix

        val limit = localLimits[i]
        val h = limit.speedLimit

        val w = sqrt(l2 - (h * h))

        val p1 = Vector2f(h, +w).rotated(r)
        val p2 = Vector2f(h, -w).rotated(r)

        LimitData(limit, r, h, p1, p2 - p1)
    }

    val points: MutableList<Vector2f> = mutableListOf()

    val default = Vector2f(0f, expectedVelocity.length)
    if (filterPoint(default, l2, ds, -1, -1)) {
        points.add(default)
    }

    ds.forEachIndexed { idx, data ->
        for (i in idx + 1 until ds.size) {
            val b = ds[i]

            val intersection = intersection(data.p, data.v, b.p, b.v) ?: continue
            if (filterPoint(intersection, l2, ds, idx, i)) {
                points.add(intersection)
            }
        }

        if (filterPoint(data.p, l2, ds, idx, -1)) {
            points.add(data.p)
        }

        val p2 = data.p + data.v
        if (filterPoint(p2, l2, ds, idx, -1)) {
            points.add(p2)
        }
    }

    val best: Vector2f? = points.maxWithOrNull(compareBy {
        val angle = (it.facing - 90f).length
        val angleWeight = (180f - angle) / 180f

        it.lengthSquared.coerceAtMost(l2) * angleWeight * angleWeight
    })

    if (best == null) {
        return Vector2f()
    }

    if (best.length.isNaN()) {
        Debug.print["nan"] = "NaN"
    }

    return (best.facing + expectedVelocity.facing - 90f).unitVector * best.length
}

fun filterPoint(p: Vector2f, l2: Float, ds: Array<LimitData>, skip1: Int, skip2: Int): Boolean {
    if (p.lengthSquared > l2 + 0.01f) {
        return false
    }

    for (i in ds.indices) {
        if (i == skip1 || i == skip2) {
            continue
        }

        val d = ds[i]
        if (p.rotatedReverse(d.r).x > d.h) {
            return false
        }
    }

    return true
}

fun intersection(v: Vector2f, dv: Vector2f, w: Vector2f, dw: Vector2f): Vector2f? {
    val a = crossProductZ(dw, dv)
    if (a == 0f) {
        return null
    }

    val b = crossProductZ(dw, v - w)
    return v - dv * (b / a)
}
