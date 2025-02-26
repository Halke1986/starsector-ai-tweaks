package com.genir.aitweaks.core.utils

import com.genir.aitweaks.core.extensions.facing
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.min

class Arc(angle: Float, val facing: Direction) {
    val angle = min(360f, abs(angle))

    val half: Float
        get() = angle * 0.5f

    val arms: Pair<Direction, Direction>
        get() = Pair(facing - half, facing + half)

    fun rotated(rotation: Float): Arc {
        return Arc(angle, facing + rotation)
    }

    fun extendedBy(degrees: Float): Arc {
        return Arc((angle + degrees).coerceIn(0f, 360f), facing)
    }

    fun overlaps(second: Arc, tolerance: Float = 0f): Boolean {
        val offset = (this.facing - second.facing).length
        return offset <= half + second.half + tolerance
    }

    fun contains(facing: Direction, tolerance: Float = 0f): Boolean {
        return (facing - this.facing).length <= half + tolerance
    }

    fun contains(v: Vector2f): Boolean {
        return contains(v.facing)
    }

    /** Distance from arc to direction.
     * 0f if arc contains the direction. */
    fun distanceTo(d: Direction): Direction {
        if (contains(d)) {
            return 0f.direction
        }

        val arms = arms

        val dist1 = d - arms.first
        val dist2 = d - arms.second

        return if (dist1.length < dist2.length) dist1 else dist2
    }

    override fun toString(): String {
        return "Arc[$angle, $facing]"
    }

    companion object {
        /** Merges a list of arcs into a single continuous arc.
         * Assumes that the list forms a single, unbroken arc.
         * If this assumption is violated, the result is undefined. */
        fun mergeOverlapping(arcsInput: List<Arc>): List<Arc> {
            val toBeRemoved = Arc(0f, 0f.direction)
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
            val offset = b.facing - a.facing
            val angle = offset.length + a.half + b.half

            return when {
                // Both arcs form a complete angle.
                angle >= 360f -> return Arc(360f, a.facing)

                // A is contained in B.
                offset.length + a.half <= b.half -> return b

                // B is contained in A.
                offset.length + b.half <= a.half -> return a

                else -> {
                    val sgn = offset.sign
                    val facing = a.facing + (offset + sgn * b.half - sgn * a.half) / 2f
                    Arc(angle, facing)
                }
            }
        }

        /** Make an arc spanning the shortest rotation
         * between 'from' and 'to' angles. */
        fun fromTo(from: Direction, to: Direction): Arc {
            val angle = to - from
            val facing = from + angle / 2f

            return Arc(
                angle = angle.length,
                facing = facing
            )
        }
    }
}
