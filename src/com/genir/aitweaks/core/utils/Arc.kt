package com.genir.aitweaks.core.utils

import com.genir.aitweaks.core.extensions.facing
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

class Arc(angle: Float, facing: Float) {
    val angle = min(360f, abs(angle))
    val facing = clampAngle(facing)

    fun overlaps(second: Arc): Boolean {
        val offset = absShortestRotation(this.facing, second.facing)
        return offset <= this.half + second.half
    }

    fun contains(facing: Float): Boolean {
        return absShortestRotation(facing, this.facing) <= half
    }

    fun contains(v: Vector2f): Boolean {
        return contains(v.facing)
    }

    fun extendedBy(degrees: Float): Arc {
        return Arc((angle + degrees).coerceIn(0f, 360f), facing)
    }

    val half: Float
        get() = angle * 0.5f

    companion object {
        /** Merges a list of arcs into a single continuous arc.
         * Assumes that the list forms a single, unbroken arc.
         * If this assumption is violated, the result is undefined. */
        fun mergeOverlapping(arcsInput: List<Arc>): List<Arc> {
            val toBeRemoved = Arc(0f, 0f)
            val arcs: MutableList<Arc> = arcsInput.toMutableList()

            for (i in 0 until arcs.size) {
                for (k in i + 1 until arcs.size) {
                    if (arcs[k].overlaps(arcs[i])) {
                        arcs[k] = union(arcs[k], arcs[i])
                        arcs[i] = toBeRemoved
                        break
                    }
                }
            }

            return arcs.filter { it !== toBeRemoved }
        }

        /** Merge two arcs. If the arcs do not overlap, returns
         * the smallest arc that contains both provided arcs. */
        fun union(a: Arc, b: Arc): Arc {
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
                    val facing = a.facing + (offset + sgn * b.half - sgn * a.half) / 2
                    Arc(angle, facing)
                }
            }
        }

        /** Intersect two arcs. If the arcs do not overlap, returns null. */
        fun intersection(a: Arc, b: Arc): Arc? {
            val offset = shortestRotation(a.facing, b.facing)
            val angle = abs(offset) + a.half + b.half

            return when {
                // Arcs do not overlap.
                offset >= a.half + b.half -> return null

                // Both arcs form a complete angle.
                angle >= 360f -> return Arc(360f, a.facing)

                // A is contained in B.
                abs(offset) + a.half <= b.half -> return a

                // B is contained in A.
                abs(offset) + b.half <= a.half -> return b

                else -> {
                    val angles = listOf(a.facing + a.half, a.facing - a.half, b.facing + b.half, b.facing - b.half)
                    val sorted = angles.sortedWith(compareBy { it })
                    fromTo(sorted[1], sorted[2])
                }
            }
        }

        /** Make an arc spanning the shortest rotation
         * between 'from' and 'to' angles. */
        fun fromTo(from: Float, to: Float): Arc {
            val angle = shortestRotation(from, to)
            val facing = from + angle / 2

            return Arc(
                angle = angle,
                facing = facing
            )
        }
    }
}
