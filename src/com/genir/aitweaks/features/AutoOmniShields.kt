package com.genir.aitweaks.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.CombatState
import lunalib.lunaSettings.LunaSettings
import org.lazywizard.lazylib.opengl.DrawUtils.drawArc
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import kotlin.math.max

const val ID = "com.genir.aitweaks.features.AutoOmniShields"

class AutoOmniShields : BaseEveryFrameCombatPlugin() {
    private var doNotUseShields = false
    private var prevPlayerShip: ShipAPI? = null
    private var keybind: Int? = null

    override fun advance(timeDelta: Float, events: MutableList<InputEventAPI>?) {
        val engine = Global.getCombatEngine() ?: return

        // Initialize omni shield plugin.
        if (!engine.customData.containsKey(ID)) {
            engine.addLayeredRenderingPlugin(RendererAutoShieldIndicator())
            keybind = LunaSettings.getInt("aitweaks", "aitweaks_omni_shield_keybind")
            engine.customData[ID] = true
        }

        if (keybind == null) return

        // Player ship has changed.
        if (engine.playerShip != prevPlayerShip) {
            prevPlayerShip = engine.playerShip
            doNotUseShields = false
        }

        // Do work only for ships with omni shields under player control.
        val shield = engine.playerShip?.shield ?: return
        if (shield.type != ShieldType.OMNI || !engine.isUIAutopilotOn) return

        // Handle input.
        events?.forEach {
            when {
                it.isConsumed -> Unit
                it.isRMBDownEvent && CombatState.AUTO_OMNI_SHIELDS -> doNotUseShields = shield.isOn
                it.isKeyDownEvent && it.eventValue == keybind -> CombatState.AUTO_OMNI_SHIELDS = !CombatState.AUTO_OMNI_SHIELDS
            }
        }
    }

    inner class RendererAutoShieldIndicator : BaseCombatLayeredRenderingPlugin() {
        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
            val engine = Global.getCombatEngine() ?: return
            val ship = engine.playerShip ?: return
            val shield = ship.shield ?: return

            when {
                shield.type != ShieldType.OMNI -> return
                !CombatState.AUTO_OMNI_SHIELDS -> return
                !engine.isUIAutopilotOn -> return
                doNotUseShields -> return
            }

            glPushAttrib(GL_ALL_ATTRIB_BITS)

            glDisable(GL_TEXTURE_2D)
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            Misc.setColor(Color.CYAN, 100)
            glLineWidth(2f / Global.getCombatEngine().viewport.viewMult)

            val arc = if (shield.activeArc > 2f) shield.activeArc + 10f else 0f
            drawArc(
                shield.location.x,
                shield.location.y,
                shield.radius - 5f,
                shield.facing + arc / 2,
                max(0f, 360f - arc),
                64,
                false,
            )

            glPopAttrib()
        }

        override fun getRenderRadius(): Float = 1e6f

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> = EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS)
    }
}