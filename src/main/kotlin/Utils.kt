package com.genir.aitweaks

import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.MathUtils.getShortestRotation
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

fun willHitShield(weapon: WeaponAPI, target: ShipAPI?) = when {
    target == null -> false
    target.shield == null -> false
    target.shield.isOff -> false
    else -> willHitActiveShieldArc(weapon, target.shield)
}

fun willHitActiveShieldArc(weapon: WeaponAPI, shield: ShieldAPI): Boolean {
    val tgtFacing = (weapon.location - shield.location).getFacing()
    val attackAngle = getShortestRotation(tgtFacing, shield.facing)
    return kotlin.math.abs(attackAngle) < (shield.activeArc / 2)
}

/**
 * Compute time after which point p moving with speed dp
 * will intersect circle centered at point 0,0, with initial
 * radius r expanding with speed dr.
 * This is done by solving the following equation for t:
 *
 * r+dr*t = |p+dp*t|
 *
 * The smaller of the two positive solutions is returned.
 * If no positive solution exists, null is returned.
 */
fun intersectionTime(p: Vector2f, dp: Vector2f, r: Float, dr: Float): Float? {
    // the solved equation can be expanded the following way:
    // r + dr * t = |p + dp * t|
    // r + dr * t = sqrt[ (p.x + dp.x * t)^2 + (p.y + dp.y * t)^2 ]
    // (r + dr * t)^2 = (p.x + dp.x * t)^2 + (p.y + dp.y * t)^2
    // 0 = (dp.x^2 + dp.y^2 - dr^2)*t^2 + 2(p.x*dp.x + p.y*dp.y - r*dr)*t + (p.x^2 + p.y^2 - r^2)
    val a = dp.lengthSquared() - dr * dr
    val b = 2f * (p.x * dp.x + p.y * dp.y - r * dr)
    val c = p.lengthSquared() - r * r

    val (t1, t2) = solve(a, b, c) ?: return null

    if (t1 < 0 && t2 < 0) return null
    if (t1 < 0 || t2 < 0) return max(t1, t2)
    return min(t1, t2)
}

/**
 * solve quadratic equation [ax^2 + bx + c = 0] for x.
 */
fun solve(a: Float, b: Float, c: Float): Pair<Float, Float>? {
    val d = b * b - 4f * a * c
    return when {
        MathUtils.equals(d, 0f) || d < 0 -> null
        MathUtils.equals(a, 0f) -> (2 * c / -b).let { Pair(it, it) }
        else -> sqrt(d).let { Pair((-b + it) / (2 * a), (-b - it) / (2 * a)) }
    }
}

internal infix operator fun Vector2f.times(d: Float): Vector2f = Vector2f(d * x, d * y)

fun rotateAroundPivot(toRotate: Vector2f, pivot: Vector2f, angle: Float): Vector2f =
    VectorUtils.rotateAroundPivot(toRotate, pivot, angle, Vector2f())

fun rotate(toRotate: Vector2f, angle: Float): Vector2f = VectorUtils.rotate(toRotate, angle, Vector2f())

fun unitVector(angle: Float): Vector2f = VectorUtils.rotate(Vector2f(1f, 0f), angle)