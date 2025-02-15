package com.genir.aitweaks.core.playerassist

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType.OMNI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.combat.ai.OmniShieldControlAI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.Obfuscated
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
    private var shieldAI: OmniShieldControlAI? = null
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
            prevPlayerShip = ship
            val flags = ShipwideAIFlags()
            shieldAI = OmniShieldControlAI(ship as Ship, flags)

            // Replace the default omni shield AI with front shield AI.
            if (shield.type == FRONT) {
                val frontShieldAI = Obfuscated.FrontShieldAI(ship, flags)

                val shieldAIField: Field = OmniShieldControlAI::class.java.getDeclaredField("shieldAI")
                shieldAIField.setAccessible(true)

                shieldAIField.set(shieldAI, frontShieldAI)
            }
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
        shieldAI!!.advance(dt)
        ship.mouseTarget?.let { ship.setShieldTargetOverride(it.x, it.y) }

        // Shield AI overrides the player ship mouse position.
        // The position needs to be restored.
        ship.mouseTarget.set(mousePosition())
    }
}
