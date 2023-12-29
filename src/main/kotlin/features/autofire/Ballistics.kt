package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.extensions.absoluteArcFacing
import com.genir.aitweaks.utils.extensions.aimLocation
import com.genir.aitweaks.utils.extensions.radius
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.sqrt

const val cos90 = 0f
const val cos180 = -1f

/**
 * All functions in this file take into account target velocity
 * when calculating hit location. Target acceleration is ignored.
 *
 * All functions in this file, except willHitCautious(), assume
 * a perfectly accurate weapon, shooting projectiles with zero
 * collision radius.
 */

/** Does the weapon have sufficient range and can rotate in its slot to aim at the target. */
fun canTrack(weapon: WeaponAPI, target: CombatEntityAPI): Boolean {
    val (p, v) = targetCoords(weapon, target)
    val closestDistance = solve(p, v, target.radius, 1f, cos180)

    return closestDistance <= weapon.range && arcsOverlap(
        Arc(weapon.arc, weapon.absoluteArcFacing), interceptArc(p, v, target.radius)
    )
}

/** Weapon aim offset from the target position, required to hit centerpoint of the moving target.
 * Vector2f(NaN,Nan) if the target is faster than the projectile. */
fun interceptOffset(weapon: WeaponAPI, target: CombatEntityAPI): Vector2f {
    val (p, v) = targetCoords(weapon, target)
    return v * solve(p, v, 0f, 1f, 0f)
}

/** Range at which the projectile will collide with the target circumference.
 * NaN if no collision. */
fun hitRange(weapon: WeaponAPI, target: CombatEntityAPI): Float {
    val (p, v) = projectileCoords(weapon, target)
    return solve(p, v, target.radius, 0f, 0f)
}

/** Calculates if the projectile will collide with target circumference,
 * ignoring weapon range. */
fun willHit(weapon: WeaponAPI, target: CombatEntityAPI) = hitRange(weapon, target) > 0f

/** Calculates if an inaccurate projectile may collide with target circumference,
 * ignoring weapon range. Assumes projectile has 8.0f collision radius, which was
 * determined experimentally in vanilla. */
fun willHitCautious(weapon: WeaponAPI, target: CombatEntityAPI): Boolean {
    val (p, v) = targetCoords(weapon, target)
    return arcsOverlap(Arc(weapon.currSpread, weapon.currAngle), interceptArc(p, v, target.radius + 8.0f))
}

/** Calculates if a perfectly accurate projectile will collide with target bounds,
 * ignoring weapon range. */
fun willHitBounds(weapon: WeaponAPI, target: CombatEntityAPI): Boolean {
    val (_, v) = projectileCoords(weapon, target)
    return CollisionUtils.getCollisionPoint(weapon.location, weapon.location + v * 10e5f, target) != null
}

/** Target location and velocity in projectile frame of reference. */
private fun targetCoords(weapon: WeaponAPI, target: CombatEntityAPI) = Pair(
    target.aimLocation - weapon.location,
    (target.velocity - weapon.ship.velocity) / weapon.projectileSpeed,
)

/** Projectile location and velocity in target frame of reference. */
private fun projectileCoords(weapon: WeaponAPI, target: CombatEntityAPI) = Pair(
    weapon.location - target.aimLocation,
    unitVector(weapon.currAngle) + (weapon.ship.velocity - target.velocity) / weapon.projectileSpeed,
)

private fun interceptArc(p: Vector2f, v: Vector2f, radius: Float): Arc {
    val tangentDistance = solve(p, v, radius, 1f, cos90)

    // Target is directly over the weapon.
    if (tangentDistance.isNaN()) Arc(360f, 0f)

    return Arc(
        arc = atan(radius / tangentDistance) * 2f,
        facing = VectorUtils.getFacing(p + v * tangentDistance),
    )
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
private fun solve(p: Vector2f, v: Vector2f, r: Float, w: Float, cosA: Float): Float {
    val a = v.lengthSquared() - w * w
    val b = 2f * (p.x * v.x + p.y * v.y + r * w * cosA)
    val c = p.lengthSquared() - r * r

    val (t1, t2) = quad(a, b, c)
    return when {
        t1 > 0 && t1 < t2 -> t1
        t2 > 0 -> t2
        else -> Float.NaN
    }
}

/**
 * solve quadratic equation [ax^2 + bx + c = 0] for x.
 */
private fun quad(a: Float, b: Float, c: Float): Pair<Float, Float> {
    val d = b * b - 4f * a * c
    return when {
        d < 0 -> Pair(Float.NaN, Float.NaN)
        a == 0f -> (2 * c / -b).let { Pair(it, it) }
        else -> sqrt(d).let { Pair((-b + it) / (2 * a), (-b - it) / (2 * a)) }
    }
}
