package com.genir.aitweaks.core.playerassist

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.handles.ShipHandle
import com.genir.aitweaks.core.handles.ShipHandle.Companion.handle
import com.genir.aitweaks.core.shipai.BaseShipAI
import com.genir.aitweaks.core.state.VanillaKeymap
import com.genir.aitweaks.core.utils.VanillaShipCommand
import com.genir.aitweaks.core.utils.clearVanillaCommands
import com.genir.aitweaks.core.utils.mousePosition
import com.genir.starfarer.combat.ai.FrontShieldAI
import com.genir.starfarer.combat.ai.OmniShieldAI
import com.genir.starfarer.combat.ai.OmniShieldControlAI
import com.genir.starfarer.combat.ai.ShieldAI
import com.genir.starfarer.combat.entities.Ship
import java.lang.reflect.Field

class ShieldAssistAI(private val manager: ShieldAssistManager) : BaseShipAI() {
    private var prevPlayerShip: ShipHandle? = null
    private var shieldControlAI: OmniShieldControlAI? = null
    var forceShieldOff = false

    override fun advance(dt: Float) {
        val prevForceShieldOff = forceShieldOff
        forceShieldOff = false

        if (!manager.shouldRunShieldAssist()) {
            return
        }

        val ship: ShipHandle = Global.getCombatEngine().playerShip?.handle
            ?: return
        val shield: ShieldAPI = ship.shield
            ?: return

        // Update shield controller if the player ship changed.
        if (ship != prevPlayerShip) {
            prevPlayerShip = ship
            val flags = ShipwideAIFlags()
            shieldControlAI = OmniShieldControlAI(ship.shipAPI as Ship, flags)

            val shieldAI: ShieldAI = when (shield.type) {
                // Replace the default omni shield AI with front shield AI.
                FRONT -> FrontShieldAI(ship.shipAPI as Ship, flags)

                // Replace the default omni shield AI. Default AI is misconfigured
                // and keeps toggling the shield on and off when the ship isn’t in danger.
                else -> OmniShieldAI(ship.shipAPI as Ship, flags)
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
