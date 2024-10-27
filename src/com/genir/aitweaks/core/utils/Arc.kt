package com.genir.aitweaks.core.utils

import com.genir.aitweaks.core.utils.extensions.facing
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.sign

data class Arc(var arc: Float, var facing: Float) {
    fun overlaps(second: Arc): Boolean {
        return abs(shortestRotation(this.facing, second.facing)) <= (this.arc + second.arc) / 2f
    }

    /** Append the second arc, under the assumption both arcs are overlapping. */
    fun append(second: Arc) {
        val larger: Arc = if (this.arc > second.arc) this else second
        val smaller: Arc = if (this.arc > second.arc) second else this

        val offset: Float = shortestRotation(larger.facing, smaller.facing)
        val overhang: Float = abs(offset) + smaller.arc / 2f - larger.arc / 2f

        if (larger.arc + overhang >= 360f) {
            this.arc = 360f
        } else {
            this.arc = larger.arc + overhang
            this.facing = larger.facing + offset.sign * overhang / 2f
        }
    }

    fun contains(facing: Float): Boolean {
        return abs(shortestRotation(facing, this.facing)) <= arc / 2f
    }

    fun contains(v: Vector2f): Boolean {
        return contains(v.facing)
    }

    companion object {
        fun merge(arcsInput: List<Arc>): List<Arc> {
            val toBeRemoved = Arc(0f, 0f)
            val arcs: MutableList<Arc> = arcsInput.toMutableList()

            for (i in 0 until arcs.size) {
                for (k in i + 1 until arcs.size) {
                    if (arcs[k].overlaps(arcs[i])) {
                        arcs[k].append(arcs[i])
                        arcs[i] = toBeRemoved
                        break
                    }
                }
            }

            return arcs.filter { it !== toBeRemoved }
        }
    }
}

