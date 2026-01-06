package com.genir.aitweaks.core.shipai.global

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatFleetManagerAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.*

/** Make ships defaults to Search and Destroy order. Ships will not be automatically
 * assigned to Assault, Eliminate or any other tasks. Player can manually assign ships
 * to any tasks. */
class SearchAndDestroyManager(private val globalAI: GlobalAI) : BaseEveryFrameCombatPlugin() {
    private val initialAssignments: MutableMap<ShipAPI, CombatFleetManagerAPI.AssignmentInfo> = mutableMapOf()

    private var firstFrameWithShips = -1

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val engine = Global.getCombatEngine()

        val allShips = engine.ships.filter { ship ->
            when {
                !ship.canReceiveOrders -> false
                ship.owner != 0 -> false
                ship.isAlly -> false
                else -> true
            }
        }

        val shipsToOrder = allShips.filter { ship ->
            when {
                ship.isUnderManualControl -> false

                !ship.isAlwaysSearchDestroy -> false

                else -> true
            }
        }

        if (firstFrameWithShips == -1 && allShips.isNotEmpty()) {
            firstFrameWithShips = globalAI.frameTracker.unpausedCount
        }

        // The hullmod is suppressed during initial deployment,
        // to allow for easy objective capping.
        if (firstFrameWithShips == globalAI.frameTracker.unpausedCount) {
            shipsToOrder.forEach { ship ->
                if (ship.hasDirectOrders) {
                    initialAssignments.remove(ship)
                } else {
                    ship.assignment?.let { assignment ->
                        initialAssignments[ship] = assignment
                    }
                }
            }
        }

        if (firstFrameWithShips != globalAI.frameTracker.unpausedCount) {
            shipsToOrder.forEach { ship ->
                // Transform initial auto-assignments into direct orders,
                // so that the task manager will not modify them.
                val initialAssignment = initialAssignments[ship]
                if (initialAssignment != null) {
                    ship.taskManager.giveAssignment(ship.deployedFleetMember, initialAssignment, false)

                    initialAssignments.remove(ship)
                }

                if (!ship.hasDirectOrders) {
                    ship.taskManager.orderSearchAndDestroy(ship.deployedFleetMember, false)
                }
            }
        }
    }
}