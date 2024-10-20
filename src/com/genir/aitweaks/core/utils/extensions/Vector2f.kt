package com.genir.aitweaks.core.utils.extensions

import com.genir.aitweaks.core.utils.RADIANS_TO_DEGREES
import com.genir.aitweaks.core.utils.atan2
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.isZeroVector
import org.lwjgl.util.vector.Vector2f

fun Vector2f.resized(length: Float): Vector2f {
    if (isZeroVector()) return Vector2f()
    val scale = length / this.length
    return Vector2f(x * scale, y * scale)
}

val Vector2f.copy: Vector2f
    get() = Vector2f(x, y)

operator fun Vector2f.unaryMinus() = Vector2f(-x, -y)

fun Vector2f.addLength(toAdd: Float): Vector2f {
    val l = length
    val s = (l + toAdd) / l
    return Vector2f(x * s, y * s)
}

val Vector2f.facing: Float
    get() {
        if (this.isZeroVector()) return 0f

        return MathUtils.clampAngle(atan2(this.y, this.x) * RADIANS_TO_DEGREES)
    }

val Vector2f.length: Float
    get() = length()

val Vector2f.lengthSquared: Float
    get() = lengthSquared()