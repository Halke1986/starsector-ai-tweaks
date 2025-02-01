package com.genir.aitweaks.core.utils

import org.lwjgl.util.vector.Vector2f
import kotlin.math.*

// TODO remove = 0f
class Rotation(degrees: Float = 0f) {
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

    companion object {
//        private fun shortestRotation(from: Float, to: Float): Float {
//            val dist = to - from
//            return dist - round(dist / 360f) * 360f
//        }

//        infix fun Rotation.to(other: Rotation): Rotation {
//            return Rotation(other.degrees - degrees)
//        }

//        infix fun Rotation.distTo(other: Rotation): Float {
//            return abs(shortestRotation(degrees, other.degrees))
//        }
    }
}
