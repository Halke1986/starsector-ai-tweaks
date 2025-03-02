package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.extensions.lengthSquared
import com.genir.aitweaks.core.extensions.minus
import com.genir.aitweaks.core.extensions.plus
import com.genir.aitweaks.core.extensions.times
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotatedReverse
import org.lwjgl.util.vector.Vector2f
import kotlin.Float.Companion.MAX_VALUE
import kotlin.math.max
import kotlin.math.min

object Bounds {
    /** Calculates the length ratio along vector with given starting position
     * and velocity, at which first collision with target bounds happens.
     * The vector position and direction are in target frame of reference!. */
    fun collision(position: Vector2f, velocity: Vector2f, target: CombatEntityAPI): Float? {
        // Check if there's a possibility of collision.
        val bounds = target.exactBounds as? Obfuscated.Bounds ?: return null

        // Rotate vector coordinates from target frame of
        // reference to target bounds frame of reference.
        val r = (-target.facing.direction).rotationMatrix
        val p = position.rotated(r)
        val v = velocity.rotated(r)

        // Equation for the collision point: [s.p1 + k (s.p2 - s.p1) = p + t v]
        var closest = MAX_VALUE
        bounds.origSegments.forEach { segment ->
            val q = segment.p1 - p
            val d = segment.p2 - segment.p1

            val m = crossProductZ(d, v)
            val k = crossProductZ(v, q) / m
            if (k < 0 || k > 1) {
                return@forEach // no collision
            }

            val t = crossProductZ(d, q) / m
            if (t >= 0) {
                closest = min(t, closest)
            }
        }

        return if (closest != MAX_VALUE) closest else null
    }

    /** Point on ship bounds closest to 'position'.
     * 'position' and result are in global frame of reference. */
    fun closestPoint(position: Vector2f, target: CombatEntityAPI): Vector2f {
        val bounds = target.exactBounds as? Obfuscated.Bounds ?: return target.location

        val r = (-target.facing.direction).rotationMatrix
        val o = (position - target.location).rotated(r)

        val points = bounds.origSegments.asSequence().map { segment ->
            val p = segment.p1 - o
            val v = segment.p2 - segment.p1
            val t = timeToOrigin(p, v)

            p + v * t.coerceIn(0f, 1f)
        }

        val closest = points.minWithOrNull(compareBy { it.lengthSquared }) ?: target.location
        return (o + closest).rotatedReverse(r) + target.location
    }

    /** Is the point 'position' inside ship bounds.
     * 'position' is in global frame of reference. */
    fun isPointWithin(position: Vector2f, target: CombatEntityAPI): Boolean {
        // Check if there's a possibility of a collision.
        val bounds = target.exactBounds as? Obfuscated.Bounds ?: return false

        val r = (-target.facing.direction).rotationMatrix
        val p = (position - target.location).rotated(r)

        // Count the number of segments below the point p.
        val count = bounds.origSegments.count { segment ->
            val x1 = min(segment.p1.x, segment.p2.x)
            val x2 = max(segment.p1.x, segment.p2.x)
            if (p.x < x1 || p.x >= x2) {
                return@count false
            }

            val q = p - segment.p1
            val d = segment.p2 - segment.p1
            q.x * d.y / d.x <= q.y
        }

        return count and 1 == 1
    }

    /** Radius of a circle encompassing the entity bounds. */
    fun radius(entity: CombatEntityAPI): Float {
        // Use obfuscated Bounds implementation, because it allows
        // to access Segments list without copying it.
        val bounds = entity.exactBounds as? Obfuscated.Bounds ?: return entity.collisionRadius

        var radius = 0f
        bounds.origSegments.forEach { segment: Obfuscated.BoundsSegment ->
            radius = max(radius, segment.x1 * segment.x1 + segment.y1 + segment.y1)
            radius = max(radius, segment.x2 * segment.x2 + segment.y2 + segment.y2)
        }

        return sqrt(radius)
    }
}
