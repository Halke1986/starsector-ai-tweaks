package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.features.autofire.AutofireAI
import com.genir.aitweaks.core.utils.Rotation
import com.genir.aitweaks.core.utils.extensions.isPD
import com.genir.aitweaks.core.utils.times
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

fun drawCircle(pos: Vector2f, r: Float, color: Color = Color.CYAN) {
    debugRenderer?.circles?.add(Renderer.Circle(pos, r, color))
}

fun drawLine(a: Vector2f, b: Vector2f, color: Color = Color.YELLOW) {
    debugRenderer?.lines?.add(Renderer.Line(a, b, color))
}

fun drawEngineLines(ship: ShipAPI) {
    drawAccelerationLines(ship)
    drawTurnLines(ship)
}

fun drawBounds(ship: ShipAPI) {
    val bounds = ship.exactBounds ?: return
    bounds.update(ship.location, ship.facing)

    bounds.segments.forEach {
        drawLine(it.p1, it.p2, Color.CYAN)
    }
}

fun drawAccelerationLines(ship: ShipAPI) {
    if (debugRenderer == null) return

    val r = Rotation(ship.facing - 90f)
    val engine = ship.engineController

    listOfNotNull(
        if (engine.isAccelerating) Vector2f(0f, 1f) else null,
        if (engine.isAcceleratingBackwards) Vector2f(0f, -1f) else null,
        if (engine.isStrafingLeft) Vector2f(-1f, 0f) else null,
        if (engine.isStrafingRight) Vector2f(1f, 0f) else null,
    ).forEach {
        drawLine(ship.location, ship.location + r.rotate(it * ship.collisionRadius * 1.2f), Color.BLUE)
    }
}

fun drawTurnLines(ship: ShipAPI) {
    if (debugRenderer == null) return

    val r = Rotation(ship.facing - 90f)
    val engine = ship.engineController

    listOfNotNull(
        if (engine.isTurningLeft) Vector2f(-0.75f, 0.20f) else null,
        if (engine.isTurningRight) Vector2f(0.75f, 0.20f) else null,
    ).forEach {
        drawLine(ship.location, ship.location + r.rotate(it * ship.collisionRadius * 1.2f), Color.CYAN)
    }
}

fun drawCollisionRadius(ship: ShipAPI) {
    if (debugRenderer == null) return

    drawCircle(ship.location, ship.collisionRadius)
}

fun drawWeaponLines(ship: ShipAPI) {
    if (debugRenderer == null) return

    val ais = ship.weaponGroupsCopy.flatMap { it.aiPlugins }.filter { it is AutofireAI && it.target != null }
    ais.forEach { drawLine(it.weapon.location, it.target!!, if (it.weapon.isPD) Color.YELLOW else Color.RED) }
}

fun drawBattleGroup(group: Set<ShipAPI>, color: Color = Color.YELLOW) {
    if (debugRenderer == null) return

    DrawBattleGroup().drawBattleGroup(group, color)
}

private class DrawBattleGroup {
    data class Edge(val src: Int, val dest: Int, val weight: Float)

    fun drawBattleGroup(group: Set<ShipAPI>, color: Color) {
        val ts = group.toTypedArray()
        val es: MutableList<Edge> = mutableListOf()

        for (i in ts.indices) {
            for (j in i + 1 until ts.size) {
                val w = (ts[i].location - ts[j].location).lengthSquared()
                es.add(Edge(i, j, w))
            }
        }

        kruskal(es, ts.size).forEach {
            drawLine(ts[it.src].location, ts[it.dest].location, color)
        }
    }

    fun kruskal(graph: List<Edge>, numVertices: Int): List<Edge> {
        val sortedEdges = graph.sortedBy { it.weight }
        val disjointSet = IntArray(numVertices) { -1 }
        val mst = mutableListOf<Edge>()

        fun find(parents: IntArray, i: Int): Int {
            if (parents[i] < 0) return i
            return find(parents, parents[i]).also { parents[i] = it }
        }

        fun union(parents: IntArray, i: Int, j: Int) {
            val root1 = find(parents, i)
            val root2 = find(parents, j)
            if (root1 != root2) {
                if (parents[root1] < parents[root2]) {
                    parents[root1] += parents[root2]
                    parents[root2] = root1
                } else {
                    parents[root2] += parents[root1]
                    parents[root1] = root2
                }
            }
        }

        for (edge in sortedEdges) {
            if (mst.size >= numVertices - 1) break
            if (find(disjointSet, edge.src) != find(disjointSet, edge.dest)) {
                union(disjointSet, edge.src, edge.dest)
                mst.add(edge)
            }
        }
        return mst
    }
}