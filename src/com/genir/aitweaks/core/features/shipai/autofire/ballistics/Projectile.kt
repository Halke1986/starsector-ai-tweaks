package com.genir.aitweaks.core.features.shipai.autofire.ballistics

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

class Projectile(weapon: WeaponAPI) : Ballistics(weapon) {
    companion object {
        private const val cos180 = -1f
        private const val approachesInfinity = 1e7f
    }

    /** Closest possible range at which the projectile can collide with the target
     * circumference, for any weapon facing.
     * When the target's speed approaches the speed of the projectile, the intercept
     * time and location approach infinity. In such cases, the function assumes an
     * arbitrary long time period to approximate the target location. */
    override fun closestHitRange(target: Target, params: BallisticParams): Float {
        val pv = targetCoords(target, params)
        if (targetAboveWeapon(pv.first, weapon, target)) return 0f

        val rangeFromBarrel = solve(pv, weapon.barrelOffset, 1f, target.radius, cos180)
        return weapon.barrelOffset + (rangeFromBarrel ?: approachesInfinity)
    }

    /** Weapon aim location required to hit center point of a moving target. */
    override fun intercept(target: Target, params: BallisticParams): Vector2f {
        if (weapon.isUnguidedMissile) return SimulateMissile.missileIntercept(weapon, target)

        val (p, v) = targetCoords(target, params)
        if (targetAboveWeapon(p, weapon, target)) return target.location

        val range = solve(Pair(p, v), weapon.barrelOffset, 1f, 0f, 0f) ?: approachesInfinity
        return p + v * range
    }

    /** Does the weapon have sufficient range and can rotate in its slot to aim at the target. */
    override fun canTrack(target: Target, params: BallisticParams, rangeOverride: Float?): Boolean {
        val closestHit = closestHitRange(target, params)
        if (closestHit > (rangeOverride ?: weapon.totalRange)) return false

        val interceptArc = interceptArc(target, params)
        return Arc(weapon.arc, weapon.absoluteArcFacing).overlaps(interceptArc)
    }

    /** Calculates the target intercept arc. If weapon facing is within this arc,
     * the weapon projectile will collide with the target circumference.
     * Similar to intercept point, but not restricted to target center point.
     * For simplicity, the barrel offset is omitted. */
    override fun interceptArc(target: Target, params: BallisticParams): Arc {
        val (p, _) = targetCoords(target, params)
        val points = pointsOfTangency(p, target.radius) ?: return Arc(360f, 0f)

        val target1 = Target(weapon.location + points.first, target.velocity, 0f)
        val target2 = Target(weapon.location + points.second, target.velocity, 0f)

        // Attack delay was accounted for when calculating points of tangency.
        val noDelayParams = BallisticParams(params.accuracy, 0f)

        return Arc.fromTo(
            intercept(target1, noDelayParams).facing,
            intercept(target2, noDelayParams).facing,
        )
    }

    /** Calculates if projectile will collide with the target circumference,
     * given current weapon facing. Weapon range is ignored. */
    override fun willHitCircumference(target: Target, params: BallisticParams): Float? {
        return solve(projectileCoords(target, params), target.radius)
    }

    /** Calculates if a perfectly accurate projectile will collide with target shield,
     * given current weapon facing. Will not detect hits to inside of shield.
     * Collision range is returned, null if no collision. */
    override fun willHitShield(target: ShipAPI, params: BallisticParams): Float? {
        val shield = target.shield ?: return null
        if (shield.isOff) return null

        val (p, v) = projectileCoords(Target.shield(target), params)
        val range = solve(Pair(p, v), shield.radius) ?: return null
        val hitPoint = p + v * range

        return if (Arc(shield.activeArc, shield.facing).contains(hitPoint)) range else null
    }

    /** Calculates if a perfectly accurate projectile will collide with target bounds,
     * given current weapon facing. Collision range is returned, null if no collision. */
    override fun willHitBounds(target: ShipAPI, params: BallisticParams): Float? {
        val (p, v) = projectileCoords(Target.entity(target), params)
        return state.bounds.collision(p, v, target)
    }

    /** Target location and velocity in weapon frame of reference. */
    private fun targetCoords(target: Target, params: BallisticParams): Pair<Vector2f, Vector2f> {
        val vAbs = (target.velocity - weapon.ship.velocity)
        val pAbs = (target.location - weapon.location)

        val p = pAbs + vAbs * (params.delay)
        val v = vAbs / (weapon.trueProjectileSpeed * params.accuracy)

        return Pair(p, v)
    }

    /** Projectile location and velocity in target frame of reference. */
    private fun projectileCoords(target: Target, params: BallisticParams): Pair<Vector2f, Vector2f> {
        val vAbs = (weapon.ship.velocity - target.velocity)
        val pAbs = (weapon.location - target.location)
        val vProj = unitVector(weapon.currAngle)

        val p = pAbs + vAbs * params.delay + vProj * weapon.barrelOffset
        val v = vProj + vAbs / (weapon.trueProjectileSpeed * params.accuracy)

        return Pair(p, v)
    }

    /** True if target collision radius is above weapon barrel radius.  */
    private fun targetAboveWeapon(locationRelative: Vector2f, weapon: WeaponAPI, target: Target): Boolean {
        val d2 = locationRelative.lengthSquared
        val r = weapon.barrelOffset + target.radius
        return d2 < r * r
    }
}
