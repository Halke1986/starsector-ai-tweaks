package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.extensions.absoluteArcFacing
import com.genir.aitweaks.utils.extensions.aimLocation
import com.genir.aitweaks.utils.extensions.aliveShield
import com.genir.aitweaks.utils.extensions.radius
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.sqrt

/**
 * All functions in this file take into account target velocity
 * when calculating hit location. Target acceleration is ignored.
 *
 * All functions in this file, except willHitCautious(), assume
 * a perfectly accurate weapon, shooting projectiles with zero
 * collision radius.
 */

private const val cos90 = 0f
private const val cos180 = -1f

/** Does the weapon have sufficient range and can rotate in its slot to aim at the target. */
fun canTrack(weapon: WeaponAPI, target: CombatEntityAPI): Boolean {
    val closestHit = closestHitRange(weapon, target) ?: return false
    if (closestHit > weapon.range) return false

    val arc = interceptArc(weapon, target) ?: return false
    return arcsOverlap(Arc(weapon.arc, weapon.absoluteArcFacing), arc)
}

/** Weapon aim offset from the target position, required to hit center point of a moving target.
 * Null if projectile is slower than the target. */
fun interceptOffset(weapon: WeaponAPI, target: CombatEntityAPI): Vector2f? {
    val pv = targetCoords(weapon, target)
    val range = solve(pv, 0f, 1f, 0f) ?: return null
    return pv.second * range
}

/** Calculates the target intercept arc. If weapon facing is within this arc,
 * the weapon projectile will collide with target circumference. Similar to
 * intercept point, but not restricted to target center point.
 * Null if projectile is slower than the target. */
fun interceptArc(weapon: WeaponAPI, target: CombatEntityAPI): Arc? {
    val (p, v) = targetCoords(weapon, target)
    if (targetAboveWeapon(p, target.radius)) return Arc(360f, 0f)

    val tangentDistance = solve(Pair(p, v), target.radius, 1f, cos90) ?: return null
    return Arc(
        arc = atan(target.radius / tangentDistance) * 2f,
        facing = VectorUtils.getFacing(p + v * tangentDistance),
    )
}

/** Closest possible range at which the projectile can collide with the target circumference,
 * for any weapon facing. Null if the target is faster than the projectile. */
fun closestHitRange(weapon: WeaponAPI, target: CombatEntityAPI): Float? {
    val pv = targetCoords(weapon, target)
    return if (targetAboveWeapon(pv.first, target.radius)) 0f
    else solve(pv, target.radius, 1f, cos180)
}

/** Analyzes the potential collision between projectile and target. Returns collision range.
 * Boolean return parameter is true if projectile will hit target shield; always false for
 * fighters and missiles. Null if no collision. */
fun analyzeHit(weapon: WeaponAPI, target: CombatEntityAPI): Pair<Float, Boolean>? {
    val range = willHitCircumference(weapon, target) ?: return null

    // Simple circumference collision is enough for missiles and fighters.
    if (target is MissileAPI || (target as? ShipAPI)?.isFighter == true) return Pair(range, false)

    // Check shield arc collision.
    val shield = target.aliveShield
    if (shield != null && shield.isOn) {
        val (p, v) = projectileCoords(weapon, target)
        val hitPoint = p + v * range
        if (vectorInArc(hitPoint, Arc(shield.activeArc, shield.facing))) return Pair(range, true)
    }

    // Check bounds collision.
    val boundRange = willHitBounds(weapon, target) ?: return null
    return Pair(boundRange, false)
}

/** Calculates if projectile will collide with the target circumference,
 * given current weapon facing. Weapon range is ignored. */
fun willHitCircumference(weapon: WeaponAPI, target: CombatEntityAPI): Float? =
    solve(projectileCoords(weapon, target), target.radius, 0f, 0f)

/** Calculates if an inaccurate projectile may collide with target circumference,
 * given current weapon facing. Weapon range is ignored.  */
// TODO add safety to spread instead of radius
fun willHitCircumferenceCautious(weapon: WeaponAPI, target: CombatEntityAPI): Boolean {
    val arc = interceptArc(weapon, target) ?: return false
    return arcsOverlap(Arc(weapon.spec.maxSpread + 2f, weapon.currAngle), arc)
}

/** Calculates if a perfectly accurate projectile will collide with target bounds,
 * given current weapon facing. Collision range is returned, null if no collision.
 * Custom implementation is roughly 5 times faster than CollisionUtils.getCollisionPoint */
fun willHitBounds(weapon: WeaponAPI, target: CombatEntityAPI): Float? {
    val bounds = target.exactBounds ?: return null
    val pv = projectileCoords(weapon, target)

    val cosA: Float = cos(-target.facing)
    val sinA: Float = sin(-target.facing)

    // Rotate weapon coordinates into target frame of reference.
    // That way the target bounds don't need to be transformed.
    // Rotation is implemented in place, as opposed to using library
    // call, for better execution time.
    fun rotate(v: Vector2f, sinA: Float, cosA: Float) = Vector2f(
        v.x * cosA - v.y * sinA,
        v.x * sinA + v.y * cosA,
    )

    val q1 = rotate(pv.first, sinA, cosA)
    val vr = rotate(pv.second, sinA, cosA)
    val q2 = q1 + vr

    return bounds.origSegments.fold(null, fun(closest: Float?, segment): Float? {
        // Both sides of the following equation set represent the collision point:
        // p1 + k(p2-p1) = q1 + t(q2-q1)
        // Solve if for k and t.
        val p1 = segment.p1
        val p2 = segment.p2

        val dp = p2 - p1
        val dq = q2 - q1

        val d = dp.x * dq.y - dp.y * dq.x

        val pqy = p1.y - q1.y
        val qpx = q1.x - p1.x

        val k = (pqy * dq.x + qpx * dq.y) / d
        if (k < 0f || k > 1f) return closest // no collision

        val t = (pqy * dp.x + qpx * dp.y) / d
        return if (t > 0 && (closest == null || t < closest)) t
        else closest
    })
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
 * If no positive solution exists, null is returned.
 *
 * Equation can be expanded the following way:
 * (|P + V * t|)^2 = (w * t)^2 + r^2 - 2(w * t * r * cosA)
 * (Px + Vx * t)^2 + (Py + Vy * t)^2 = = w^2 * t^2 + r^2 - 2(w * t * r * cosA)
 * (Vx^2 + Vy^2 - w^2)*t^2 + 2(Px*Vx + Py*Vy + r*w*cosA)*t + (Px^2 + Py^2 - r^2) = 0
 */
private fun solve(pv: Pair<Vector2f, Vector2f>, r: Float, w: Float, cosA: Float): Float? {
    val (p, v) = pv
    val a = v.lengthSquared() - w * w
    val b = 2f * (p.x * v.x + p.y * v.y + r * w * cosA)
    val c = p.lengthSquared() - r * r

    val (t1, t2) = quad(a, b, c) ?: return null
    return when {
        t1 > 0 && t1 < t2 -> t1
        t2 > 0 -> t2
        else -> null
    }
}

/** Solve quadratic equation [ax^2 + bx + c = 0] for x. */
private fun quad(a: Float, b: Float, c: Float): Pair<Float, Float>? {
    val d = b * b - 4f * a * c
    return when {
        d < 0 -> null
        a == 0f -> (2 * c / -b).let { Pair(it, it) }
        else -> sqrt(d).let { Pair((-b + it) / (2 * a), (-b - it) / (2 * a)) }
    }
}

private fun targetAboveWeapon(p: Vector2f, r: Float) = p.lengthSquared() < r * r