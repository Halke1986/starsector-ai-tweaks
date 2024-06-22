package com.genir.aitweaks.core.utils.extensions

import com.genir.aitweaks.core.utils.Rotation
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

fun Vector2f.resized(length: Float): Vector2f = VectorUtils.resize(this, length, Vector2f())

fun Vector2f.rotated(r: Rotation): Vector2f = r.rotate(this)

fun Vector2f.rotatedAroundPivot(r: Rotation, p: Vector2f): Vector2f = r.rotate(this - p) + p

fun Vector2f.rotatedReverse(r: Rotation): Vector2f = r.reverse(this)

val Vector2f.copy: Vector2f
    get() = Vector2f(x, y)

operator fun Vector2f.unaryMinus() = Vector2f(-x, -y)
