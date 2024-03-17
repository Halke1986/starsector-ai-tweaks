package com.genir.aitweaks.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.features.autofire.AutofireAI
import com.genir.aitweaks.utils.div
import com.genir.aitweaks.utils.extensions.isPD
import com.genir.aitweaks.utils.extensions.strafeAcceleration
import com.genir.aitweaks.utils.rotate
import lunalib.lunaSettings.LunaSettings
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

var debugVertices = mutableListOf<Line>()

class Line(val a: Vector2f, val b: Vector2f, val color: Color)

class RenderLines : BaseCombatLayeredRenderingPlugin() {
    private fun drawWeaponLines() {
        if (LunaSettings.getBoolean("aitweaks", "aitweaks_debug_weapon_target") != true) return

        val ship = Global.getCombatEngine().playerShip ?: return
        val ais = ship.weaponGroupsCopy.flatMap { it.aiPlugins }.filter { it is AutofireAI && it.target != null }

        debugVertices.addAll(ais.map { Line(it.weapon.location, it.target!!, if (it.weapon.isPD) Color.YELLOW else Color.RED) })
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        drawWeaponLines()

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

fun drawEngineLines(ship: ShipAPI) {
    val cmds = (ship as Ship).commands.mapNotNull { runCatching { ShipCommand.valueOf(it.new.name) }.getOrNull() }
    cmds.mapNotNull {
        when (it) {
            ShipCommand.ACCELERATE -> Vector2f(0f, ship.acceleration)
            ShipCommand.ACCELERATE_BACKWARDS -> Vector2f(0f, -ship.deceleration)
            ShipCommand.STRAFE_RIGHT -> Vector2f(ship.strafeAcceleration, 0f)
            ShipCommand.STRAFE_LEFT -> Vector2f(-ship.strafeAcceleration, 0f)
            else -> null
        }
    }.forEach {
        debugVertices.add(Line(ship.location, ship.location + rotate(it / 3f, ship.facing - 90f), Color.BLUE))
    }
}
