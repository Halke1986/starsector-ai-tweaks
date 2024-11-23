package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.*
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
fun closestHitRange(weapon: WeaponAPI, target: BallisticTarget, params: BallisticParams): Float {
    val pv = targetCoords(weapon, target, params)
    if (targetAboveWeapon(pv.first, weapon, target)) return 0f

    val rangeFromBarrel = solve(pv, weapon.barrelOffset, 1f, target.radius, cos180)
    return weapon.barrelOffset + (rangeFromBarrel ?: approachesInfinity)
}

/** Weapon aim location required to hit center point of a moving target. */
fun intercept(weapon: WeaponAPI, target: BallisticTarget, params: BallisticParams): Vector2f {
    if (weapon.isUnguidedMissile) return SimulateMissile.missileIntercept(weapon, target)

    val (p, v) = targetCoords(weapon, target, params)
    if (targetAboveWeapon(p, weapon, target)) return target.location

    val range = solve(Pair(p, v), weapon.barrelOffset, 1f, 0f, 0f) ?: approachesInfinity
    return p + v * range
}

/** Does the weapon have sufficient range and can rotate in its slot to aim at the target. */
fun canTrack(weapon: WeaponAPI, target: BallisticTarget, params: BallisticParams, rangeOverride: Float? = null): Boolean {
    val closestHit = closestHitRange(weapon, target, params)
    if (closestHit > (rangeOverride ?: weapon.totalRange)) return false

    val interceptArc = interceptArc(weapon, target, params)
    return Arc(weapon.arc, weapon.absoluteArcFacing).overlaps(interceptArc)
}

/** Calculates the target intercept arc. If weapon facing is within this arc,
 * the weapon projectile will collide with the target circumference.
 * Similar to intercept point, but not restricted to target center point.
 * For simplicity, the barrel offset is omitted. */
fun interceptArc(weapon: WeaponAPI, target: BallisticTarget, params: BallisticParams): Arc {
    val (p, _) = targetCoords(weapon, target, params)
    val points = pointsOfTangency(p, target.radius) ?: return Arc(360f, 0f)

    val target1 = BallisticTarget(weapon.location + points.first, target.velocity, 0f)
    val target2 = BallisticTarget(weapon.location + points.second, target.velocity, 0f)

    return Arc.fromTo(
        intercept(weapon, target1, params).facing,
        intercept(weapon, target2, params).facing,
    )
}

/** Calculates if projectile will collide with the target circumference,
 * given current weapon facing. Weapon range is ignored. */
fun willHitCircumference(weapon: WeaponAPI, target: BallisticTarget, params: BallisticParams): Float? {
    return solve(projectileCoords(weapon, target, params), target.radius)
}

/** Calculates if a perfectly accurate projectile will collide with target shield,
 * given current weapon facing. Will not detect hits to inside of shield.
 * Collision range is returned, null if no collision. */
fun willHitShield(weapon: WeaponAPI, target: ShipAPI, params: BallisticParams): Float? {
    val shield = target.shield ?: return null
    if (shield.isOff) return null

    val (p, v) = projectileCoords(weapon, BallisticTarget.shield(target), params)
    val range = solve(Pair(p, v), shield.radius) ?: return null
    val hitPoint = p + v * range

    return if (Arc(shield.activeArc, shield.facing).contains(hitPoint)) range else null
}

/** Calculates if an inaccurate projectile may collide with target,
 * given current weapon facing. Weapon range is ignored. */
fun willHitCautious(weapon: WeaponAPI, target: BallisticTarget, params: BallisticParams): Boolean {
    val interceptArc = interceptArc(weapon, target, params)
    return Arc(weapon.spec.maxSpread + 2f, weapon.currAngle).overlaps(interceptArc)
}

/** Calculates if a perfectly accurate projectile will collide with target bounds,
 * given current weapon facing. Collision range is returned, null if no collision. */
fun willHitBounds(weapon: WeaponAPI, target: ShipAPI, params: BallisticParams): Float? {
    val (p, v) = projectileCoords(weapon, BallisticTarget.entity(target), params)
    return state.bounds.collision(p, v, target)
}

/** Target location and velocity in weapon frame of reference. */
private fun targetCoords(weapon: WeaponAPI, target: BallisticTarget, params: BallisticParams): Pair<Vector2f, Vector2f> {
    val vAbs = (target.velocity - weapon.ship.velocity)
    val pAbs = (target.location - weapon.location)

    val p = pAbs + vAbs * (params.delay)
    val v = vAbs / (weapon.trueProjectileSpeed * params.accuracy)

    return Pair(p, v)
}

/** Projectile location and velocity in target frame of reference. */
private fun projectileCoords(weapon: WeaponAPI, target: BallisticTarget, params: BallisticParams): Pair<Vector2f, Vector2f> {
    val vAbs = (weapon.ship.velocity - target.velocity)
    val pAbs = (weapon.location - target.location)
    val vProj = unitVector(weapon.currAngle)

    val p = pAbs + vAbs * params.delay + vProj * weapon.barrelOffset
    val v = vProj + vAbs / (weapon.trueProjectileSpeed * params.accuracy)

    return Pair(p, v)
}

/** True if target collision radius is above weapon barrel radius.  */
private fun targetAboveWeapon(locationRelative: Vector2f, weapon: WeaponAPI, target: BallisticTarget): Boolean {
    val d2 = locationRelative.lengthSquared
    val r = weapon.barrelOffset + target.radius
    return d2 < r * r
}
