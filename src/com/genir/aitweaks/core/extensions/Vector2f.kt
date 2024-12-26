package com.genir.aitweaks.core.extensions

import com.genir.aitweaks.core.utils.RADIANS_TO_DEGREES
import com.genir.aitweaks.core.utils.atan2
import com.genir.aitweaks.core.utils.clampAngle
import org.lwjgl.util.vector.Vector2f
import kotlin.math.sqrt

operator fun Vector2f.plus(other: Vector2f): Vector2f = Vector2f(x + other.x, y + other.y)
operator fun Vector2f.minus(other: Vector2f): Vector2f = Vector2f(x - other.x, y - other.y)
operator fun Vector2f.unaryMinus() = Vector2f(-x, -y)

fun Vector2f.resized(length: Float): Vector2f {
    if (isZero) return Vector2f()
    val scale = length / this.length
    return Vector2f(x * scale, y * scale)
}

val Vector2f.copy: Vector2f
    get() = Vector2f(x, y)

fun Vector2f.addLength(toAdd: Float): Vector2f {
    val l = length
    val s = (l + toAdd) / l
    return Vector2f(x * s, y * s)
}

val Vector2f.facing: Float
    get() = if (isZero) 0f else clampAngle(atan2(y, x) * RADIANS_TO_DEGREES)

val Vector2f.length: Float
    get() = sqrt(lengthSquared)

val Vector2f.lengthSquared: Float
    get() = x * x + y * y

val Vector2f.isZero: Boolean
    get() = x == 0f && y == 0f

val Vector2f.isNonZero: Boolean
    get() = x != 0f || y != 0f
