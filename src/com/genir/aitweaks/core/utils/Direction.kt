package com.genir.aitweaks.core.utils

import org.lwjgl.util.vector.Vector2f
import kotlin.math.*

/** Represents a directional angle in degrees, normalized into the range [-180, 180]. */
class Direction(degrees: Float) {
    val degrees: Float = degrees - round(degrees / 360) * 360

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

    override fun toString(): String {
        return degrees.toString()
    }

    operator fun plus(other: Direction): Direction {
        return Direction(degrees + other.degrees)
    }

    operator fun plus(other: Float): Direction {
        return Direction(degrees + other)
    }

    operator fun minus(other: Direction): Direction {
        return Direction(degrees - other.degrees)
    }

    operator fun minus(other: Float): Direction {
        return Direction(degrees - other)
    }

    operator fun div(f: Float): Direction {
        return Direction(degrees / f)
    }

    operator fun times(f: Float): Direction {
        return Direction(degrees * f)
    }

    operator fun unaryMinus(): Direction {
        return Direction(-degrees)
    }

    companion object {
        val Float.direction: Direction
            get() = Direction(this)
    }
}
