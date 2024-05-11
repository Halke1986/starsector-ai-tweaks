package com.genir.aitweaks.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.util.Misc
import com.genir.aitweaks.features.autofire.AutofireAI
import com.genir.aitweaks.utils.Rotation
import com.genir.aitweaks.utils.extensions.isPD
import com.genir.aitweaks.utils.times
import lunalib.lunaSettings.LunaSettings
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

fun debugVertex(a: Vector2f, b: Vector2f, color: Color) {
    debugVertices.add(Line(a, b, color))
}

var debugVertices = mutableListOf<Line>()

class Line(val a: Vector2f, val b: Vector2f, val color: Color)

fun drawEngineLines(ship: ShipAPI) = shipsToDrawEngineLines.add(ship)

fun drawWeaponLines(ship: ShipAPI) = shipsToDrawWeaponLines.add(ship)

fun drawBattleGroup(group: Set<ShipAPI>, color: Color = Color.YELLOW) = DrawBattleGroup().drawBattleGroup(group, color)

class RenderLines : BaseCombatLayeredRenderingPlugin() {
    private fun drawDebugWeaponLines() {
        if (LunaSettings.getBoolean("aitweaks", "aitweaks_debug_weapon_target") == true) {
            val ship = Global.getCombatEngine().playerShip ?: return
            shipsToDrawWeaponLines.add(ship)
        }
    }

    private fun drawEngineLines(ship: ShipAPI) {
        val r = Rotation(ship.facing - 90f)
        val engine = ship.engineController

        listOfNotNull(
            if (engine.isAccelerating) Vector2f(0f, 1f) else null,
            if (engine.isAcceleratingBackwards) Vector2f(0f, -1f) else null,
            if (engine.isStrafingLeft) Vector2f(-1f, 0f) else null,
            if (engine.isStrafingRight) Vector2f(1f, 0f) else null,
        ).forEach {
            debugVertices.add(Line(ship.location, ship.location + r.rotate(it * ship.collisionRadius * 1.2f), Color.BLUE))
        }

        listOfNotNull(
            if (engine.isTurningLeft) Vector2f(-0.75f, 0.20f) else null,
            if (engine.isTurningRight) Vector2f(0.75f, 0.20f) else null,
        ).forEach {
            debugVertices.add(Line(ship.location, ship.location + r.rotate(it * ship.collisionRadius * 1.2f), Color.CYAN))
        }
    }

    private fun drawWeaponLines(ship: ShipAPI) {
        val ais = ship.weaponGroupsCopy.flatMap { it.aiPlugins }.filter { it is AutofireAI && it.target != null }
        debugVertices.addAll(ais.map { Line(it.weapon.location, it.target!!, if (it.weapon.isPD) Color.YELLOW else Color.RED) })
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        drawDebugWeaponLines()

        shipsToDrawEngineLines.forEach { drawEngineLines(it) }
        shipsToDrawEngineLines.clear()

        shipsToDrawWeaponLines.forEach { drawWeaponLines(it) }
        shipsToDrawWeaponLines.clear()

        if (debugVertices.isEmpty()) {
            return
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glLineWidth(3f / Global.getCombatEngine().viewport.viewMult)

        debugVertices.forEach {
            Misc.setColor(it.color)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex2f(it.a.x, it.a.y)
            GL11.glVertex2f(it.b.x, it.b.y)
            GL11.glEnd()
        }

        GL11.glPopAttrib()

        debugVertices.clear()
    }

    override fun getRenderRadius(): Float = 1e6f

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> = EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS)
}

private var shipsToDrawEngineLines: MutableSet<ShipAPI> = mutableSetOf()
private var shipsToDrawWeaponLines: MutableSet<ShipAPI> = mutableSetOf()

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
            debugVertices.add(Line(ts[it.src].location, ts[it.dest].location, color))
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