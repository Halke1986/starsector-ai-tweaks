package com.genir.aitweaks.core

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AssignmentTargetAPI
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.starfarer.combat.tasks.CombatTaskManager

/** Make ships defaults to Search and Destroy order. Ships will not be automatically
 * assigned to Assault, Eliminate or any other tasks. Player can manually assign ships
 * to any tasks. */
class SearchAndDestroyManager : BaseEveryFrameCombatPlugin() {
    private val initialAssignments: MutableMap<ShipAPI, AssignmentTargetAPI> = mutableMapOf()

    private var firstFrameWithShips = -1

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val engine = Global.getCombatEngine()

        val allShips = engine.ships.filter { ship ->
            when {
                ship.owner != 0 -> false
                ship.isAlly -> false
                !ship.isAlive -> false
                ship.isExpired -> false
                ship.isStation -> false
                ship.isModule -> false
                ship.isFighter -> false
                else -> true
            }
        }

        val shipsToOrder = allShips.filter { ship ->
            when {
                ship.isUnderManualControl -> false
                !ship.variant.hasHullMod("aitweaks_search_and_destroy") -> false
                else -> true
            }
        }

        if (firstFrameWithShips == -1 && allShips.isNotEmpty()) {
            firstFrameWithShips = state.unpausedFrameCount
        }

        // The hullmod is suppressed during initial deployment,
        // to allow for easy objective capping.
        if (firstFrameWithShips == state.unpausedFrameCount) {
            shipsToOrder.forEach { ship ->
                val assignment = ship.assignment

                if (assignment != null && !ship.hasDirectOrders) {
                    initialAssignments[ship] = assignment.target
                }
            }
        }

        shipsToOrder.forEach { ship ->
            if (ship.hasDirectOrders) {
                initialAssignments.remove(ship)
                return@forEach
            }

            val assignment = ship.assignment
            if (assignment != null && assignment.target == initialAssignments[ship]) {
                return@forEach
            }

            if (firstFrameWithShips != state.unpausedFrameCount) {
                ship.taskManager.orderSearchAndDestroy(ship.deployedFleetMember, false)
            }
        }
    }

    private val ShipAPI.hasDirectOrders: Boolean
        get() {
            val taskManager = taskManager as CombatTaskManager
            val fleetMember = deployedFleetMember as CombatTaskManager.DeployedFleetMember

            return taskManager.hasDirectOrders(fleetMember)
        }
}
