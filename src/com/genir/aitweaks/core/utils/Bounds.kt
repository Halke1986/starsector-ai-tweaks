package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.utils.Rotation.Companion.rotated
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

/** Calculates the length ratio along vector with given starting position
 * and direction, at which first collision with target bounds happens.
 * The vector position and direction are in target frame of reference!. */
fun boundsCollision(position: Vector2f, direction: Vector2f, target: ShipAPI): Float? {
    val bounds = target.exactBounds ?: return null

    // Rotate vector coordinates from target frame of reference
    // to target bounds frame of reference. That way the target
    // bounds don't need to be transformed.
    val r = Rotation(-target.facing)
    val q1 = position.rotated(r)
    val vr = direction.rotated(r)
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
