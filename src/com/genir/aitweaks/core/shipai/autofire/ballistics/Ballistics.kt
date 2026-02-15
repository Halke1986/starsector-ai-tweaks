package com.genir.aitweaks.core.shipai.autofire.ballistics

import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.LinearMotion
import org.lwjgl.util.vector.Vector2f

/**
 * All functions in this interface take into account target velocity
 * when calculating hit location. Target acceleration is ignored.
 *
 * All functions in this interface assume a perfectly accurate weapon,
 * shooting projectiles with zero collision radius.
 *
 * All functions in this interface return the result in weapon frame
 * of reference, unless stated otherwise.
 */

interface Ballistics {
    /** Weapon aim location required to hit center point of a moving target.
     * When the target's speed approaches the speed of the projectile, the intercept
     * time and location approach infinity. In such cases, the function assumes an
     * arbitrary long time period to approximate the target location. */
    fun intercept(target: BallisticTarget, params: BallisticParams): Vector2f

    /** Calculates the target intercept arc. If weapon facing is within this arc,
     * the weapon projectile will collide with the target circumference.
     * Similar to intercept point, but not restricted to target center point.
     * For simplicity, the barrel offset is omitted. */
    fun interceptArc(target: BallisticTarget, params: BallisticParams): Arc

    /** Closest possible range at which the projectile fired by the weapon can collide
     * with the target circumference, for any weapon facing. */
    fun closestHitRange(target: BallisticTarget, params: BallisticParams): Float

    /** Does the weapon have sufficient range and can rotate in its slot to aim at the target. */
    fun canTrack(target: BallisticTarget, params: BallisticParams, rangeOverride: Float? = null): Boolean

    /** Closest possible range at which the projectile fired by the weapon can collide
     * with the target circumference, for any weapon facing.
     *
     * Returns hitPoint in target frame of reference and projectile flight range. */
    fun closestHitInTargetFoR(target: BallisticTarget, params: BallisticParams): Pair<Vector2f, Float>

    /** Predicted projectile location and velocity in target frame of reference.
     * Assumes a perfectly accurate weapon.
     * weapon.projectileSpeed is used as the unit of velocity.*/
    fun projectileMotionInTargetFoR(target: LinearMotion, params: BallisticParams): LinearMotion
}