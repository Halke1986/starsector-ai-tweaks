package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.utils.Rotation.Companion.rotated
import com.genir.aitweaks.core.utils.Rotation.Companion.rotatedReverse
import com.genir.aitweaks.core.utils.extensions.boundsRadius
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.lengthSquared
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

object Bounds {
    /** Calculates the length ratio along vector with given starting position
     * and direction, at which first collision with target bounds happens.
     * The vector position and direction are in target frame of reference!. */
    fun collision(position: Vector2f, direction: Vector2f, target: ShipAPI): Float? {
        val bounds = target.exactBounds ?: return null

        if (target.boundsRadius < (distanceToOrigin(position, direction) ?: position.length)) return null

        // Rotate vector coordinates from target frame of reference
        // to target bounds frame of reference. That way the target
        // bounds don't need to be transformed.
        val r = Rotation(-target.facing)
        val q1 = position.rotated(r)
        val vr = direction.rotated(r)
        val q2 = q1 + vr

        var closest: Float? = null
        bounds.origSegments.forEach { segment ->
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
            if (k < 0f || k > 1f) return@forEach // no collision

            val t = (pqy * dp.x + qpx * dp.y) / d
            if (t > 0 && (closest == null || t < closest!!)) closest = t
        }

        return closest
    }

    /** Point on ship bounds closest to 'position'.
     * 'position' and result are in global frame of reference. */
    fun closestPoint(position: Vector2f, target: ShipAPI): Vector2f {
        val bounds = target.exactBounds ?: return target.location

        val r = Rotation(-target.facing)
        val o = (position - target.location).rotated(r)

        val closest = bounds.origSegments.asSequence().map { segment ->
            val p = segment.p1 - o
            val v = segment.p2 - segment.p1
            val t = timeToOrigin(p, v)

            when {
                t <= 0 -> p
                t >= 1 -> p + v
                else -> p + v * t
            }
        }.minWithOrNull(compareBy { it.lengthSquared }) ?: target.location

        return (o + closest).rotatedReverse(r) + target.location
    }
}
