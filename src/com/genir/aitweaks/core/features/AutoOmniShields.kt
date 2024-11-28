package com.genir.aitweaks.core.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine
import com.fs.starfarer.combat.ai.OmniShieldControlAI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.features.shipai.BaseShipAIPlugin
import com.genir.aitweaks.core.features.shipai.command
import com.genir.aitweaks.core.state.State
import com.genir.aitweaks.core.state.VanillaKeymap
import com.genir.aitweaks.core.state.VanillaKeymap.PlayerAction.SHIP_SHIELDS
import com.genir.aitweaks.core.utils.VanillaShipCommand
import com.genir.aitweaks.core.utils.clearVanillaCommands
import com.genir.aitweaks.core.utils.extensions.isUnderManualControl
import com.genir.aitweaks.core.utils.makeAIDrone
import com.genir.aitweaks.core.utils.mousePosition
import org.lazywizard.lazylib.opengl.DrawUtils
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.max

class AutoOmniShields : BaseEveryFrameCombatPlugin() {
    private var aiDrone: ShipAPI? = null

    private var enableShieldAssist = false
    private var forceShieldOff = false

    companion object {
        const val ENABLE_SHIELD_ASSIST_KEY = "\$aitweaks_AUTO_OMNI_SHIELDS"
        const val RENDER_PLUGIN_KEY = "aitweaks_shield_assist_renderer"
    }

    override fun advance(timeDelta: Float, events: MutableList<InputEventAPI>?) {
        // Load shield assist setting from memory.
        val memory: MemoryAPI = CampaignEngine.getInstance().memoryWithoutUpdate
        enableShieldAssist = memory.getBoolean(ENABLE_SHIELD_ASSIST_KEY)

        // Handle input.
        events?.forEach {
            // Toggle the shield assist and persist the setting to memory.
            if (!it.isConsumed && it.isKeyDownEvent && it.eventValue == State.state.config.omniShieldKeybind) {
                enableShieldAssist = !enableShieldAssist
                memory.set(ENABLE_SHIELD_ASSIST_KEY, enableShieldAssist)
            }
        }

        // Nothing to do, exit early.
        if (!enableShieldAssist) return

        // Make a drone to hold player ship shield AI.
        if (aiDrone == null) {
            aiDrone = makeAIDrone(ShieldAssistAI())
            Global.getCombatEngine().addEntity(aiDrone)
        }

        // Initialize shield assist rendering plugin.
        val engine = Global.getCombatEngine()
        if (!engine.customData.containsKey(RENDER_PLUGIN_KEY)) {
            engine.addLayeredRenderingPlugin(RendererAutoShieldIndicator())
            engine.customData[RENDER_PLUGIN_KEY] = true
        }
    }

    inner class ShieldAssistAI : BaseShipAIPlugin() {
        private var prevPlayerShip: ShipAPI? = null
        private var shieldAI: OmniShieldControlAI? = null

        override fun advance(dt: Float) {
            val prevForceShieldOff = forceShieldOff
            forceShieldOff = false

            val ship: ShipAPI = Global.getCombatEngine().playerShip ?: return
            val shield: ShieldAPI = ship.shield ?: return

            // Decide if shield assist should run.
            when {
                !ship.isAlive -> return
                !ship.isUnderManualControl -> return

                shield.type != ShieldAPI.ShieldType.OMNI -> return
                !enableShieldAssist -> return
            }

            // Update shield controller if the player ship changed.
            if (ship != prevPlayerShip) {
                prevPlayerShip = ship
                val flags = ShipwideAIFlags()
                shieldAI = OmniShieldControlAI(ship as Ship, flags)
            }

            // Clear player manual command.
            clearVanillaCommands(ship, VanillaShipCommand.TOGGLE_SHIELD)

            // Handle input.
            forceShieldOff = prevForceShieldOff
            if (VanillaKeymap.isKeyDownEvent(SHIP_SHIELDS)) {
                forceShieldOff = !forceShieldOff
                if (forceShieldOff == shield.isOn) ship.command(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK)
            }

            if (forceShieldOff) return

            // Control the omni shield.
            shieldAI!!.advance(dt)
            ship.mouseTarget?.let { ship.setShieldTargetOverride(it.x, it.y) }

            // Shield AI overrides the player ship mouse position.
            // The position needs to be restored.
            ship.mouseTarget.set(mousePosition())
        }
    }

    inner class RendererAutoShieldIndicator : BaseCombatLayeredRenderingPlugin() {
        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
            val engine = Global.getCombatEngine() ?: return
            val ship = engine.playerShip ?: return
            val shield = ship.shield ?: return

            when {
                shield.type != ShieldAPI.ShieldType.OMNI -> return
                !engine.isUIAutopilotOn -> return
                !enableShieldAssist -> return
            }

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)

            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            Misc.setColor(Color.CYAN, if (forceShieldOff) 40 else 110)

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
}
