package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.combat.ai.movement.maneuvers.EscortTargetManeuverV3
import com.fs.starfarer.combat.ai.movement.maneuvers.StrafeTargetManeuverV2
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.state.State
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.VanillaShipCommand
import com.genir.aitweaks.core.utils.clearVanillaCommands
import com.genir.aitweaks.core.utils.defaultAIInterval
import java.awt.Color

/** Ship AI implementation that extends vanilla BasicShipAI and overrides certain decisions. */
class ExtendedShipAI(val ship: ShipAPI, config: ShipAIConfig) : Obfuscated.BasicShipAI(ship as Obfuscated.Ship, config) {
    private val engineController: EngineController = EngineController(ship)
    private val updateInterval: IntervalUtil = defaultAIInterval()

    // Attack details.
    private var stats: ShipStats = ShipStats(ship)
    var expectedFacing: Direction? = null

    init {
        // Ensure AI Tweaks is in control of autofire management.
        AutofireManager.inject(ship, attackAI)
    }

    override fun advance(dt: Float) {
        debug()

        super.advance(dt)
        updateInterval.advance(dt)

        controlFacing(dt)
    }

    private fun debug() {
        if (State.state.config.highlightCustomAI) {
            Debug.drawCircle(ship.location, ship.collisionRadius / 2f, Color.GREEN)
        }
    }

    /** Control the ship facing to properly aim weapons. */
    private fun controlFacing(dt: Float) {
        if (ship.isModule) {
            return
        }

        expectedFacing = null

        if (updateInterval.intervalElapsed()) {
            // Refresh the list of weapons to aim.
            stats = ShipStats(ship)
        }

        if (ship.fluxTracker.isOverloadedOrVenting) {
            return
        }

        // Override the ship facing during attack-related and escort maneuvers.
        val currentManeuver: Obfuscated.Maneuver? = super.getCurrentManeuver()
        when (currentManeuver) {
            is StrafeTargetManeuverV2 -> Unit
            is Obfuscated.ApproachManeuver -> Unit
            is EscortTargetManeuverV3 -> Unit

            else -> return
        }

        // Find ship target and force a refresh if it's invalid.
        val target: ShipAPI = currentManeuver.maneuver_getTarget() as? ShipAPI ?: return
        if (!target.isValidTarget) {
            super.cancelCurrentManeuver()
            return
        }

        // Find a weapon group appropriate to attack the ship target.
        val targetFacing: Direction = (target.location - ship.location).facing - ship.facing.direction
        val weaponGroup = stats.weaponGroups.minWithOrNull(compareBy { (targetFacing - it.defaultFacing).length })!!

        // Aim ship only if the target is close to weapons range.
        val rangeThreshold = weaponGroup.maxRange * 1.75f
        if (rangeThreshold * 1.75f < (target.location - ship.location).length) {
            return
        }

        // Remove vanilla turn commands.
        clearVanillaCommands(ship, VanillaShipCommand.TURN_LEFT, VanillaShipCommand.TURN_RIGHT)

        // Control the ship rotation.
        expectedFacing = weaponGroup.attackFacing(target)
        engineController.facing(dt, expectedFacing!!)
    }
}
