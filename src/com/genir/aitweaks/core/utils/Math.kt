package com.genir.aitweaks.core.utils

import org.lazywizard.lazylib.FastTrig
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sign
import kotlin.math.sqrt

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
    return if (t >= 0) (p + v * t).length()
    else null
}

/** Vector projection of 'a' onto 'b' */
fun vectorProjection(a: Vector2f, b: Vector2f): Vector2f {
    return b * (dotProduct(a, b) / dotProduct(b, b))
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

/** Polynomial approximation of arctangenet, extended to
 * full float range from the FastTrig.atan [-1,1] range. */
fun atan(z: Double): Double {
    return if (z <= 1f) FastTrig.atan(z)
    else z.sign * PI / 2f - FastTrig.atan(1f / z)
}

fun atan(z: Float): Float = atan(z.toDouble()).toFloat()

/** Maximum velocity in given direction to not overshoot target. */
fun vMax(dt: Float, dist: Float, deceleration: Float): Float {
    val (q, _) = quad(0.5f, 0.5f, -dist / (deceleration * dt * dt)) ?: return 0f
    return floor(q) * deceleration * dt
}
