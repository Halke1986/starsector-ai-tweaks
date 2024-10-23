package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.combat.ai.movement.maneuvers.StrafeTargetManeuverV2
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.debug.drawCircle
import com.genir.aitweaks.core.state.combatState
import com.genir.aitweaks.core.utils.Interval
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.aitweaks.core.utils.extensions.allGroupedWeapons
import com.genir.aitweaks.core.utils.extensions.isMissile
import com.genir.aitweaks.core.utils.extensions.isValidTarget
import com.genir.aitweaks.core.utils.extensions.length
import org.lazywizard.lazylib.ext.minus
import java.awt.Color

/** Ship AI implementation that wraps around vanilla BasicShipAI and overrides certain decisions.
 * Currently, it is used to aim hardpoint weapons on frigates. */
class WrapperShipAI(val ship: ShipAPI) : Ship.ShipAIWrapper(Global.getSettings().createDefaultShipAI(ship, ShipAIConfig())) {
    private val basicShipAI: Obfuscated.BasicShipAI = super.getAI() as Obfuscated.BasicShipAI
    private val engineController: EngineController = EngineController(ship)
    private val updateInterval: Interval = defaultAIInterval()
    private var weaponGroup: WeaponGroup = WeaponGroup(ship, listOf(), 0f)

    var expectedFacing: Float? = null

    override fun advance(dt: Float) {
        debug()

        basicShipAI.advance(dt)
        updateInterval.advance(dt)

        expectedFacing = null

        if (updateInterval.elapsed()) {
            updateInterval.reset()

            // Refresh the list of weapons to aim.
            val weapons = ship.allGroupedWeapons.filter { shouldAim(it) }
            weaponGroup = WeaponGroup(ship, weapons, 0f)
        }

        if (ship.fluxTracker.isOverloadedOrVenting) return

        // Override only attack-related maneuvers.
        val currentManeuver: Obfuscated.Maneuver? = basicShipAI.currentManeuver
        if (currentManeuver !is StrafeTargetManeuverV2 && currentManeuver !is Obfuscated.ApproachManeuver) return

        // Find ship target and force a refresh is it's invalid.
        val target: ShipAPI = currentManeuver.target_Maneuver as? ShipAPI ?: return
        if (!target.isValidTarget) {
            basicShipAI.cancelCurrentManeuver()
            return
        }

        // Aim ship only if the target is close to weapons range.
        val rangeThreshold = weaponGroup.maxRange * 1.75f
        if (rangeThreshold * 1.75f < (target.location - ship.location).length) return

        // Remove vanilla turn commands.
        clearTurnCommands(ship)

        // Control the ship rotation.
        expectedFacing = Movement.aimShip(target, weaponGroup)
        engineController.facing(dt, expectedFacing!!)
    }

    private fun debug() {
        if (combatState.devMode) drawCircle(ship.location, ship.collisionRadius / 2f, Color.YELLOW)
    }

    private fun clearTurnCommands(ship: Any) {
        val commandWrappers: MutableIterator<Obfuscated.ShipCommandWrapper> = (ship as Obfuscated.Ship).commands.iterator()
        while (commandWrappers.hasNext()) {
            val command: Obfuscated.ShipCommand = commandWrappers.next().command_ShipCommandWrapper
            if (command.ordinal == 0 || command.ordinal == 1) commandWrappers.remove()
        }
    }

    companion object {
        fun shouldAim(weapon: WeaponAPI): Boolean {
            return when {
                weapon.isMissile -> false
                weapon.isDisabled -> false
                weapon.isPermanentlyDisabled -> false

                else -> true
            }
        }
    }
}
