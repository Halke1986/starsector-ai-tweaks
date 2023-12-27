package com.genir.aitweaks.utils

import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.Float.Companion.NaN
import kotlin.math.sqrt

/**
 *  Find the square of distance between point (0,0)
 *  and point p traveling with velocity v.
 *
 *  Returns Nan if v points away from (0,0).
 */
fun distanceToOriginSqr(p: Vector2f, v: Vector2f): Float {
    val t = -(p.x * v.x + p.y * v.y) / (v.x * v.x + v.y * v.y)
    return if (t >= 0) (p + v.times(t)).lengthSquared()
    else NaN
}

/**
 * Solve the following cosine law equation for t:
 *
 * a(t)^2 = b(t)^2 + r^2 - 2*b(t)*r*cosA
 *
 * where
 *
 * a(t) = |P + V * t|
 * b(t) = w * t
 *
 * The smaller positive solutions is returned.
 * If no positive solution exists, NaN is returned.
 *
 * Equation can be expanded the following way:
 * (|P + V * t|)^2 = (w * t)^2 + r^2 - 2(w * t * r * cosA)
 * (Px + Vx * t)^2 + (Py + Vy * t)^2 = = w^2 * t^2 + r^2 - 2(w * t * r * cosA)
 * (Vx^2 + Vy^2 - w^2)*t^2 + 2(Px*Vx + Py*Vy + r*w*cosA)*t + (Px^2 + Py^2 - r^2) = 0
 */
fun solve(p: Vector2f, v: Vector2f, r: Float, w: Float, cosA: Float): Float {
    val a = v.lengthSquared() - w * w
    val b = 2f * (p.x * v.x + p.y * v.y + r * w * cosA)
    val c = p.lengthSquared() - r * r

    return smallerPositive(quad(a, b, c))
}

/**
 * solve quadratic equation [ax^2 + bx + c = 0] for x.
 */
fun quad(a: Float, b: Float, c: Float): Pair<Float, Float> {
    val d = b * b - 4f * a * c
    return when {
        d < 0 -> Pair(NaN, NaN)
        a == 0f -> (2 * c / -b).let { Pair(it, it) }
        else -> sqrt(d).let { Pair((-b + it) / (2 * a), (-b - it) / (2 * a)) }
    }
}

fun smallerPositive(p: Pair<Float, Float>): Float = when {
    p.first > 0 && p.first < p.second -> p.first
    p.second > 0 -> p.second
    else -> NaN
}