package com.genir.aitweaks.core.utils

import org.lwjgl.util.vector.Vector2f
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class QuadSolution(val x1: Float, val x2: Float) {
    val smallerNonNegative: Float?
        get() = when {
            // Equation has no positive solutions.
            x1 < 0f && x2 < 0f -> null

            // x2 is the only positive solution.
            x1 < 0f -> x2

            // x1 is the only positive solution.
            x2 < 0f -> x1

            else -> min(x1, x2)
        }

    val largerNonNegative: Float?
        get() = when {
            // Equation has no positive solutions.
            x1 < 0f && x2 < 0f -> null

            // x2 is the only positive solution.
            x1 < 0f -> x2

            // x1 is the only positive solution.
            x2 < 0f -> x1

            else -> max(x1, x2)
        }
}

/** Solve the quadratic equation [ax² + bx + c = 0] for x. */
fun quad(a: Float, b: Float, c: Float): QuadSolution? {
    // Equation is degenerated to const case.
    if (a == 0f && b == 0f) {
        return null
    }

    // Equation is degenerated to linear case.
    if (a == 0f) {
        val x = -c / b
        return QuadSolution(x, x)
    }

    val delta = b * b - 4 * a * c

    // Equation has no real solutions.
    if (delta < 0) {
        return null
    }

    val d = sqrt(delta)
    val x1 = (-b + d) / (2 * a)
    val x2 = (-b - d) / (2 * a)

    return QuadSolution(x1, x2)
}

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
 * Equation can be expanded in the following way:
 * (Px + Vx * t)²          + (Py + Vy * t)²          = (q + w * t)²        + r² - 2((q + w * t) * r * cosA)
 * Px² + 2Px*Vx*t + Vx²*t² + Py² + 2Py*Vy*t + Vy²*t² = q² + 2q*w*t + w²*t² + r² - 2q*r*cosA - 2w*t*r*cosA
 * 0 = (Vx² + Vy² - w²)*t² + 2(Px*Vx + Py*Vy - w*q + r*w*cosA)*t + (Px² + Py² - q² - r² + 2q*r*cosA)
 */
fun solve(p: Vector2f, v: Vector2f, q: Float, w: Float, r: Float, cosA: Float): QuadSolution? {
    val a = (v.x * v.x) + (v.y * v.y) - (w * w)
    val b = (p.x * v.x) + (p.y * v.y) - (w * q) + (w * r * cosA)
    val c = (p.x * p.x) + (p.y * p.y) - (r * r) - (q * q) + (2 * q * r * cosA)

    return quad(a, 2 * b, c)
}

fun solve(pv: Pair<Vector2f, Vector2f>, q: Float, w: Float, r: Float, cosA: Float): QuadSolution? {
    return solve(pv.first, pv.second, q, w, r, cosA)
}

/** Time after which point P travelling with velocity V
 * will find itself at R distance from (0,0). */
fun solve(p: Vector2f, v: Vector2f, r: Float): QuadSolution? {
    val a = (v.x * v.x) + (v.y * v.y)
    val b = (p.x * v.x) + (p.y * v.y)
    val c = (p.x * p.x) + (p.y * p.y) - (r * r)

    return quad(a, 2 * b, c)
}

fun solve(pv: Pair<Vector2f, Vector2f>, r: Float): QuadSolution? {
    return solve(pv, 0f, 0f, r, 0f)
}
