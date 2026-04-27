package com.genir.aitweaks.core.shipai.autofire.ballistics

import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.utils.Solution
import com.genir.aitweaks.core.utils.pointsOfTangency
import com.genir.aitweaks.core.utils.solve
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection
import com.genir.aitweaks.core.utils.types.LinearMotion
import org.lwjgl.util.vector.Vector2f

private const val cos180 = -1f
private const val approachesInfinity = 1e7f

open class Projectile(private val weapon: WeaponHandle) : Ballistics {
    /** Weapon aim location required to hit center point of a moving target.
     * When the target's speed approaches the speed of the projectile, the intercept
     * time and location approach infinity. In such cases, the function assumes an
     * arbitrary long time period to approximate the target location. */
    override fun intercept(target: BallisticTarget, params: BallisticParams): Vector2f {
        val pv = targetMotion(target, params)
        val projectileFlightDistance = solve(pv, weapon.projectileSpawnOffset, 1f, 0f, 0f, Solution.SMALLER_NON_NEGATIVE).ifNaN(approachesInfinity)

        return pv.positionAfter(projectileFlightDistance)
    }

    /** Calculates the target intercept arc. If weapon facing is within this arc,
     * the weapon projectile will collide with the target circumference.
     * Similar to intercept point, but not restricted to target center point.
     * For simplicity, the barrel offset is omitted. */
    override fun interceptArc(target: BallisticTarget, params: BallisticParams): Arc {
        val pv = targetMotion(target, params)
        val points = pointsOfTangency(pv.position, target.radius)
            ?: return Arc(360f, 0f.toDirection)

        val target1 = BallisticTarget(weapon.location + points.first, target.velocity, 0f, target.entity)
        val target2 = BallisticTarget(weapon.location + points.second, target.velocity, 0f, target.entity)

        // Attack delay was accounted for when calculating points of tangency.
        val noDelayParams = BallisticParams(params.accuracy, 0f)

        return Arc.fromTo(
            intercept(target1, noDelayParams).facing,
            intercept(target2, noDelayParams).facing,
        )
    }

    /** Closest possible range at which the projectile fired by the weapon can collide
     * with the target circumference, for any weapon facing. */
    override fun closestHitRange(target: BallisticTarget, params: BallisticParams): Float {
        val pv = targetMotion(target, params)
        if (targetAboveWeapon(pv.position, weapon, target)) {
            return 0f
        }

        val projectileOffset = weapon.projectileSpawnOffset
        val projectileFlightDistance = solve(pv, projectileOffset, 1f, target.radius, cos180, Solution.SMALLER_NON_NEGATIVE)
        if (projectileFlightDistance.isNaN()) {
            return Float.POSITIVE_INFINITY
        }

        return projectileOffset + projectileFlightDistance
    }

    /** Does the weapon have sufficient range and can rotate in its slot to aim at the target. */
    override fun canEngage(target: BallisticTarget, params: BallisticParams, rangeOverride: Float?): Boolean {
        val closestHit = closestHitRange(target, params)
        if (closestHit > (rangeOverride ?: weapon.engagementRange)) {
            return false
        }

        val interceptArc = interceptArc(target, params)
        return weapon.absoluteArc.overlaps(interceptArc)
    }

    /** Closest possible range at which the projectile fired by the weapon can collide
     * with the target circumference, for any weapon facing.
     *
     * Returns hitPoint in target frame of reference and projectile flight range. */
    override fun closestHitInTargetFoR(target: BallisticTarget, params: BallisticParams): Pair<Vector2f, Float> {
        val pv = targetMotion(target, params)
        val range = closestHitRange(target, params)
        val hitPoint = -pv.positionAfter(range).resized(target.radius)
        return Pair(hitPoint, range)
    }

    /** Predicted projectile location and velocity in target frame of reference.
     * Assumes a perfectly accurate weapon.
     * weapon.projectileSpeed is used as the unit of velocity.*/
    override fun projectileMotionInTargetFoR(target: LinearMotion, params: BallisticParams): LinearMotion {
        val vAbs = weapon.ship.velocity - target.velocity
        val pAbs = weapon.location - target.position
        val vProj = weapon.facingWhenFiringThisFrame.unitVector

        return LinearMotion(
            position = pAbs + vAbs * params.delay + vProj * weapon.projectileSpawnOffset,
            velocity = vProj + vAbs / (weapon.projectileSpeed * params.accuracy)
        )
    }

    /** Target location and velocity in weapon frame of reference.
     * weapon.projectileSpeed is used as velocity unit.  */
    private fun targetMotion(target: BallisticTarget, params: BallisticParams): LinearMotion {
        val pAbs = target.location - weapon.location
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
}