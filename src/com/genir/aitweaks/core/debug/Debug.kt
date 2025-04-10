package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.autofire.AutofireAI
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotated
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

object Debug {
    object Print {
        operator fun set(index: Any, value: Any?) {
            debugPlugin?.set(index, value)
        }
    }

    fun clear() = debugPlugin?.clear()

    val print: Print
        get() = Print

    private val debugPlugin: DebugPlugin?
        get() = state.debugPlugin

    fun drawCircle(pos: Vector2f, r: Float, color: Color = Color.CYAN) {
        debugPlugin?.renderer?.circles?.add(Renderer.Circle(pos, r, color))
    }

    fun drawArc(pos: Vector2f, r: Float, a: Arc, color: Color = Color.CYAN) {
        debugPlugin?.renderer?.arcs?.add(Renderer.Arc(pos, r, a, color))
    }

    fun drawArcArms(pos: Vector2f, r: Float, a: Arc, color: Color = Color.CYAN) {
        debugPlugin?.renderer?.lines?.add(Renderer.Line(pos, pos + (a.facing - a.half).unitVector * r, color))
        debugPlugin?.renderer?.lines?.add(Renderer.Line(pos, pos + (a.facing + a.half).unitVector * r, color))
    }

    fun drawLine(a: Vector2f, b: Vector2f, color: Color = Color.YELLOW) {
        debugPlugin?.renderer?.lines?.add(Renderer.Line(a, b, color))
    }

    fun drawVector(p: Vector2f, v: Vector2f, color: Color = Color.YELLOW) {
        debugPlugin?.renderer?.lines?.add(Renderer.Line(p, p + v, color))
    }

    fun drawEngineLines(ship: ShipAPI) {
        drawAccelerationLines(ship)
        drawTurnLines(ship)
    }

    fun drawBounds(entity: CombatEntityAPI, color: Color = Color.YELLOW) {
        val bounds = entity.exactBounds ?: return
        bounds.update(entity.location, entity.facing)

        bounds.segments.forEach {
            drawLine(it.p1, it.p2, color)
        }
    }

    fun drawAccelerationLines(ship: ShipAPI) {
        if (debugPlugin?.renderer == null) return

        val r = (ship.facing.direction - 90f).rotationMatrix
        val engine = ship.engineController

        listOfNotNull(
            if (engine.isAccelerating) Vector2f(0f, 1f) else null,
            if (engine.isAcceleratingBackwards) Vector2f(0f, -1f) else null,
            if (engine.isStrafingLeft) Vector2f(-1f, 0f) else null,
            if (engine.isStrafingRight) Vector2f(1f, 0f) else null,
        ).forEach {
            drawLine(ship.location, ship.location + (it * ship.collisionRadius * 1.2f).rotated(r), Color.BLUE)
        }
    }

    fun drawTurnLines(ship: ShipAPI) {
        if (debugPlugin?.renderer == null) return

        val r = (ship.facing.direction - 90f).rotationMatrix
        val engine = ship.engineController

        listOfNotNull(
            if (engine.isTurningLeft) Vector2f(-0.75f, 0.20f) else null,
            if (engine.isTurningRight) Vector2f(0.75f, 0.20f) else null,
        ).forEach {
            drawLine(ship.location, ship.location + (it * ship.collisionRadius * 1.2f).rotated(r), Color.CYAN)
        }
    }

    fun drawCollisionRadius(entity: CombatEntityAPI, color: Color = Color.CYAN) {
        if (debugPlugin?.renderer == null) return

        drawCircle(entity.location, entity.collisionRadius, color)
    }

    fun drawWeaponLines(ship: ShipAPI) {
        if (debugPlugin?.renderer == null) return

        val ais = ship.weaponGroupsCopy.flatMap { it.aiPlugins }.filter { it is AutofireAI && it.target != null }
        ais.forEach { drawLine(it.weapon.location, it.target!!, if (it.weapon.isPD) Color.YELLOW else Color.RED) }
    }

    private data class Edge(val src: Int, val dest: Int, val weight: Float)

    fun drawBattleGroup(group: Set<ShipAPI>, color: Color = Color.YELLOW) {
        if (debugPlugin?.renderer == null) return

        val ts = group.toTypedArray()
        val es: MutableList<Edge> = mutableListOf()

        for (i in ts.indices) {
            for (j in i + 1 until ts.size) {
                val w = (ts[i].location - ts[j].location).lengthSquared
                es.add(Edge(i, j, w))
            }
        }

        kruskal(es, ts.size).forEach {
            drawLine(ts[it.src].location, ts[it.dest].location, color)
        }
    }

    private fun kruskal(graph: List<Edge>, numVertices: Int): List<Edge> {
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
