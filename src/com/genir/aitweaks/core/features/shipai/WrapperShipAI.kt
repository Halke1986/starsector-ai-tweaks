package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.combat.ai.movement.maneuvers.StrafeTargetManeuverV2
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.debug.drawCircle
import com.genir.aitweaks.core.state.combatState
import com.genir.aitweaks.core.utils.averageFacing
import com.genir.aitweaks.core.utils.extensions.*
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

/** Ship AI implementation that wraps around vanilla BasicShipAI and overrides certain decisions.
 * Currently, it is used to aim hardpoint weapons on frigates. */
class WrapperShipAI(val ship: ShipAPI) : Ship.ShipAIWrapper(Global.getSettings().createDefaultShipAI(ship, ShipAIConfig())) {
    private val basicShipAI: Obfuscated.BasicShipAI = super.getAI() as Obfuscated.BasicShipAI
    private val engineController: EngineController = EngineController(ship)

    var expectedFacing: Float? = null

    override fun advance(dt: Float) {
        debug()

        basicShipAI.advance(dt)
        expectedFacing = null

        if (ship.fluxTracker.isOverloadedOrVenting) return

        // Override only attack-related maneuvers.
        val currentManeuver: Obfuscated.Maneuver? = basicShipAI.currentManeuver
        if (currentManeuver !is StrafeTargetManeuverV2 && currentManeuver !is Obfuscated.ApproachManeuver) return

        expectedFacing = ship.attackTarget?.let { aimShip(it) } ?: return

        // Remove vanilla turn commands.
        clearTurnCommands(ship)

        // Control the ship rotation.
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

    private fun aimShip(attackTarget: CombatEntityAPI): Float? {
        val hardpoints = ship.allGroupedWeapons.filter { shouldAim(it) }

        val maxRange = hardpoints.maxOfOrNull { it.slotRange } ?: 0f
        if (maxRange * 1.75f < (attackTarget.location - ship.location).length) return null

        val makeSolution = fun(weapon: WeaponAPI): Float? {
            val ai = weapon.customAI ?: return null

            // Use weapon intercept point instead of target vector,
            // as they may be different for hardpoints.
            val intercept: Vector2f? = if (ai.targetShip == attackTarget) ai.intercept
            else ai.plotIntercept(attackTarget)
            intercept ?: return null

            return (intercept - weapon.location).facing
        }

        val solutions: List<Float> = hardpoints.mapNotNull { makeSolution(it) }
        if (solutions.isEmpty()) return null

        return averageFacing(solutions)
    }

    companion object {
        fun shouldAim(weapon: WeaponAPI): Boolean {
            return when {
                weapon.isMissile -> false
                weapon.isBeam -> false
                !weapon.slot.isHardpoint -> false
                !weapon.isFrontFacing -> false
                weapon.isDisabled -> false
                weapon.isPermanentlyDisabled -> false

                else -> true
            }
        }
    }
}
