package com.genir.aitweaks.core.utils.types

import com.genir.aitweaks.core.extensions.minus
import com.genir.aitweaks.core.extensions.plus
import com.genir.aitweaks.core.extensions.times
import com.genir.aitweaks.core.utils.crossProductZ
import org.lwjgl.util.vector.Vector2f

data class LinearMotion(
    val position: Vector2f,
    val velocity: Vector2f,
) {
    fun positionAfter(time: Float): Vector2f {
        return position + velocity * time
    }

    operator fun minus(other: LinearMotion): LinearMotion {
        return LinearMotion(
            position = position - other.position,
            velocity = velocity - other.velocity,
        )
    }

    companion object {
        /**
         * Find an intersection between two linear motions.
         * If intersection exists, returns (K,T) pair, such that
         *
         * a.position + K * a.velocity = b.position + T * b.velocity = point of intersection
         */
        fun intersection(a: LinearMotion, b: LinearMotion): Pair<Float, Float>? {
            val m = crossProductZ(a.velocity, b.velocity)
            if (m == 0f) {
                return null
            }

            val offset = a.position - b.position
            val t = crossProductZ(a.velocity, offset) / m
            val k = crossProductZ(b.velocity, offset) / m

            return Pair(k, t)
        }
    }
}
