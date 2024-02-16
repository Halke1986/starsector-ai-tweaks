package com.genir.aitweaks.features.autoshield

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import com.genir.aitweaks.debugPlugin
import org.lazywizard.lazylib.opengl.DrawUtils.drawCircle
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*

const val ID = "com.genir.aitweaks.autoshield.AutoShield"

class AutoShield : BaseEveryFrameCombatPlugin() {
    private var wantShieldOnForAutoOmni = false
    private var doNotUseShields = false

    private var prevPlayerShip: ShipAPI? = null

//    private var isAIActive = false
//    private var initialized = false

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val engine = Global.getCombatEngine() ?: return

        debugPlugin[0] = StateAccess.getAutoOmni()
        debugPlugin[1] = wantShieldOnForAutoOmni

        // Register render plugin.
        if (!engine.customData.containsKey(ID)) {
            engine.addLayeredRenderingPlugin(Renderer())
            engine.customData[ID] = true
        }

        // Player ship has changed.
        if (engine.playerShip != prevPlayerShip) {
            prevPlayerShip = engine.playerShip
            wantShieldOnForAutoOmni = false
            doNotUseShields = false
        }

        // The ship is on autopilot.
        if (!engine.isUIAutopilotOn) {
            wantShieldOnForAutoOmni = false
        }

        // Do work only for ship with omni shield.
        val shield = engine.playerShip?.shield ?: return
        if (shield.type != ShieldType.OMNI) return

        // Handle input
        events?.forEach {
            when {
                it.isConsumed -> Unit
                it.isMouseDownEvent && it.eventValue == 2 -> StateAccess.setAutoOmni(!StateAccess.getAutoOmni())
                it.isRMBDownEvent && StateAccess.getAutoOmni() -> {
                    doNotUseShields = shield.isOn
                    wantShieldOnForAutoOmni = !shield.isOn
                }
            }
        }


//        // Initialize the AI state.
//        if (!initialized) {
//            StateAccess.setAutoOmni(true) // TODO tmp
//            initialized = true
//            isAIActive = true
//        }
//
//        // Ship is on autopilot.
//        if (!engine.isUIAutopilotOn) {
//            isAIActive = false
//            return
//        }

//        isAIActive = isAIActive and StateAccess.getAutoOmni()


//        debugPlugin[0] = StateAccess.getAutoOmni()
//        debugPlugin[1] = isAIActive

//        when {
//            shield.isOn -> isAIActive = true
//            !StateAccess.getAutoOmni() -> isAIActive = shield.isOn
//        }


    }

    inner class Renderer() : BaseCombatLayeredRenderingPlugin() {
        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
            val engine = Global.getCombatEngine() ?: return
            val ship = engine.playerShip ?: return
            val shield = ship.shield ?: return

//            if (!StateAccess.getAutoOmni() || !wantShieldOnForAutoOmni) return
            if (!StateAccess.getAutoOmni() || doNotUseShields) return

            glPushAttrib(GL_ALL_ATTRIB_BITS)

            glDisable(GL_TEXTURE_2D)
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            Misc.setColor(Color.BLUE)
            glLineWidth(2f / Global.getCombatEngine().viewport.viewMult)

            drawCircle(shield.location.x, shield.location.y, shield.radius, 64, false)

            glPopAttrib()
        }


        override fun getRenderRadius(): Float = 1e6f

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> = EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS)
    }
}