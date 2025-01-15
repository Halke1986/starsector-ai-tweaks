package com.genir.aitweaks.core

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.combat.ai.movement.maneuvers.StrafeTargetManeuverV2
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.EngineController
import com.genir.aitweaks.core.shipai.WeaponGroup
import com.genir.aitweaks.core.shipai.autofire.BallisticTarget
import com.genir.aitweaks.core.state.State
import com.genir.aitweaks.core.utils.VanillaShipCommand
import com.genir.aitweaks.core.utils.clearVanillaCommands
import com.genir.aitweaks.core.utils.defaultAIInterval
import java.awt.Color

/** Ship AI implementation that wraps around vanilla BasicShipAI and overrides certain decisions.
 * Currently, it is used to aim hardpoint weapons on frigates. */
class WrapperShipAI(val ship: ShipAPI) : Ship.ShipAIWrapper(Global.getSettings().createDefaultShipAI(ship, ShipAIConfig())) {
    private val basicShipAI: Obfuscated.BasicShipAI = super.getAI() as Obfuscated.BasicShipAI
    private val engineController: EngineController = EngineController(ship)
    private val updateInterval: IntervalUtil = defaultAIInterval()
    private var weaponGroup: WeaponGroup = WeaponGroup(ship, listOf())

    var expectedFacing: Float? = null

    override fun advance(dt: Float) {
        debug()

        basicShipAI.advance(dt)
        updateInterval.advance(dt)

        expectedFacing = null

        if (updateInterval.intervalElapsed()) {
            // Refresh the list of weapons to aim.
            val weapons = ship.allGroupedWeapons.filter { shouldAim(it) }
            weaponGroup = WeaponGroup(ship, weapons)
        }

        if (ship.fluxTracker.isOverloadedOrVenting) return

        // Override only attack-related maneuvers.
        val currentManeuver: Obfuscated.Maneuver? = basicShipAI.currentManeuver
        if (currentManeuver !is StrafeTargetManeuverV2 && currentManeuver !is Obfuscated.ApproachManeuver) return

        // Find ship target and force a refresh if it's invalid.
        val target: ShipAPI = currentManeuver.maneuver_getTarget() as? ShipAPI ?: return
        if (!target.isValidTarget) {
            basicShipAI.cancelCurrentManeuver()
            return
        }

        // Aim ship only if the target is close to weapons range.
        val rangeThreshold = weaponGroup.maxRange * 1.75f
        if (rangeThreshold * 1.75f < (target.location - ship.location).length) return

        // Remove vanilla turn commands.
        clearVanillaCommands(ship, VanillaShipCommand.TURN_LEFT, VanillaShipCommand.TURN_RIGHT)

        // Control the ship rotation.
        val ballisticTarget = BallisticTarget.entity(target)
        expectedFacing = weaponGroup.attackFacing(ballisticTarget)
        engineController.facing(dt, expectedFacing!!)
    }

    private fun debug() {
        if (State.state.config.highlightCustomAI) Debug.drawCircle(ship.location, ship.collisionRadius / 2f, Color.YELLOW)
    }

    companion object {
        fun shouldAim(weapon: WeaponAPI): Boolean {
            return when {
                weapon.isMissile -> false
                weapon.isDisabled -> false
                weapon.isPermanentlyDisabled -> false

                // Aim ship according to front facing weapon arcs.
                !weapon.isFrontFacing -> false

                else -> true
            }
        }
    }
}