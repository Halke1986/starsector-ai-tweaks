package com.genir.aitweaks.core.utils

import com.genir.aitweaks.core.utils.extensions.length
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*

const val DEGREES_TO_RADIANS: Float = 0.017453292F
const val RADIANS_TO_DEGREES: Float = 57.29578F

/** Solve quadratic equation [ax^2 + bx + c = 0] for x. */
fun quad(a: Float, b: Float, c: Float): Pair<Float, Float>? {
    val d = b * b - 4f * a * c
    return when {
        d < 0 -> null
        a == 0f -> (2 * c / -b).let { Pair(it, it) }
        else -> sqrt(d).let { Pair((-b + it) / (2 * a), (-b - it) / (2 * a)) }
    }
}

/** Time after which point P travelling with velocity V
 * will find itself at R distance from (0,0). */
fun solve(pv: Pair<Vector2f, Vector2f>, r: Float) = solve(pv, 0f, 0f, r, 0f)

/**
 * Solve the following cosine law equation for t:
 *
 * a(t)^2 = b(t)^2 + r^2 - 2*b(t)*r*cosA
 *
 * where
 *
 * a(t) = |P + V * t|
 * b(t) =  q + w * t
 *
 * The smaller positive solutions is returned.
 * If no positive solution exists, null is returned.
 *
 * Equation can be expanded in the following way:
 * (Px + Vx * t)^2            + (Py + Vy * t)^2            = (q + w * t)^2          + r^2 - 2((q + w * t) * r * cosA)
 * Px^2 + 2Px*Vx*t + Vx^2*t^2 + Py^2 + 2Py*Vy*t + Vy^2*t^2 = q^2 + 2q*w*t + w^2*t^2 + r^2 - 2q*r*cosA - 2w*t*r*cosA
 * 0 = (Vx^2 + Vy^2 - w^2)*t^2 + 2(Px*Vx + Py*Vy - w*q + r*w*cosA)*t + (Px^2 + Py^2 - q^2 - r^2 + 2q*r*cosA)
 */
fun solve(pv: Pair<Vector2f, Vector2f>, q: Float, w: Float, r: Float, cosA: Float): Float? {
    val (p, v) = pv
    val a = (v.x * v.x) + (v.y * v.y) - (w * w)
    val b = (p.x * v.x) + (p.y * v.y) - (w * q) + (w * r * cosA)
    val c = (p.x * p.x) + (p.y * p.y) - (r * r) - (q * q) + (2 * q * r * cosA)

    val (t1, t2) = quad(a, 2f * b, c) ?: return null
    return when {
        t1 >= 0 && t2 >= 0 -> min(t1, t2)
        t1 <= 0 != t2 <= 0 -> max(t1, t2)
        else -> null
    }
}

/** Time after which point p travelling with velocity v will be the closes to (0,0). */
fun timeToOrigin(p: Vector2f, v: Vector2f): Float {
    return -(p.x * v.x + p.y * v.y) / (v.x * v.x + v.y * v.y)
}

/**
 *  Find the distance between point (0,0)
 *  and point p traveling with velocity v.
 *
 *  Returns null if v points away from (0,0).
 */
fun distanceToOrigin(p: Vector2f, v: Vector2f): Float? {
    val t = timeToOrigin(p, v)
    return if (t >= 0) (p + v * t).length
    else null
}

/** Vector projection of 'a' onto 'b' */
fun vectorProjection(a: Vector2f, b: Vector2f): Vector2f {
    return b * (dotProduct(a, b) / dotProduct(b, b))
}

/** Length of vector projection of 'a' onto 'b'. Positive value
 * is returned if 'b' and 'a projection onto b' have same the
 * direction, negative otherwise. */
fun vectorProjectionLength(a: Vector2f, b: Vector2f): Float {
    val p = vectorProjection(a, b)
    return dotProduct(p, b).sign * p.length
}

fun dotProduct(a: Vector2f, b: Vector2f): Float {
    return a.x * b.x + a.y * b.y
}

/** Angular size of a circle, as observed from point (0,0). */
fun angularSize(distanceSqr: Float, radius: Float): Float {
    val radiusSqr = radius * radius
    if (radiusSqr >= distanceSqr) return 360f

    val adjacent = sqrt(distanceSqr - radius * radius)
    return atan(radius / adjacent) * RADIANS_TO_DEGREES * 2f
}

/** Maximum velocity in given direction to not overshoot target. */
fun vMax(dt: Float, dist: Float, deceleration: Float): Float {
    val (q, _) = quad(0.5f, 0.5f, -dist / (deceleration * dt * dt)) ?: return 0f
    return floor(q) * deceleration * dt
}

/** Distance covered by ship when decelerating from given velocity. */
fun decelerationDist(dt: Float, velocity: Float, deceleration: Float): Float {
    val v = velocity * dt
    val a = deceleration * dt * dt

    val t = ceil(v / a)
    return (v + a) * t * 0.5f
}

fun atan(z: Float): Float {
    return atan(z.toDouble()).toFloat()
}

fun atan2(y: Float, x: Float): Float {
    return atan2(y.toDouble(), x.toDouble()).toFloat()
}

fun atan(z: Double): Double {
    // extend atanApprox range from [-1,1]
    return if (z <= 1.0) atanApprox(z)
    else z.sign * PI / 2.0 - atanApprox(1.0 / z)
}

/** Based on org.lazywizard.lazylib.FastTrig.atan2 */
fun atan2(y: Double, x: Double): Double {
    val ay = abs(y)
    val ax = abs(x)
    val invert = ay > ax
    val z = if (invert) ax / ay else ay / ax // [0,1]
    var th = atanApprox(z) // [0,π/4]
    if (invert) th = Math.PI / 2.0 - th // [0,π/2]

    if (x < 0.0) th = Math.PI - th // [0,π]

    return th.withSign(y) // [-π,π]
}

/**
 * Polynomial atan approximation taken from
 * https://stackoverflow.com/questions/42537957/fast-accurate-atan-arctan-approximation-algorithm
 *
 * Accurate in range [-1,1]
 */
private fun atanApprox(x: Double): Double {
    val a = 0.0869142852883849
    val b = -2.9993501171084700E-01
    val c = 0.998418889819911

    val xx = x * x
    return ((a * xx + b) * xx + c) * x
}

class Rotation(angle: Float) {
    private val sin: Float
    private val cos: Float

    init {
        val radians = angle * DEGREES_TO_RADIANS
        sin = sin(radians)
        cos = cos(radians)
    }

    companion object {
        fun Vector2f.rotated(r: Rotation): Vector2f {
            return Vector2f(x * r.cos - y * r.sin, x * r.sin + y * r.cos)
        }

        fun Vector2f.rotatedAroundPivot(r: Rotation, p: Vector2f): Vector2f {
            return (this - p).rotated(r) + p
        }

        fun Vector2f.rotatedReverse(r: Rotation): Vector2f {
            return Vector2f(x * r.cos + y * r.sin, -x * r.sin + y * r.cos)
        }
    }
}

fun unitVector(angle: Float): Vector2f {
    val radians = angle * DEGREES_TO_RADIANS
    val x = cos(radians)
    val y = sin(radians)
    return Vector2f(x, y)
}

/** Returns the shortest rotation angle from `from` to `to`.
 * Exhibits lower numerical instability compared to
 * lazyLib MathUtils.getShortestRotation. */
fun shortestRotation(from: Float, to: Float): Float {
    val dist = (to - from) % 360f
    return if (abs(dist) <= 180f) dist
    else (-dist.sign) * 360f + dist
}
