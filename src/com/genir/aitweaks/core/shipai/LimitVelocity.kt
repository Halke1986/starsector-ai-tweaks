package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedReverse
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.sqrt

data class LimitData(val r: RotationMatrix, val h: Float, val p: Vector2f, val v: Vector2f)

fun limitVelocity2(dt: Float, ship: ShipAPI, toShipFacing: Direction, expectedVelocity: Vector2f, absLimits: List<EngineController.Limit>): Vector2f {
    // No relevant speed limits found, move ahead.
    if (absLimits.isEmpty()) {
        return expectedVelocity
    }

//    Debug.clear()

    val expectedVelocityFacing = expectedVelocity.facing - toShipFacing // global frame of reference
    val floor = listOf(EngineController.Limit(90f.direction, 0f))
    val limits = floor + absLimits.map { EngineController.Limit(it.direction - expectedVelocityFacing + 90f, it.speedLimit * dt) }

    Debug.drawArc(ship.location, expectedVelocity.length / dt, Arc(180f, 90f), Color.YELLOW)

    val l2 = expectedVelocity.lengthSquared

    val ds: Array<LimitData> = Array(limits.size) { i ->
        val r = limits[i].direction.rotationMatrix

        val limit = limits[i]
        val h = limit.speedLimit

        val w = sqrt(l2 - (h * h))

        val p1 = Vector2f(h, +w).rotated(r)
        val p2 = Vector2f(h, -w).rotated(r)

        LimitData(r, h, p1, p2 - p1)
    }

    limits.forEach { limit ->
        Debug.drawLine(ship.location, ship.location + limit.direction.unitVector * limit.speedLimit / dt, Color.RED)
    }

    val points: MutableList<Vector2f> = mutableListOf()

    val default = Vector2f(0f, expectedVelocity.length)
    if (filterPoint(default, l2, ds, -1, -1)) {
        points.add(default)
    }

    ds.forEachIndexed { idx, data ->
        Debug.drawLine(ship.location + data.p / dt, ship.location + (data.p + data.v) / dt, Color.CYAN)

        for (i in idx + 1 until ds.size) {
            val b = ds[i]

            val intersection = intersection(data.p, data.v, b.p, b.v)
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

    val best = points.maxWithOrNull(compareBy<Vector2f> { (it.lengthSquared.coerceAtMost(l2) * 100 / l2).toInt().coerceAtMost(99) }.thenBy { -(it.facing - 90f).length })
    if (best == null) {
        return Vector2f()
    }

    points.forEach { p ->
        Debug.drawLine(ship.location, ship.location + p / dt, Color.BLUE)

//        val l = (p.lengthSquared.coerceAtMost(l2) * 100 / l2).toInt().coerceAtMost(99)
//        Debug.print[p] = l
    }

    Debug.drawLine(ship.location, ship.location + (best) / dt, Color.GREEN)

//    Debug.print[ship] = "${ship.name} ${best.facing + expectedVelocity.facing - 90f} ${expectedVelocity.facing}"

    return (best.facing + expectedVelocity.facing - 90f).unitVector * best.length
}

fun filterPoint(p: Vector2f, l2: Float, ds: Array<LimitData>, skip1: Int, skip2: Int): Boolean {
    if (p.y < -0.01f) {
        return false
    }

    if (p.lengthSquared > l2 + 0.01f) {
        return false
    }

    for (i in 1 until ds.size) {
        if (i == skip1 || i == skip2) {
            continue
        }


        val d = ds[i]

        log("$p ${p.rotatedReverse(d.r).x} ${d.h}")

        if (p.rotatedReverse(d.r).x > d.h) {
            return false
        }
    }

    return true
}

fun intersection(p: Vector2f, v: Vector2f, q: Vector2f, w: Vector2f): Vector2f {
    val a = crossProductZ(w, p - q)
    val b = crossProductZ(w, v)

    return p - v * (a / b)
}