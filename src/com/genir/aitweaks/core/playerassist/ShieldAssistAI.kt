package com.genir.aitweaks.core.playerassist

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType.OMNI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.combat.ai.FrontShieldAI
import com.fs.starfarer.combat.ai.OmniShieldAI
import com.fs.starfarer.combat.ai.OmniShieldControlAI
import com.fs.starfarer.combat.ai.ShieldAI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.extensions.command
import com.genir.aitweaks.core.extensions.isUnderManualControl
import com.genir.aitweaks.core.shipai.BaseShipAIPlugin
import com.genir.aitweaks.core.state.VanillaKeymap
import com.genir.aitweaks.core.utils.VanillaShipCommand
import com.genir.aitweaks.core.utils.clearVanillaCommands
import com.genir.aitweaks.core.utils.mousePosition
import java.lang.reflect.Field

class ShieldAssistAI(private val manager: ShieldAssistManager) : BaseShipAIPlugin() {
    private var prevPlayerShip: ShipAPI? = null
    private var shieldControlAI: OmniShieldControlAI? = null
    var forceShieldOff = false

    override fun advance(dt: Float) {
        val prevForceShieldOff = forceShieldOff
        forceShieldOff = false

        val ship: ShipAPI = Global.getCombatEngine().playerShip ?: return
        val shield: ShieldAPI = ship.shield ?: return

        // Decide if shield assist should run.
        when {
            !ship.isAlive -> return
            !ship.isUnderManualControl -> return

            shield.type != OMNI && shield.type != FRONT -> return
            !manager.enableShieldAssist -> return
        }

        // Update shield controller if the player ship changed.
        if (ship != prevPlayerShip) {
            prevPlayerShip = ship as Ship
            val flags = ShipwideAIFlags()
            shieldControlAI = OmniShieldControlAI(ship, flags)

            val shieldAI: ShieldAI = when (shield.type) {
                // Replace the default omni shield AI with front shield AI.
                FRONT -> FrontShieldAI(ship, flags)

                // Replace the default omni shield AI. Default AI is misconfigured
                // and keeps toggling the shield on and off when the ship isn’t in danger.
                else -> OmniShieldAI(ship, flags)
            }

            val shieldAIField: Field = OmniShieldControlAI::class.java.getDeclaredField("shieldAI")
            shieldAIField.setAccessible(true)

            shieldAIField.set(shieldControlAI, shieldAI)
        }

        // Clear player manual command.
        clearVanillaCommands(ship, VanillaShipCommand.TOGGLE_SHIELD)

        // Handle input.
        forceShieldOff = prevForceShieldOff
        if (VanillaKeymap.isKeyDownEvent(VanillaKeymap.PlayerAction.SHIP_SHIELDS)) {
            forceShieldOff = !forceShieldOff
            if (forceShieldOff == shield.isOn) {
                ship.command(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK)
            }
        }

        if (forceShieldOff) {
            return
        }

        // Control the shield.
        shieldControlAI!!.advance(dt)
        ship.mouseTarget?.let {
            ship.setShieldTargetOverride(it.x, it.y)
        }

        // Shield AI overrides the player ship mouse position.
        // The position needs to be restored.
        ship.mouseTarget.set(mousePosition())
    }
}
