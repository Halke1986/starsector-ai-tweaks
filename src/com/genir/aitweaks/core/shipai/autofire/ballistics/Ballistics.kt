package com.genir.aitweaks.core.shipai.autofire.ballistics

import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.utils.pointsOfTangency
import com.genir.aitweaks.core.utils.solve
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
import com.genir.aitweaks.core.utils.types.LinearMotion
import org.lwjgl.util.vector.Vector2f

/**
 * All functions in this file take into account target velocity
 * when calculating hit location. Target acceleration is ignored.
 *
 * All functions in this file assume a perfectly accurate weapon,
 * shooting projectiles with zero collision radius.
 *
 * All functions in this file return the result in weapon frame
 * of reference, unless stated otherwise.
 */

private const val cos180 = -1f
private const val approachesInfinity = 1e7f

/** Closest possible range at which the projectile fired by the weapon can collide
 * with the target circumference, for any weapon facing. */
fun closestHitRange(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Float {
    val pv = targetMotion(weapon, target.linearMotion, params)
    if (targetAboveWeapon(pv.position, weapon, target)) {
        return 0f
    }

    val projectileOffset = weapon.projectileSpawnOffset
    val projectileFlightDistance = solve(pv, projectileOffset, 1f, target.radius, cos180)?.smallerNonNegative
        ?: return Float.POSITIVE_INFINITY

    return projectileOffset + projectileFlightDistance
}

/** Weapon aim location required to hit center point of a moving target.
 * When the target's speed approaches the speed of the projectile, the intercept
 * time and location approach infinity. In such cases, the function assumes an
 * arbitrary long time period to approximate the target location. */
fun intercept(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Vector2f {
    if (weapon.isUnguidedMissile) {
        return SimulateMissile.missileIntercept(weapon, target)
    }

    val pv = targetMotion(weapon, target.linearMotion, params)
    val projectileFlightDistance = solve(pv, weapon.projectileSpawnOffset, 1f, 0f, 0f)?.smallerNonNegative

    return pv.positionAfter(projectileFlightDistance ?: approachesInfinity)
}

/** Does the weapon have sufficient range and can rotate in its slot to aim at the target. */
fun canTrack(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams, rangeOverride: Float? = null): Boolean {
    val closestHit = closestHitRange(weapon, target, params)
    if (closestHit > (rangeOverride ?: weapon.totalRange)) {
        return false
    }

    val interceptArc = interceptArc(weapon, target, params)
    return weapon.absoluteArc.overlaps(interceptArc)
}

/** Calculates the target intercept arc. If weapon facing is within this arc,
 * the weapon projectile will collide with the target circumference.
 * Similar to intercept point, but not restricted to target center point.
 * For simplicity, the barrel offset is omitted. */
fun interceptArc(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Arc {
    val pv = targetMotion(weapon, target.linearMotion, params)
    val points = pointsOfTangency(pv.position, target.radius)
        ?: return Arc(360f, 0f.direction)

    val target1 = BallisticTarget(weapon.location + points.first, target.velocity, 0f, target.entity)
    val target2 = BallisticTarget(weapon.location + points.second, target.velocity, 0f, target.entity)

    // Attack delay was accounted for when calculating points of tangency.
    val noDelayParams = BallisticParams(params.accuracy, 0f)

    return Arc.fromTo(
        intercept(weapon, target1, noDelayParams).facing,
        intercept(weapon, target2, noDelayParams).facing,
    )
}

/** Closest possible range at which the projectile fired by the weapon can collide
 * with the target circumference, for any weapon facing.
 *
 * Returns hitPoint in target frame of reference and projectile flight range. */
fun closestHitInTargetFrameOfReference(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Pair<Vector2f, Float> {
    val pv = targetMotion(weapon, target.linearMotion, params)
    val range = closestHitRange(weapon, target, params)
    val hitPoint = -pv.positionAfter(range).resized(target.radius)
    return Pair(hitPoint, range)
}

/** Time after which target enters weapon effective firing range. */
fun timeToRange(weapon: WeaponHandle, target: BallisticTarget, range: Float, params: BallisticParams): Float {
    if (range <= 0) {
        return 0f
    }

    val currentRange = closestHitRange(weapon, target, params)

    when {
        // Already in range.
        currentRange <= range -> {
            return 0f
        }

        // Not possible to hit the target if
        // it's faster than the projectile.
        currentRange == Float.POSITIVE_INFINITY -> {
            return Float.POSITIVE_INFINITY
        }
    }

    // Target motion in weapon frame of reference. The projectile velocity
    // is not relevant in the following calculation, therefore the target
    // velocity is not normalized to the projectile speed.
    val vAbs = target.velocity - weapon.ship.velocity
    val targetMotion = LinearMotion(
        position = target.location - weapon.location + vAbs * params.delay,
        velocity = vAbs,
    )

    // Time after which the target crosses the weapon range radius.
    // If the equation has no positive solutions, the target will
    // never cross the radius.
    val timeToCross = solve(targetMotion, range + target.radius)?.smallerNonNegative
        ?: return Float.POSITIVE_INFINITY

    // Time it takes the projectile to reach the weapon range radius.
    val projectileFlightTime = (range - weapon.projectileSpawnOffset) / weapon.projectileSpeed

    // Target began inside range but moving away; the earlier
    // currentRange <= range would have caught any hittable case.
    if (timeToCross < projectileFlightTime) {
        return Float.POSITIVE_INFINITY
    }

    // Weapon should be fired in advance of the target entering the range
    // threshold, so that the projectile meets it at the edge of its range.
    return timeToCross - projectileFlightTime
}

/** Target location and velocity in weapon frame of reference.
 * weapon.projectileSpeed is used as velocity unit.  */
private fun targetMotion(weapon: WeaponHandle, target: LinearMotion, params: BallisticParams): LinearMotion {
    val pAbs = target.position - weapon.location
    val vAbs = target.velocity - weapon.ship.velocity

    return LinearMotion(
        position = pAbs + vAbs * params.delay,
        velocity = vAbs / (weapon.projectileSpeed * params.accuracy),
    )
}

/** True if target collision radius is above weapon projectile spawn radius. */
private fun targetAboveWeapon(locationRelative: Vector2f, weapon: WeaponHandle, target: BallisticTarget): Boolean {
    val d2 = locationRelative.lengthSquared
    val r = weapon.projectileSpawnOffset + target.radius
    return d2 < r * r
}
