package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.opengl.DrawUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class Renderer : BaseCombatLayeredRenderingPlugin() {
    data class Line(val a: Vector2f, val b: Vector2f, val color: Color)
    data class Circle(val pos: Vector2f, val r: Float, val color: Color)
    data class Arc(val pos: Vector2f, val r: Float, val a: com.genir.aitweaks.core.utils.types.Arc, val color: Color)

    internal var lines: MutableSet<Line> = mutableSetOf()
    internal var circles: MutableSet<Circle> = mutableSetOf()
    internal var arcs: MutableSet<Arc> = mutableSetOf()

    private var prevLines: MutableSet<Line> = mutableSetOf()
    private var prevCircles: MutableSet<Circle> = mutableSetOf()
    private var prevArcs: MutableSet<Arc> = mutableSetOf()

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        if (Global.getCombatEngine().isPaused) {
            lines = prevLines
            circles = prevCircles
            arcs = prevArcs
        } else {
            prevLines = mutableSetOf()
            prevCircles = mutableSetOf()
            prevArcs = mutableSetOf()
        }

        if (lines.isEmpty() && circles.isEmpty() && arcs.isEmpty()) {
            return
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glLineWidth(3f / Global.getCombatEngine().viewport.viewMult)

        lines.forEach {
            Misc.setColor(it.color)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex2f(it.a.x, it.a.y)
            GL11.glVertex2f(it.b.x, it.b.y)
            GL11.glEnd()
        }

        circles.forEach {
            Misc.setColor(it.color)
            DrawUtils.drawArc(
                it.pos.x,
                it.pos.y,
                it.r,
                0f,
                360f,
                64,
                false,
            )
        }

        arcs.forEach {
            Misc.setColor(it.color)
            DrawUtils.drawArc(
                it.pos.x,
                it.pos.y,
                it.r,
                (it.a.facing - it.a.halfAngle).degrees,
                it.a.angle,
                64,
                false,
            )
        }

        GL11.glPopAttrib()

        prevArcs = arcs
        prevCircles = circles
        prevLines = lines

        arcs = mutableSetOf()
        circles = mutableSetOf()
        lines = mutableSetOf()
    }

    override fun getRenderRadius(): Float = 1e6f

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS)
    }
}
