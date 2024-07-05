package com.genir.aitweaks.core.utils.attack

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.absoluteArcFacing
import com.genir.aitweaks.core.utils.extensions.rotated
import com.genir.aitweaks.core.utils.extensions.totalRange
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

/**
 * All functions in this file take into account target velocity
 * when calculating hit location. Target acceleration is ignored.
 *
 * All functions in this file, except willHitCircumferenceCautious(),
 * assume a perfectly accurate weapon, shooting projectiles with zero
 * collision radius.
 */

private const val cos90 = 0f
private const val cos180 = -1f

/** Simplified representation of a circular, moving target. */
data class AttackTarget(val location: Vector2f, val velocity: Vector2f, val radius: Float) {
    constructor(entity: CombatEntityAPI) : this(entity.location, entity.velocity, entity.collisionRadius)
}

/** Use ship shields as an approximation for its circumference */
fun targetShield(ship: ShipAPI): AttackTarget {
    return AttackTarget(ship.shieldCenterEvenIfNoShield, ship.velocity, ship.shieldRadiusEvenIfNoShield)
}

/** Weapon attack parameters: accuracy and delay until attack. */
data class BallisticParams(val accuracy: Float, val delay: Float)

fun defaultBallisticParams() = BallisticParams(1f, 0f)

/** Weapon aim location required to hit center point of a moving target.
 * Null if projectile is slower than the target. */
fun intercept(weapon: WeaponAPI, target: AttackTarget, params: BallisticParams): Vector2f? {
    val pv = targetCoords(weapon, target, params)
    val range = solve(pv, 0f, 1f, 0f) ?: return null
    val offset = pv.second * range

    return target.location + (target.velocity - weapon.ship.velocity) * params.delay + offset
}

/** Does the weapon have sufficient range and can rotate in its slot to aim at the target. */
fun canTrack(weapon: WeaponAPI, target: AttackTarget, params: BallisticParams, rangeOverride: Float? = null): Boolean {
    val closestHit = closestHitRange(weapon, target, params) ?: return false
    if (closestHit > (rangeOverride ?: weapon.totalRange)) return false

    val arc = interceptArc(weapon, target, params) ?: return false
    return arcsOverlap(Arc(weapon.arc, weapon.absoluteArcFacing), arc)
}

/** Closest possible range at which the projectile can collide with the target circumference,
 * for any weapon facing. Null if the target is faster than the projectile. */
fun closestHitRange(weapon: WeaponAPI, target: AttackTarget, params: BallisticParams): Float? {
    val pv = targetCoords(weapon, target, params)
    return if (targetAboveZero(pv.first, target.radius)) 0f
    else solve(pv, target.radius, 1f, cos180)
}

/** Calculates the target intercept arc. If weapon facing is within this arc,
 * the weapon projectile will collide with target circumference.
 * Similar to intercept point, but not restricted to target center point.
 * Null if projectile is slower than the target. */
fun interceptArc(weapon: WeaponAPI, target: AttackTarget, params: BallisticParams): Arc? {
    val (p, v) = targetCoords(weapon, target, params)
    if (targetAboveZero(p, target.radius)) return Arc(360f, 0f)

    val tangentDistance = solve(Pair(p, v), target.radius, 1f, cos90) ?: return null
    return Arc(
        arc = atan(target.radius / tangentDistance) * RADIANS_TO_DEGREES * 2f,
        facing = VectorUtils.getFacing(p + v * tangentDistance),
    )
}

/** Calculates if projectile will collide with the target circumference,
 * given current weapon facing. Weapon range is ignored. */
fun willHitCircumference(weapon: WeaponAPI, target: AttackTarget, params: BallisticParams): Float? {
    return solve(projectileCoords(weapon, target, params), target.radius, 0f, 0f)
}

/** Calculates if a perfectly accurate projectile will collide with target shield,
 * given current weapon facing. Will not detect hits to inside of shield.
 * Collision range is returned, null if no collision. */
fun willHitShield(weapon: WeaponAPI, target: ShipAPI, params: BallisticParams): Float? {
    val shield = target.shield ?: return null
    if (shield.isOff) return null

    val (p, v) = projectileCoords(weapon, targetShield(target), params)
    val range = solve(Pair(p, v), shield.radius, 0f, 0f) ?: return null
    val hitPoint = p + v * range

    return if (vectorInArc(hitPoint, Arc(shield.activeArc, shield.facing))) range else null
}

/** Calculates if an inaccurate projectile may collide with target shield,
 * given current weapon facing. Assumes shield is up and 360 degree.
 * Weapon range is ignored.  */
fun willHitShieldCautious(weapon: WeaponAPI, target: ShipAPI, params: BallisticParams): Boolean {
    val arc = interceptArc(weapon, targetShield(target), params) ?: return false
    return arcsOverlap(Arc(weapon.spec.maxSpread + 2f, weapon.currAngle), arc)
}

/** Calculates if a perfectly accurate projectile will collide with target bounds,
 * given current weapon facing. Collision range is returned, null if no collision. */
fun willHitBounds(weapon: WeaponAPI, target: ShipAPI, params: BallisticParams): Float? {
    val bounds = target.exactBounds ?: return null
    val pv = projectileCoords(weapon, AttackTarget(target), params)

    // Rotate weapon coordinates into target frame of reference.
    // That way the target bounds don't need to be transformed.
    val r = Rotation(-target.facing)
    val q1 = pv.first.rotated(r)
    val vr = pv.second.rotated(r)
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

/** Target location and velocity in weapon frame of reference. */
private fun targetCoords(weapon: WeaponAPI, target: AttackTarget, params: BallisticParams) = Pair(
    (target.location - weapon.location) + (target.velocity - weapon.ship.velocity) * params.delay,
    (target.velocity - weapon.ship.velocity) / (weapon.projectileSpeed * params.accuracy),
)

/** Projectile location and velocity in target frame of reference. */
private fun projectileCoords(weapon: WeaponAPI, target: AttackTarget, params: BallisticParams) = Pair(
    (weapon.location - target.location) + (weapon.ship.velocity - target.velocity) * params.delay,
    unitVector(weapon.currAngle) + (weapon.ship.velocity - target.velocity) / (weapon.projectileSpeed * params.accuracy),
)

/** True if coordinate system zero point is within entity radius */
private fun targetAboveZero(p: Vector2f, r: Float) = p.lengthSquared() <= r * r
