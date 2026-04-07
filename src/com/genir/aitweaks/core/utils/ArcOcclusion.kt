package com.genir.aitweaks.core.utils

import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection

/**
 * Computes the visible portions of overlapping arc segments, keeping only the nearest segment at each angle.
 */
ArcOcclusion {
    fun calculateOcclusion(arcSegments: List<ArcSegment>): List<ArcSegment> {
        val edges: MutableList<Edge> = splitSegments(arcSegments)
        edges.sortWith(compareBy<Edge> { it.location }.thenBy { if (it.isBeginning) it.height else -it.height })

        var topSegment: Edge? = null
        var topSegmentBeginning: Float = 0f
        val stack: MutableList<Edge> = mutableListOf()
        val arcs: MutableList<ArcSegment> = mutableListOf()

        for (edge in edges) {
            if (edge.isBeginning) {
                stack.add(edge)

                if (topSegment == null || edge.height < topSegment.height) {
                    // New segment occluded the previous top one.
                    if (topSegment != null) {
                        arcs.add(makeArcSegment(topSegmentBeginning, edge.location, topSegment))
                    }

                    topSegment = edge
                    topSegmentBeginning = edge.location
                }
            } else {
                // Remove edge from the stack.
                for (i in 0 until stack.size) {
                    if (stack[i].id == edge.id) {
                        stack[i] = stack[stack.size - 1]
                        stack.removeLast()
                        break
                    }
                }

                // Find new top stack element.
                if (edge.id == topSegment?.id) {
                    arcs.add(makeArcSegment(topSegmentBeginning, edge.location, topSegment))

                    topSegment = null
                    for (stackElem in stack) {
                        if (topSegment == null || stackElem.height < topSegment.height) {
                            topSegment = stackElem
                            topSegmentBeginning = edge.location
                        }
                    }
                }
            }
        }

        return arcs
    }

    private fun splitSegments(arcSegments: List<ArcSegment>): MutableList<Edge> {
        val edges: MutableList<Edge> = mutableListOf()
        var edgeID: Int = 0

        for (arcSegment in arcSegments) {
            val arc: Arc = arcSegment.arc
            val begin: Float = arc.facing.degrees - arc.angle / 2f
            val end: Float = arc.facing.degrees + arc.angle / 2f

            // Split any arc that crosses the -180/180 boundary into two arcs within [-180, 180].
            if (begin < -180f) {
                edges.add(makeEdge(begin + 360f, true, arcSegment, edgeID))
                edges.add(makeEdge(-180f + 360f, false, arcSegment, edgeID++))

                edges.add(makeEdge(-180f, true, arcSegment, edgeID))
                edges.add(makeEdge(end, false, arcSegment, edgeID++))
            } else if (end > 180f) {
                edges.add(makeEdge(begin, true, arcSegment, edgeID))
                edges.add(makeEdge(180f, false, arcSegment, edgeID++))

                edges.add(makeEdge(180f - 360f, true, arcSegment, edgeID))
                edges.add(makeEdge(end - 360f, false, arcSegment, edgeID++))
            } else {
                edges.add(makeEdge(begin, true, arcSegment, edgeID))
                edges.add(makeEdge(end, false, arcSegment, edgeID++))
            }
        }

        return edges
    }

    private fun makeEdge(location: Float, isBeginning: Boolean, arcSegment: ArcSegment, id: Int): Edge {
        return Edge(
            location = location,
            isBeginning = isBeginning,
            height = arcSegment.dist,
            source = arcSegment.source,
            id = id,
        )
    }

    private fun makeArcSegment(begin: Float, end: Float, edge: Edge): ArcSegment {
        val angle: Float = end - begin
        return ArcSegment(
            arc = Arc(
                angle = angle,
                facing = (begin + angle / 2f).toDirection,
            ),
            dist = edge.height,
            source = edge.source,
        )
    }

    data class ArcSegment(val arc: Arc, val dist: Float, val source: Any)

    private data class Edge(val location: Float, val isBeginning: Boolean, val height: Float, val source: Any, val id: Int)
}
