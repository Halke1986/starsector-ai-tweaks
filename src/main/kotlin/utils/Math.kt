package com.genir.aitweaks.utils

import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 *  Find the square of distance between point (0,0)
 *  and point p traveling with velocity v.
 *
 *  Returns null if v points away from (0,0).
 */
fun distanceToOriginSqr(p: Vector2f, v: Vector2f): Float? {
    val t = -(p.x * v.x + p.y * v.y) / (v.x * v.x + v.y * v.y)
    return if (t >= 0) (p + v.times(t)).lengthSquared()
    else null
}

/**
 * Solve the following equation for t:
 *
 * |p + t*v| = r + t*w
 *
 * where p and v are vectors and r and w are scalar values.
 *
 * The smaller of the two positive solutions is returned.
 * If no positive solution exists, null is returned.
 *
 * One of the possible interpretations of the solution is:
 * "time after which point p moving with velocity v
 * will intersect circle centered at point (0,0), with initial
 * radius r and expanding with speed w".
 */
fun solve(p: Vector2f, v: Vector2f, r: Float, w: Float): Float? {
    // Equation can be expanded the following way:
    // |p + v * t| = r + w * t
    // sqrt[ (p.x + v.x * t)^2 + (p.y + v.y * t)^2 ] = r + w * t
    // (p.x + v.x * t)^2 + (p.y + v.y * t)^2 = (r + w * t)^2
    // 0 = (v.x^2 + v.y^2 - w^2)*t^2 + 2(p.x*v.x + p.y*v.y - r*w)*t + (p.x^2 + p.y^2 - r^2)
    val a = v.lengthSquared() - w * w
    val b = 2f * (p.x * v.x + p.y * v.y - r * w)
    val c = p.lengthSquared() - r * r

    val (t1, t2) = quad(a, b, c) ?: return null

    if (t1 < 0 && t2 < 0) return null
    if (t1 < 0 || t2 < 0) return max(t1, t2)
    return min(t1, t2)
}

/**
 * solve quadratic equation [ax^2 + bx + c = 0] for x.
 */
fun quad(a: Float, b: Float, c: Float): Pair<Float, Float>? {
    val d = b * b - 4f * a * c
    return when {
        MathUtils.equals(d, 0f) || d < 0 -> null
        MathUtils.equals(a, 0f) -> (2 * c / -b).let { Pair(it, it) }
        else -> sqrt(d).let { Pair((-b + it) / (2 * a), (-b - it) / (2 * a)) }
    }
}