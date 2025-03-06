package com.genir.aitweaks.core.utils

import com.genir.aitweaks.core.extensions.minus
import com.genir.aitweaks.core.extensions.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.cos
import kotlin.math.sin

/** 2D rotation matrix. */
data class RotationMatrix(val sin: Float, val cos: Float) {

    constructor(angle: Float) : this(
        sin(angle * DEGREES_TO_RADIANS),
        cos(angle * DEGREES_TO_RADIANS),
    )

    companion object {
        fun Vector2f.rotated(r: RotationMatrix): Vector2f {
            return Vector2f(x * r.cos - y * r.sin, x * r.sin + y * r.cos)
        }

        fun Vector2f.rotatedX(r: RotationMatrix): Float {
            return x * r.cos - y * r.sin
        }

        fun Vector2f.rotatedAroundPivot(r: RotationMatrix, p: Vector2f): Vector2f {
            return (this - p).rotated(r) + p
        }

        fun Vector2f.rotatedReverse(r: RotationMatrix): Vector2f {
            return Vector2f(x * r.cos + y * r.sin, -x * r.sin + y * r.cos)
        }
    }
}
