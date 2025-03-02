package com.genir.aitweaks.core.extensions

import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RADIANS_TO_DEGREES
import com.genir.aitweaks.core.utils.atan2
import com.genir.aitweaks.core.utils.sqrt
import org.lwjgl.util.vector.Vector2f

operator fun Vector2f.plus(b: Vector2f): Vector2f {
    return Vector2f(x + b.x, y + b.y)
}

operator fun Vector2f.minus(b: Vector2f): Vector2f {
    return Vector2f(x - b.x, y - b.y)
}

operator fun Vector2f.unaryMinus(): Vector2f {
    return Vector2f(-x, -y)
}

operator fun Vector2f.times(b: Float): Vector2f {
    return Vector2f(x * b, y * b)
}

operator fun Vector2f.div(b: Float): Vector2f {
    return Vector2f(x / b, y / b)
}

fun Vector2f.resized(length: Float): Vector2f {
    return if (isZero) Vector2f()
    else this * length / this.length
}

val Vector2f.copy: Vector2f
    get() = Vector2f(x, y)

val Vector2f.facing: Direction
    get() = (if (isZero) 0f else atan2(y, x) * RADIANS_TO_DEGREES).direction

val Vector2f.length: Float
    get() = sqrt(lengthSquared)

val Vector2f.lengthSquared: Float
    get() = x * x + y * y

val Vector2f.isZero: Boolean
    get() = x == 0f && y == 0f

val Vector2f.isNonZero: Boolean
    get() = x != 0f || y != 0f
