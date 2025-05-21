package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.utils.Bounds
import com.genir.aitweaks.core.utils.pointsOfTangency
import com.genir.aitweaks.core.utils.solve
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
import org.lwjgl.util.vector.Vector2f

/**
 * All functions in this file take into account target velocity
 * when calculating hit location. Target acceleration is ignored.
 *
 * All functions in this file, except willHitCircumferenceCautious(),
 * assume a perfectly accurate weapon, shooting projectiles with zero
 * collision radius.
 */

private const val cos180 = -1f
private const val approachesInfinity = 1e7f

/** Weapon attack parameters: accuracy and delay until attack. */
data class BallisticParams(val accuracy: Float, val delay: Float)

val defaultBallisticParams = BallisticParams(1f, 0f)

/** Closest possible range at which the projectile can collide with the target
 * circumference, for any weapon facing.
 * When the target's speed approaches the speed of the projectile, the intercept
 * time and location approach infinity. In such cases, the function assumes an
 * arbitrary long time period to approximate the target location. */
fun closestHitRange(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Float {
    val pv = targetCoords(weapon, target, params)
    if (targetAboveWeapon(pv.first, weapon, target)) return 0f

    val projectileOffset = weapon.projectileSpawnOffset
    val rangeFromBarrel = solve(pv, projectileOffset, 1f, target.radius, cos180)?.smallerNonNegative

    return projectileOffset + (rangeFromBarrel ?: approachesInfinity)
}

/** Weapon aim location required to hit center point of a moving target. */
fun intercept(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Vector2f {
    if (weapon.isUnguidedMissile) {
        return SimulateMissile.missileIntercept(weapon, target)
    }

    val (p, v) = targetCoords(weapon, target, params)
    val range = solve(p, v, weapon.projectileSpawnOffset, 1f, 0f, 0f)?.smallerNonNegative
    return p + v * (range ?: approachesInfinity)
}

/** Does the weapon have sufficient range and can rotate in its slot to aim at the target. */
fun canTrack(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams, rangeOverride: Float? = null): Boolean {
    val closestHit = closestHitRange(weapon, target, params)
    if (closestHit > (rangeOverride ?: weapon.totalRange)) return false

    val interceptArc = interceptArc(weapon, target, params)
    return weapon.absoluteArc.overlaps(interceptArc)
}

/** Calculates the target intercept arc. If weapon facing is within this arc,
 * the weapon projectile will collide with the target circumference.
 * Similar to intercept point, but not restricted to target center point.
 * For simplicity, the barrel offset is omitted. */
fun interceptArc(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Arc {
    val (p, _) = targetCoords(weapon, target, params)
    val points = pointsOfTangency(p, target.radius) ?: return Arc(360f, 0f.direction)

    val target1 = BallisticTarget(weapon.location + points.first, target.velocity, 0f)
    val target2 = BallisticTarget(weapon.location + points.second, target.velocity, 0f)

    // Attack delay was accounted for when calculating points of tangency.
    val noDelayParams = BallisticParams(params.accuracy, 0f)

    return Arc.fromTo(
        intercept(weapon, target1, noDelayParams).facing,
        intercept(weapon, target2, noDelayParams).facing,
    )
}

/** Calculates if projectile will collide with the target circumference,
 * given current weapon facing. Weapon range is ignored. */
fun willHitCircumference(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Float? {
    return solve(projectileCoords(weapon, target, params), target.radius)?.smallerNonNegative
}

/** Calculates if a perfectly accurate projectile will collide with target shield,
 * given current weapon facing. Will not detect hits to inside of shield.
 * Collision range is returned, null if no collision. */
fun willHitShield(weapon: WeaponHandle, target: ShipAPI, params: BallisticParams): Float? {
    val shield = target.shield ?: return null
    if (shield.isOff) return null

    val (p, v) = projectileCoords(weapon, BallisticTarget.shieldRadius(target), params)
    val range = solve(p, v, shield.radius)?.smallerNonNegative ?: return null
    val hitPoint = p + v * range

    return if (Arc(shield.activeArc, shield.facing.direction).contains(hitPoint)) range else null
}

/** Calculates if a perfectly accurate projectile will collide with target bounds,
 * given current weapon facing. Collision range is returned, null if no collision. */
fun willHitBounds(weapon: WeaponHandle, target: ShipAPI, params: BallisticParams): Float? {
    val (p, v) = projectileCoords(weapon, BallisticTarget.collisionRadius(target), params)
    return Bounds.collision(p, v, target)
}

/** Target location and velocity in weapon frame of reference. */
private fun targetCoords(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Pair<Vector2f, Vector2f> {
    val vAbs = (target.velocity - weapon.ship.velocity)
    val pAbs = (target.location - weapon.location)

    val p = pAbs + vAbs * (params.delay)
    val v = vAbs / (weapon.projectileSpeed * params.accuracy)

    return Pair(p, v)
}

/** Projectile location and velocity in target frame of reference. */
private fun projectileCoords(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Pair<Vector2f, Vector2f> {
    val vAbs = (weapon.ship.velocity - target.velocity)
    val pAbs = (weapon.location - target.location)
    val vProj = weapon.currAngle.direction.unitVector

    val p = pAbs + vAbs * params.delay + vProj * weapon.projectileSpawnOffset
    val v = vProj + vAbs / (weapon.projectileSpeed * params.accuracy)

    return Pair(p, v)
}

/** True if target collision radius is above weapon barrel radius.  */
private fun targetAboveWeapon(locationRelative: Vector2f, weapon: WeaponHandle, target: BallisticTarget): Boolean {
    val d2 = locationRelative.lengthSquared
    val r = weapon.projectileSpawnOffset + target.radius
    return d2 < r * r
}
