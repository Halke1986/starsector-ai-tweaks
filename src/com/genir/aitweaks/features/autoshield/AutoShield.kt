package com.genir.aitweaks.features.autoshield

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import com.genir.aitweaks.debugPlugin
import org.lazywizard.lazylib.opengl.DrawUtils.drawCircle
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*

const val ID = "com.genir.aitweaks.autoshield.AutoShield.AutoShield"

class AutoShield : BaseEveryFrameCombatPlugin() {
    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val engine = Global.getCombatEngine()

        if (!engine.customData.containsKey(ID)) {
            StateAccess.setAutoOmni(true)
            engine.addLayeredRenderingPlugin(Renderer())
            engine.customData[ID] = true
        }
    }
}

class Renderer : BaseCombatLayeredRenderingPlugin() {
    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
//        debugPlugin[0] = ""
//        debugPlugin[1] = ""


        val ship = Global.getCombatEngine()?.playerShip ?: return
//
        debugPlugin[0] = ship.location

        // TODO test ships with no shield
//        if (ship.shield?.type != ShieldAPI.ShieldType.OMNI) return

        glPushAttrib(GL_ALL_ATTRIB_BITS)

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        Misc.setColor(Color.GREEN)
        glLineWidth(2f / Global.getCombatEngine().viewport.viewMult)
        drawCircle(ship.location.x, ship.location.y, 200f, 64, false)

        glEnable(GL_TEXTURE_2D)

        glPopAttrib()


//        debugPlugin[1] = ship.shieldRadiusEvenIfNoShield

    }

    override fun getRenderRadius(): Float {
        return 999999f
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS)
    }
}

