package com.genir.aitweaks.core.shipai.autofire.ballistics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.utils.Bounds
import com.genir.aitweaks.core.utils.pointsOfTangency
import com.genir.aitweaks.core.utils.solve
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction
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


/** Closest possible range at which the projectile can collide with the target
 * circumference, for any weapon facing.
 * When the target's speed approaches the speed of the projectile, the intercept
 * time and location approach infinity. In such cases, the function assumes an
 * arbitrary long time period to approximate the target location. */
fun closestHitRange(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Float {
    val pv = targetCoords(weapon, target, params)
    if (targetAboveWeapon(pv.first, weapon, target)) {
        return 0f
    }

    val projectileOffset = weapon.projectileSpawnOffset
    val projectileFlightDistance = solve(pv, projectileOffset, 1f, target.radius, cos180)?.smallerNonNegative
        ?: return approachesInfinity

    return projectileOffset + projectileFlightDistance
}

fun closestHitInTargetFrameOfReference(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Pair<Vector2f, Float> {
    val (p, v) = targetCoords(weapon, target, params)
    val range = closestHitRange(weapon, target, params)
    val hitPoint = -(p + v * range).resized(target.radius)
    return Pair(hitPoint, range)
}

/** Weapon aim location required to hit center point of a moving target. */
fun intercept(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Vector2f {
    if (weapon.isUnguidedMissile) {
        return SimulateMissile.missileIntercept(weapon, target)
    }

    val (p, v) = targetCoords(weapon, target, params)
    val projectileFlightDistance = solve(p, v, weapon.projectileSpawnOffset, 1f, 0f, 0f)?.smallerNonNegative

    return p + v * (projectileFlightDistance ?: approachesInfinity)
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
    val (p, _) = targetCoords(weapon, target, params)
    val points = pointsOfTangency(p, target.radius)
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

/** Calculates if hypothetical projectile will collide with the target circumference,
 * given current weapon facing.
 * Collision range is returned, null if no collision. */
fun willHitCircumference(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Float? {
    val pv = projectileCoords(weapon, target, params)
    val projectileFlightDistance = solve(pv, target.radius)?.smallerNonNegative
        ?: return null

    return weapon.projectileSpawnOffset + projectileFlightDistance
}

/** Calculates if a projectile will collide with the target circumference.
 * Collision range is returned, null if no collision. */
fun willHitCircumference(projectile: DamagingProjectileAPI, target: BallisticTarget): Float? {
    val projectileVelocity = projectile.facing.direction.unitVector * projectile.moveSpeed + projectile.weapon.ship.velocity

    val p = projectile.location - target.location
    val v = projectileVelocity - target.velocity

    val projectileFlightTime = solve(p, v, target.radius)?.smallerNonNegative
        ?: return null

    return projectileFlightTime * projectile.moveSpeed
}

/** Calculates if a perfectly accurate projectile will collide with target shield,
 * given current weapon facing. Will not detect hits to inside of shield.
 * Collision range is returned, null if no collision. */
fun willHitShield(weapon: WeaponHandle, target: ShipAPI, params: BallisticParams): Float? {
    val shield = target.shield
        ?: return null

    if (shield.isOff) {
        return null
    }

    val (p, v) = projectileCoords(weapon, BallisticTarget.shieldRadius(target), params)
    val projectileFlightDistance = solve(p, v, shield.radius)?.smallerNonNegative
        ?: return null

    val hitPoint = p + v * projectileFlightDistance

    return if (shield.isHit(hitPoint)) {
        weapon.projectileSpawnOffset + projectileFlightDistance
    } else {
        null
    }
}

/** Calculates if a perfectly accurate projectile will collide with target bounds,
 * given current weapon facing.
 * Collision range is returned, null if no collision. */
fun willHitBounds(weapon: WeaponHandle, target: ShipAPI, params: BallisticParams): Float? {
    val (p, v) = projectileCoords(weapon, BallisticTarget.collisionRadius(target), params)
    val projectileFlightDistance = Bounds.collision(p, v, target)
        ?: return null

    return weapon.projectileSpawnOffset + projectileFlightDistance
}

/** Target location and velocity in weapon frame of reference. */
private fun targetCoords(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Pair<Vector2f, Vector2f> {
    val vAbs = target.velocity - weapon.ship.velocity
    val pAbs = target.location - weapon.location

    val p = pAbs + vAbs * (params.delay)
    val v = vAbs / (weapon.projectileSpeed * params.accuracy)

    return Pair(p, v)
}

/** Projectile location and velocity in target frame of reference. */
private fun projectileCoords(weapon: WeaponHandle, target: BallisticTarget, params: BallisticParams): Pair<Vector2f, Vector2f> {
    val vAbs = weapon.ship.velocity - target.velocity
    val pAbs = weapon.location - target.location
    val vProj = weapon.angleWhenFiring.unitVector

    val p = pAbs + vAbs * params.delay + vProj * weapon.projectileSpawnOffset
    val v = vProj + vAbs / (weapon.projectileSpeed * params.accuracy)

    return Pair(p, v)
}

/** True if target collision radius is above weapon projectile spawn radius.  */
private fun targetAboveWeapon(locationRelative: Vector2f, weapon: WeaponHandle, target: BallisticTarget): Boolean {
    val d2 = locationRelative.lengthSquared
    val r = weapon.projectileSpawnOffset + target.radius
    return d2 < r * r
}

/** Ballistic calculations are performed before the game engine updates weapon states.
 * For beam weapons, which fire after completing rotation, this means the ballistic
 * calculations use outdated facing values.
 * To prevent errors caused by this timing mismatch, the WeaponHandle.angleWhenFiring
 * method computes the expected weapon facing at the moment of firing. */
private val WeaponHandle.angleWhenFiring: Direction
    get() {
        val currAngle = currAngle.direction

        // Non-beam weapons fire *before* rotation.
        if (!isBeam)
            return currAngle

        val ai: AutofireAIPlugin = autofirePlugin
            ?: return currAngle

        val target: CombatEntityAPI = (ai.targetShip ?: ai.targetMissile)
            ?: return currAngle

        // Assume beam weapon will aim directly at the target.
        val expectedAngle = (target.location - location).facing
        val offset = currAngle - expectedAngle
        val turnRate = turnRate * Global.getCombatEngine().elapsedInLastFrame

        return if (offset.length < turnRate) {
            expectedAngle
        } else {
            currAngle
        }
    }
