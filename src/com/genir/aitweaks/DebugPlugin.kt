package com.genir.aitweaks

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import com.genir.aitweaks.features.autofire.AutofireAI
import com.genir.aitweaks.utils.times
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

private const val ID = "com.genir.aitweaks.DebugPlugin"

var debugPlugin: DebugPlugin = DebugPlugin()
var debugVertices: MutableList<Line> = mutableListOf()

data class Line(val a: Vector2f, val b: Vector2f, val color: Color)

// DebugPlugin is used to render debug information during combat.
class DebugPlugin : BaseEveryFrameCombatPlugin() {
    private var font: LazyFont? = null
    private var logs: MutableMap<String, LazyFont.DrawableString> = TreeMap()

    operator fun set(index: Any, value: Any?) {
        if (font == null) return

        if (value == null) logs.remove("$index")
        else logs["$index"] = font!!.createText("$value", baseColor = Color.ORANGE)
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if (font == null) {
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
            debugPlugin = this
        }

        // Initialize debug renderer.
        val engine = Global.getCombatEngine()
        if (!engine.customData.containsKey(ID)) {
            engine.addLayeredRenderingPlugin(RenderDebugLines())
            engine.customData[ID] = true
        }

        debug()
//        speedupAsteroids()
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)

        for ((i, v) in logs.entries.withIndex()) {
            v.value.draw(500f, 500f + (logs.count() / 2 - i) * 16f)
        }
    }

    private fun debug() {
//        val ships = Global.getCombatEngine().ships
//
//        for (i in ships.indices) {
//            val it = ships[i]
//            debugPlugin[i] = "${it.hullSpec.hullId} ${it.stationSlot} ${it.owner}"
//        }
    }

    private fun speedupAsteroids() {
        val asteroids = Global.getCombatEngine().asteroids
        for (i in asteroids.indices) {
            val a = asteroids[i]
            a.mass = 0f
            a.velocity.set(VectorUtils.getDirectionalVector(Vector2f(), a.velocity) * 1200f)
        }
    }

    inner class RenderDebugLines : BaseCombatLayeredRenderingPlugin() {
        private fun getVertices(): List<Line> {
            val ships = Global.getCombatEngine().ships.filter { it != Global.getCombatEngine().playerShip }
            val ais = ships.flatMap { it.weaponGroupsCopy }.flatMap { it.aiPlugins }.filterIsInstance<AutofireAI>()
            val hardpoints = ais.filter { it.weapon.slot.isHardpoint && it.target != null }

            return hardpoints.map { Line(it.weapon.location, it.target!!, Color.RED) }
        }

        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
            if (debugVertices.isEmpty()) {
                return
            }

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)

            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glLineWidth(2f / Global.getCombatEngine().viewport.viewMult)

            debugVertices.forEach {
                Misc.setColor(it.color)
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex2f(it.a.x, it.a.y);
                GL11.glVertex2f(it.b.x, it.b.y);
                GL11.glEnd();
            }

            GL11.glPopAttrib()

            debugVertices.clear()
        }

        override fun getRenderRadius(): Float = 1e6f

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> = EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS)
    }
}
