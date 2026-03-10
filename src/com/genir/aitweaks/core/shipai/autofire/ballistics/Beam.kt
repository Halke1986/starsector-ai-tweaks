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
        return targetLocation(target, params)
    }

    /** Calculates the target intercept arc. If weapon facing is within this arc,
     * the weapon beam will collide with the target circumference.
     * Similar to intercept point, but not restricted to target center point. */
    override fun interceptArc(target: BallisticTarget, params: BallisticParams): Arc {
        val toTarget: Vector2f = targetLocation(target, params)
        return Arc(
            angle = angularSize(toTarget.lengthSquared, target.radius),
            facing = toTarget.facing
        )
    }

    /** Closest possible range at which the beam fired by the weapon can collide
     * with the target circumference, for any weapon facing. */
    override fun closestHitRange(target: BallisticTarget, params: BallisticParams): Float {
        val toTarget: Vector2f = targetLocation(target, params)
        val projectileOffset: Float = weapon.projectileSpawnOffset
        val range: Float = toTarget.length - (target.radius + projectileOffset)

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
        val toTarget: Vector2f = targetLocation(target, params)
        val range: Float = closestHitRange(target, params)
        val hitPoint = -toTarget.resized(target.radius)
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
            velocity = vProj // For beams, it is safe to ignore the target velocity.
        )
    }

    /** Target location weapon frame of reference. */
    private fun targetLocation(target: BallisticTarget, params: BallisticParams): Vector2f {
        val pAbs = target.location - weapon.location
        val vAbs = target.velocity - weapon.ship.velocity
        return pAbs + vAbs * params.delay
    }
}