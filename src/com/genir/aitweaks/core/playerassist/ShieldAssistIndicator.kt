package com.genir.aitweaks.core.playerassist

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType.OMNI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.opengl.DrawUtils
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.max

class ShieldAssistIndicator(private val manager: ShieldAssistManager) : BaseCombatLayeredRenderingPlugin() {
    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        val engine = Global.getCombatEngine() ?: return
        val ship = engine.playerShip ?: return
        val shield = ship.shield ?: return

        when {
            shield.type != OMNI && shield.type != FRONT -> return
            !engine.isUIAutopilotOn -> return
            !manager.enableShieldAssist -> return
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        Misc.setColor(Color.CYAN, if (manager.ai?.forceShieldOff == true) 40 else 110)

        GL11.glLineWidth(2f / Global.getCombatEngine().viewport.viewMult)

        val arc = if (shield.activeArc > 2f) shield.activeArc + 10f else 0f
        DrawUtils.drawArc(
            shield.location.x,
            shield.location.y,
            shield.radius - 5f,
            shield.facing + arc / 2,
            max(0f, 360f - arc),
            64,
            false,
        )

        GL11.glPopAttrib()
    }

    override fun getRenderRadius(): Float = 1e6f

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> = EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS)
}
