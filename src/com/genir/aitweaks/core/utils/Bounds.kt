package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.length
import com.genir.aitweaks.core.extensions.lengthSquared
import com.genir.aitweaks.core.extensions.minus
import com.genir.aitweaks.core.extensions.plus
import com.genir.aitweaks.core.utils.Rotation.Companion.rotated
import com.genir.aitweaks.core.utils.Rotation.Companion.rotatedReverse
import org.lwjgl.util.vector.Vector2f
import kotlin.Float.Companion.MAX_VALUE
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class Bounds {
    private val radiusCache = mutableMapOf<ShipAPI, Pair<Float, Float>>()

    /** Calculates the length ratio along vector with given starting position
     * and velocity, at which first collision with target bounds happens.
     * The vector position and direction are in target frame of reference!. */
    fun collision(position: Vector2f, velocity: Vector2f, target: ShipAPI): Float? {
        // Check if there's a possibility of collision.
        val bounds = target.exactBounds ?: return null
        if (radius(target) < distanceToOrigin(position, velocity)) return null

        // Rotate vector coordinates from target frame of
        // reference to target bounds frame of reference.
        val r = Rotation(-target.facing)
        val p = position.rotated(r)
        val v = velocity.rotated(r)

        // Equation for the collision point: [s.p1 + k (s.p2 - s.p1) = p + t v]
        var closest = MAX_VALUE
        bounds.origSegments.forEach { segment ->
            val q = segment.p1 - p
            val d = segment.p2 - segment.p1

            val m = crossProductZ(d, v)
            val k = crossProductZ(v, q) / m
            if (k < 0 || k > 1) return@forEach // no collision

            val t = crossProductZ(d, q) / m
            if (t >= 0) closest = min(t, closest)
        }

        return if (closest != MAX_VALUE) closest else null
    }

    /** Point on ship bounds closest to 'position'.
     * 'position' and result are in global frame of reference. */
    fun closestPoint(position: Vector2f, target: ShipAPI): Vector2f {
        val bounds = target.exactBounds ?: return target.location

        val r = Rotation(-target.facing)
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
    fun isPointWithin(position: Vector2f, target: ShipAPI): Boolean {
        // Check if there's a possibility of collision.
        val bounds = target.exactBounds ?: return false
        if (radius(target) < (position - target.location).length) return false

        val r = Rotation(-target.facing)
        val p = (position - target.location).rotated(r)

        // Count the number of segments below the point p.
        val count = bounds.origSegments.count { segment ->
            val x1 = min(segment.p1.x, segment.p2.x)
            val x2 = max(segment.p1.x, segment.p2.x)
            if (p.x < x1 || p.x >= x2) return@count false

            val q = p - segment.p1
            val d = segment.p2 - segment.p1
            q.x * d.y / d.x <= q.y
        }

        return count and 1 == 1
    }

    /** Radius of a circle encompassing the ship bounds. */
    fun radius(ship: ShipAPI): Float {
        val now = Global.getCombatEngine().getTotalElapsedTime(false)

        radiusCache[ship]?.let { (radius, timestamp) ->
            if (now - timestamp < 1f) return radius
        }

        val bounds = ship.exactBounds ?: return 0f
        val points = bounds.origSegments.flatMap { listOf(it.p1, it.p2) }
        val radius = points.maxOfOrNull { it.lengthSquared }?.let { sqrt(it) } ?: 0f

        radiusCache[ship] = Pair(radius, now)

        return radius
    }
}
