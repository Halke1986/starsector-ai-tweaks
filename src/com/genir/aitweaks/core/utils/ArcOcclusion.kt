package com.genir.aitweaks.core.utils

import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection

/**
 * Computes the visible portions of overlapping arc segments, keeping only the nearest segment at each angle.
 */
object ArcOcclusion {
    fun calculateOcclusion(arcs: List<ArcData>): List<ArcData> {
        // Select bottom arc and use it as a pivot.
        var pivotArc: ArcData? = null
        for (arcData in arcs) {
            if (arcData.arc.angle == 0f) {
                continue
            }

            if (pivotArc == null || arcData.height < pivotArc.height) {
                pivotArc = arcData
            }
        }

        if (pivotArc == null) {
            return listOf()
        }

        val pivotFacing: Direction = pivotArc.arc.facing
        val stack: MutableList<ArcData> = mutableListOf()
        val edges: MutableList<Edge> = mutableListOf()

        // Split arcs into edges.
        for (arcData in arcs) {
            val arc: Arc = arcData.arc
            if (arc.angle == 0f) {
                continue
            }

            if (arc.contains(pivotFacing)) {
                stack.add(arcData)
            }

            val halfAngle: Direction = arc.halfAngle.toDirection
            var start: Float = (arc.facing - halfAngle).degrees
            var end: Float = (arc.facing + halfAngle).degrees

            if (start < pivotFacing.degrees) {
                start += 360f
            }

            if (end < pivotFacing.degrees) {
                end += 360f
            }

            edges.add(Edge(start, false, arcData))
            edges.add(Edge(end, true, arcData))
        }

        edges.sortWith(compareBy({ it.facing }, { it.isEnd }))

        val visibleArcs: MutableList<ArcData> = mutableListOf()
        var bottomArc: ArcData? = pivotArc
        var bottomArcStart: Float = pivotFacing.degrees - pivotArc.arc.halfAngle

        for (edge in edges) {
            if (edge.isEnd) {
                // Remove edge from the stack.
                for (i in 0 until stack.size) {
                    if (stack[i].source == edge.sourceArc.source) {
                        stack[i] = stack[stack.size - 1]
                        stack.removeLast()
                        break
                    }
                }

                // Find the new bottom stack arc.
                if (bottomArc?.source == edge.sourceArc.source) {
                    addVisibleArc(visibleArcs, bottomArcStart, edge.facing, bottomArc)

                    bottomArc = null
                    for (stackElem in stack) {
                        if (bottomArc == null || stackElem.height < bottomArc.height) {
                            bottomArc = stackElem
                            bottomArcStart = edge.facing
                        }
                    }
                }
            } else {
                stack.add(edge.sourceArc)

                if (bottomArc == null || edge.sourceArc.height < bottomArc.height) {
                    // New segment occluded the previous bottom one.
                    if (bottomArc != null) {
                        addVisibleArc(visibleArcs, bottomArcStart, edge.facing, bottomArc)
                    }

                    bottomArc = edge.sourceArc
                    bottomArcStart = edge.facing
                }
            }
        }

        return visibleArcs
    }

    private fun addVisibleArc(visibleArcs: MutableList<ArcData>, start: Float, end: Float, source: ArcData) {
        val angle: Float = end - start
        if (angle == 0f) {
            return
        }

        visibleArcs.add(ArcData(
            arc = Arc(
                angle = angle,
                facing = (start + angle / 2f).toDirection,
            ),
            height = source.height,
            source = source.source,
        ))
    }

    data class ArcData(val arc: Arc, val height: Float, val source: Any)

    private data class Edge(val facing: Float, val isEnd: Boolean, val sourceArc: ArcData)
}
