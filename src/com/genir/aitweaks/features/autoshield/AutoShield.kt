package com.genir.aitweaks.features.autoshield

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import com.genir.aitweaks.debugPlugin
import org.lazywizard.lazylib.opengl.DrawUtils.drawCircle
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*

const val ID = "com.genir.aitweaks.autoshield.AutoShield"

class AutoShield : BaseEveryFrameCombatPlugin() {
    private var isShieldOn = false

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        // Register render plugin.
        val engine = Global.getCombatEngine() ?: return
        if (!engine.customData.containsKey(ID)) {
            engine.addLayeredRenderingPlugin(Renderer())
            engine.customData[ID] = true
        }

        if (!engine.isUIAutopilotOn) return
        val shield = engine.playerShip?.shield ?: return
        if (shield.type != ShieldType.OMNI) return

        debugPlugin[0] = StateAccess.getAutoOmni()
        debugPlugin[1] = isShieldOn

        when {
            shield.isOn -> isShieldOn = true
            !StateAccess.getAutoOmni() -> isShieldOn = shield.isOn
        }

        events?.forEach {
            when {
                it.isConsumed -> Unit
                it.isRMBDownEvent -> isShieldOn = !isShieldOn
                it.isMouseDownEvent && it.eventValue == 2 -> StateAccess.setAutoOmni(!StateAccess.getAutoOmni())
            }
        }
    }

    inner class Renderer() : BaseCombatLayeredRenderingPlugin() {
        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
            val engine = Global.getCombatEngine() ?: return
            val ship = engine.playerShip ?: return
            val shield = ship.shield ?: return



            when {
                !StateAccess.getAutoOmni() -> return
                !isShieldOn -> return
                !engine.isUIAutopilotOn -> return
                shield.type != ShieldType.OMNI -> return
            }

            glPushAttrib(GL_ALL_ATTRIB_BITS)

            glDisable(GL_TEXTURE_2D)
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            Misc.setColor(Color.GREEN)
            glLineWidth(2f / Global.getCombatEngine().viewport.viewMult)

            drawCircle(ship.location.x, ship.location.y, shield.radius, 64, false)

            glPopAttrib()
        }


        override fun getRenderRadius(): Float {
            return 999999f
        }

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS)
        }
    }

}