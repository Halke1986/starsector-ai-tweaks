package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.combat.ai.movement.maneuvers.StrafeTargetManeuverV2
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.autofire.BallisticTarget
import com.genir.aitweaks.core.state.State
import com.genir.aitweaks.core.utils.VanillaShipCommand
import com.genir.aitweaks.core.utils.clearVanillaCommands
import com.genir.aitweaks.core.utils.defaultAIInterval
import java.awt.Color

/** Ship AI implementation that extends vanilla BasicShipAI and overrides certain decisions. */
class ExtendedShipAI(val ship: ShipAPI, config: ShipAIConfig) : Obfuscated.BasicShipAI(ship as Obfuscated.Ship, config) {
    private val engineController: EngineController = EngineController(ship)
    private val updateInterval: IntervalUtil = defaultAIInterval()
    private var weaponGroup: WeaponGroup = WeaponGroup(ship, listOf())

    var expectedFacing: Float? = null

    override fun advance(dt: Float) {
        debug()

        super.advance(dt)
        updateInterval.advance(dt)

        controlFacing(dt)
    }

    private fun debug() {
        if (State.state.config.highlightCustomAI) {
            Debug.drawCircle(ship.location, ship.collisionRadius / 2f, Color.YELLOW)
        }
    }

    /** Control the ship facing to properly aim weapons. */
    private fun controlFacing(dt: Float) {
        // Control facing only for frigates.
        if (ship.isModule || !ship.isFrigate) {
            return
        }

        expectedFacing = null

        if (updateInterval.intervalElapsed()) {
            // Refresh the list of weapons to aim.
            val weapons = ship.allGroupedWeapons.filter { shouldAimWeapon(it) }
            weaponGroup = WeaponGroup(ship, weapons)
        }

        if (ship.fluxTracker.isOverloadedOrVenting) {
            return
        }

        // Override only attack-related maneuvers.
        val currentManeuver: Obfuscated.Maneuver? = super.getCurrentManeuver()
        if (currentManeuver !is StrafeTargetManeuverV2 && currentManeuver !is Obfuscated.ApproachManeuver) {
            return
        }

        // Find ship target and force a refresh if it's invalid.
        val target: ShipAPI = currentManeuver.maneuver_getTarget() as? ShipAPI ?: return
        if (!target.isValidTarget) {
            super.cancelCurrentManeuver()
            return
        }

        // Aim ship only if the target is close to weapons range.
        val rangeThreshold = weaponGroup.maxRange * 1.75f
        if (rangeThreshold * 1.75f < (target.location - ship.location).length) {
            return
        }

        // Remove vanilla turn commands.
        clearVanillaCommands(ship, VanillaShipCommand.TURN_LEFT, VanillaShipCommand.TURN_RIGHT)

        // Control the ship rotation.
        val ballisticTarget = BallisticTarget.entity(target)
        expectedFacing = weaponGroup.attackFacing(ballisticTarget)
        engineController.facing(dt, expectedFacing!!)
    }

    private fun shouldAimWeapon(weapon: WeaponAPI): Boolean {
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
