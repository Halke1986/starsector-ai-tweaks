package com.genir.aitweaks.core.utils

import org.lwjgl.util.vector.Vector2f
import kotlin.math.*

// TODO rename to Direction
// TODO remove API extensions

class Rotation(degrees: Float) {
    val degrees: Float = degrees - round(degrees / 360) * 360

    val radians: Float
        get() = degrees * DEGREES_TO_RADIANS

    /** Length of the rotation, in range [0;180] */
    val length: Float
        get() = abs(degrees)

    val isZero: Boolean
        get() = degrees == 0f

    // TODO remove?
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

    operator fun plus(other: Rotation): Rotation {
        return Rotation(degrees + other.degrees)
    }

    operator fun plus(other: Float): Rotation {
        return Rotation(degrees + other)
    }

    operator fun minus(other: Rotation): Rotation {
        return Rotation(degrees - other.degrees)
    }

    operator fun minus(other: Float): Rotation {
        return Rotation(degrees - other)
    }

    operator fun div(f: Float): Rotation {
        return Rotation(degrees / f)
    }

    operator fun times(f: Float): Rotation {
        return Rotation(degrees * f)
    }

    operator fun unaryMinus(): Rotation {
        return Rotation(-degrees)
    }
}
