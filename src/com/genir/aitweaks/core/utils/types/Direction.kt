package com.genir.aitweaks.core.utils.types

import com.genir.aitweaks.core.utils.DEGREES_TO_RADIANS
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*

/** Represents a directional angle in degrees, normalized into the range [-180, 180]. */
@JvmInline
value class Direction private constructor(val degrees: Float) {
    val radians: Float
        get() = degrees * DEGREES_TO_RADIANS

    /** Absolute value of the angle; range: [0, 180] */
    val length: Float
        get() = abs(degrees)

    val isZero: Boolean
        get() = degrees == 0f

    /** The sign (`-1.0`, `0.0`, or `1.0`) of the angle in degrees. */
    val sign: Float
        get() = degrees.sign

    val rotationMatrix: RotationMatrix
        get() = RotationMatrix(degrees)

    val unitVector: Vector2f
        get() {
            val radians = degrees * DEGREES_TO_RADIANS
            val x = cos(radians)
            val y = sin(radians)
            return Vector2f(x, y)
        }

    operator fun plus(other: Direction): Direction {
        return normalizeDirection(degrees + other.degrees)
    }

    operator fun plus(other: Float): Direction {
        return normalizeDirection(degrees + other)
    }

    operator fun minus(other: Direction): Direction {
        return normalizeDirection(degrees - other.degrees)
    }

    operator fun minus(other: Float): Direction {
        return normalizeDirection(degrees - other)
    }

    operator fun div(f: Float): Direction {
        return normalizeDirection(degrees / f)
    }

    operator fun times(f: Float): Direction {
        return normalizeDirection(degrees * f)
    }

    operator fun unaryMinus(): Direction {
        return normalizeDirection(-degrees)
    }

    override fun toString(): String {
        return degrees.toString()
    }

    companion object {
        private fun normalizeDirection(degrees: Float): Direction {
            return Direction(degrees - round(degrees / 360) * 360)
        }

        val Float.toDirection: Direction
            get() = normalizeDirection(this)
    }
}
