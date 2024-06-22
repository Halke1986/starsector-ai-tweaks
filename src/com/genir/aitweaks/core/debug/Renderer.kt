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

private var debugRenderer: Renderer = Renderer()

fun drawCircle(pos: Vector2f, r: Float, color: Color = Color.CYAN) {
    debugRenderer.circles.add(Renderer.Circle(pos, r, color))
}

fun drawLine(a: Vector2f, b: Vector2f, color: Color = Color.YELLOW) {
    debugRenderer.lines.add(Renderer.Line(a, b, color))
}

class Renderer : BaseCombatLayeredRenderingPlugin() {
    data class Line(val a: Vector2f, val b: Vector2f, val color: Color)
    data class Circle(val pos: Vector2f, val r: Float, val color: Color)

    internal val lines: MutableList<Line> = mutableListOf()
    internal val circles: MutableList<Circle> = mutableListOf()

    override fun advance(dt: Float) {
        debugRenderer = this
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        if (lines.isEmpty() && circles.isEmpty()) return

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

        GL11.glPopAttrib()

        lines.clear()
        circles.clear()
    }

    override fun getRenderRadius(): Float = 1e6f

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS)
    }
}