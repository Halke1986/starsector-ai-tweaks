package com.genir.aitweaks.core.utils

import com.genir.aitweaks.core.extensions.*
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.math.withSign

const val DEGREES_TO_RADIANS: Float = 0.017453292F
const val RADIANS_TO_DEGREES: Float = 57.29578F
const val PI = Math.PI.toFloat()

fun dotProduct(a: Vector2f, b: Vector2f): Float {
    return a.x * b.x + a.y * b.y
}

/** Calculates the z-axis component of the cross product of two vectors. */
fun crossProductZ(a: Vector2f, b: Vector2f): Float {
    return a.x * b.y - a.y * b.x
}

/** Time after which point p travelling with velocity v will be the closes to (0,0). */
fun timeToOrigin(p: Vector2f, v: Vector2f): Float {
    return -dotProduct(p, v) / v.lengthSquared
}

/** Calculates the minimum distance between a point `p`, moving with velocity `v`, and the origin (0,0).
 *  If the velocity vector `v` points away from the origin, the current distance of point `p` is returned. */
fun distanceToOrigin(p: Vector2f, v: Vector2f): Float {
    // return abs(crossProductZ(p, v)) / v.length
    val t = timeToOrigin(p, v)
    return (p + v * max(0f, t)).length
}

/** Angular velocity of point 'p' moving with a linear
 * velocity 'v' in relation to the origin (0, 0). */
fun angularVelocity(p: Vector2f, v: Vector2f): Float {
    return crossProductZ(p, v) / p.lengthSquared * RADIANS_TO_DEGREES
}

/** Vector projection of 'a' onto 'b' */
fun vectorProjection(a: Vector2f, b: Vector2f): Vector2f {
    return b * (dotProduct(a, b) / dotProduct(b, b))
}

/** Vector rejection of 'a' from 'b' */
fun vectorRejection(a: Vector2f, b: Vector2f): Vector2f {
    return a - vectorProjection(a, b)
}

/** Length of vector projection of 'a' onto 'b'. Positive value
 * is returned if 'b' and 'a projection onto b' have same the
 * direction, negative otherwise. */
fun vectorProjectionLength(a: Vector2f, b: Vector2f): Float {
    val p = vectorProjection(a, b)
    return dotProduct(p, b).sign * p.length
}

/** Angular size of a circle, as observed from point (0,0). */
fun angularSize(distanceSqr: Float, radius: Float): Float {
    val radiusSqr = radius * radius
    if (radiusSqr >= distanceSqr) return 360f

    val adjacent = sqrt(distanceSqr - radiusSqr)
    return atan(radius / adjacent) * RADIANS_TO_DEGREES * 2
}

/** Calculates the points of tangency for a circle centered at point `p`
 * with radius `r`, where the tangents pass through the origin (0, 0). */
fun pointsOfTangency(p: Vector2f, r: Float): Pair<Vector2f, Vector2f>? {
    val d = p.length
    when {
        // Undefined for negative radius.
        r < 0f -> return null

        // Undefined for circles containing the origin.
        d <= r -> return null

        // For degenerate circles both points of tangency equal the circle location.
        r == 0f -> return Pair(p.copy, p.copy)
    }

    // Calculate one of the points of tangency in a rotated frame
    // of reference, where the point 'p' lies on the x-axis.
    val r2 = r * r
    val hx = d - r2 / d

    // Handle the case of numerical instability leading to negative radicand.
    val radicand = d * d - hx * hx - r2
    val hy = if (radicand > 0f) sqrt(radicand) else 0f

    val cos = p.x / d
    val sin = p.y / d

    // Rotate the point of tangency back into the original frame of reference.
    val h1 = Vector2f(hx * cos - hy * sin, hx * sin + hy * cos)
    val h2 = Vector2f(hx * cos + hy * sin, hx * sin - hy * cos)

    return Pair(h1, h2)
}

fun atan(z: Float): Float {
    // extend atanApprox range from [-1,1]
    return if (abs(z) <= 1) atanApprox(z)
    else z.sign * PI / 2 - atanApprox(1 / z)
}

/** Based on org.lazywizard.lazylib.FastTrig.atan2 */
fun atan2(y: Float, x: Float): Float {
    val ay = abs(y)
    val ax = abs(x)
    val invert = ay > ax
    val z = if (invert) ax / ay else ay / ax // [0,1]
    var th = atanApprox(z) // [0,π/4]
    if (invert) th = PI / 2 - th // [0,π/2]

    if (x < 0) th = PI - th // [0,π]

    return th.withSign(y) // [-π,π]
}

/**
 * Atan approximation accurate in range [-1,1].
 * Zarowski, C. Differential Evolution for a Better Approximation to the Arctangent Function,
 * Nanodottek Report NDT3-04-2006
 */
fun atanApprox(x: Float): Float {
    val a = 0.372003f
    val b = 0.703384f
    val c = 0.043562f

    val xx = x * x
    return (1 + a * xx) * x / (1 + xx * (b + c * xx))
}

/**
 * Smoothly caps a float value near a specified limit using a sigmoid-like function.
 *
 * This function ensures a gradual transition as the input value approaches the limit,
 * avoiding hard clamping while maintaining smooth behavior. The output grows linearly
 * for small values of `x` and asymptotically approaches `lim` for large values.
 */
fun smoothCap(x: Float, lim: Float): Float {
    val t = x / lim
    return x / sqrt(1 + t * t)
}
