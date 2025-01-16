package com.genir.aitweaks.core.utils

import com.genir.aitweaks.core.extensions.*
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*

const val DEGREES_TO_RADIANS: Float = 0.017453292F
const val RADIANS_TO_DEGREES: Float = 57.29578F
const val PI = Math.PI.toFloat()

/** Solve quadratic equation [ax² + bx + c = 0] for x. */
fun quad(a: Float, b: Float, c: Float): Pair<Float, Float>? {
    val d = b * b - 4 * a * c
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
 * a(t)² = b(t)² + r² - 2*b(t)*r*cosA
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
 * (Px + Vx * t)²          + (Py + Vy * t)²          = (q + w * t)²        + r² - 2((q + w * t) * r * cosA)
 * Px² + 2Px*Vx*t + Vx²*t² + Py² + 2Py*Vy*t + Vy²*t² = q² + 2q*w*t + w²*t² + r² - 2q*r*cosA - 2w*t*r*cosA
 * 0 = (Vx² + Vy² - w²)*t² + 2(Px*Vx + Py*Vy - w*q + r*w*cosA)*t + (Px² + Py² - q² - r² + 2q*r*cosA)
 */
fun solve(pv: Pair<Vector2f, Vector2f>, q: Float, w: Float, r: Float, cosA: Float): Float? {
    val (p, v) = pv
    val a = (v.x * v.x) + (v.y * v.y) - (w * w)
    val b = (p.x * v.x) + (p.y * v.y) - (w * q) + (w * r * cosA)
    val c = (p.x * p.x) + (p.y * p.y) - (r * r) - (q * q) + (2 * q * r * cosA)

    val (t1, t2) = quad(a, 2 * b, c) ?: return null
    return when {
        t1 >= 0 && t2 >= 0 -> min(t1, t2)
        t1 <= 0 != t2 <= 0 -> max(t1, t2)
        else -> null
    }
}

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
    val r2 = r * r
    val d2 = p.lengthSquared
    val d = sqrt(d2)

    // Undefined for circles containing the origin.
    if (d < r) return null

    // Calculate one of the points of tangency in a rotated frame
    // of reference, where the point 'p' lies on the x-axis.
    val hx = d - r2 / d
    val hy = sqrt(d2 - hx * hx - r2)

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

/** 2D rotation matrix. */
class Rotation(private val sin: Float, private val cos: Float) {

    constructor(angle: Float) : this(
        sin(angle * DEGREES_TO_RADIANS),
        cos(angle * DEGREES_TO_RADIANS),
    )

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
    val dist = to - from
    return dist - round(dist / 360f) * 360f
}

fun absShortestRotation(from: Float, to: Float): Float {
    return abs(shortestRotation(from, to))
}

/** Clamps angle to range [0,360) */
fun clampAngle(angle: Float): Float {
    return angle - floor(angle / 360f) * 360f
}
