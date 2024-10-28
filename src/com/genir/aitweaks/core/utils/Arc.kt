package com.genir.aitweaks.core.utils

import com.genir.aitweaks.core.utils.extensions.facing
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.sign

data class Arc(val angle: Float, val facing: Float) {
    fun overlaps(second: Arc): Boolean {
        val offset = abs(shortestRotation(this.facing, second.facing))
        return offset <= this.half + second.half
    }

    fun contains(facing: Float): Boolean {
        return abs(shortestRotation(facing, this.facing)) <= half
    }

    fun contains(v: Vector2f): Boolean {
        return contains(v.facing)
    }

    val half: Float
        get() = angle * 0.5f

    companion object {
        fun mergeOverlapping(arcsInput: List<Arc>): List<Arc> {
            val toBeRemoved = Arc(0f, 0f)
            val arcs: MutableList<Arc> = arcsInput.toMutableList()

            for (i in 0 until arcs.size) {
                for (k in i + 1 until arcs.size) {
                    if (arcs[k].overlaps(arcs[i])) {
                        arcs[k] = merge(arcs[k], arcs[i])
                        arcs[i] = toBeRemoved
                        break
                    }
                }
            }

            return arcs.filter { it !== toBeRemoved }
        }

        fun merge(a: Arc, b: Arc): Arc {
            val offset = shortestRotation(a.facing, b.facing)
            val angle = abs(offset) + a.half + b.half

            return when {
                // Both arcs form a complete angle.
                angle >= 360f -> return Arc(360f, a.facing)

                // A is contained in B.
                abs(offset) + a.half <= b.half -> return b

                // B is contained in A.
                abs(offset) + b.half <= a.half -> return a

                else -> {
                    val sgn = offset.sign
                    val facing = a.facing + (offset + sgn * b.half - sgn * a.half) / 2f
                    Arc(angle, facing)
                }
            }
        }

        /** Make an arc spanning the shortest rotation
         * between 'from' and 'to' angles. */
        fun fromTo(from: Float, to: Float): Arc {
            val angle = shortestRotation(from, to)
            val facing = MathUtils.clampAngle(from + angle / 2f)

            return Arc(
                angle = abs(angle),
                facing = facing
            )
        }
    }
}
