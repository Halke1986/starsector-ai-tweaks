package com.genir.aitweaks.core.shipai.autofire.ballistics

import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.utils.angularSize
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.LinearMotion
import org.lwjgl.util.vector.Vector2f

class Beam(private val weapon: WeaponHandle) : Ballistics {
    /** Weapon aim location required to hit center point of a moving target. */
    override fun intercept(target: BallisticTarget, params: BallisticParams): Vector2f {
        return target.location - weapon.location
    }

    /** Calculates the target intercept arc. If weapon facing is within this arc,
     * the weapon beam will collide with the target circumference.
     * Similar to intercept point, but not restricted to target center point. */
    override fun interceptArc(target: BallisticTarget, params: BallisticParams): Arc {
        val toTarget: Vector2f = target.location - weapon.location
        return Arc(
            angle = angularSize(toTarget.lengthSquared, target.radius),
            facing = toTarget.facing
        )
    }

    /** Closest possible range at which the beam fired by the weapon can collide
     * with the target circumference, for any weapon facing. */
    override fun closestHitRange(target: BallisticTarget, params: BallisticParams): Float {
        val dist: Float = (target.location - weapon.location).length
        val projectileOffset: Float = weapon.projectileSpawnOffset
        val range: Float = dist - (target.radius + projectileOffset)

        return range.coerceAtLeast(0f)
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

    /** Closest possible range at which the beam fired by the weapon can collide
     * with the target circumference, for any weapon facing.
     *
     * Returns hitPoint in target frame of reference and beam flight range. */
    override fun closestHitInTargetFoR(target: BallisticTarget, params: BallisticParams): Pair<Vector2f, Float> {
        val range: Float = closestHitRange(target, params)
        val hitPoint = (weapon.location - target.location).resized(target.radius)
        return Pair(hitPoint, range)
    }

    /** Predicted projectile location and velocity in target frame of reference.
     * Assumes a perfectly accurate weapon.
     * weapon.projectileSpeed is used as the unit of velocity.*/
    override fun projectileMotionInTargetFoR(target: LinearMotion, params: BallisticParams): LinearMotion {
        val pAbs = weapon.location - target.position
        val vProj = weapon.facingWhenFiringThisFrame.unitVector

        return LinearMotion(
            position = pAbs + vProj * weapon.projectileSpawnOffset,
            velocity = vProj // For beams, it is safe to ignore the target velocity.
        )
    }
}