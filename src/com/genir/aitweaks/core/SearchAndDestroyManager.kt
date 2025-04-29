package com.genir.aitweaks.core

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AssignmentTargetAPI
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.state.State.Companion.state

class SearchAndDestroyManager : BaseEveryFrameCombatPlugin() {
    private val initialAssignments: MutableMap<ShipAPI, AssignmentTargetAPI> = mutableMapOf()

    private var firstFrameWithShips = Int.MAX_VALUE

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

        // Remember non-direct orders given during initial deployment.
        if (firstFrameWithShips >= state.unpausedFrameCount && allShips.isNotEmpty()) {
            shipsToOrder.forEach { ship ->
                val assignment = ship.assignment

                if (assignment != null && !ship.hasDirectOrders) {
                    initialAssignments[ship] = assignment.target
                }
            }

            firstFrameWithShips = state.unpausedFrameCount
            return
        }

        shipsToOrder.forEach { ship ->
            if (ship.hasDirectOrders) {
                return@forEach
            }

            val assignment = ship.assignment
            if (assignment != null) {
                if (assignment.target == initialAssignments[ship]) {
                    return@forEach
                } else {
                    initialAssignments.remove(ship)
                }
            }

            ship.taskManager.orderSearchAndDestroy(ship.deployedFleetMember, false)
        }
    }

    private val ShipAPI.hasDirectOrders: Boolean
        get() {
            val taskManager = taskManager as Obfuscated.CombatTaskManager
            val fleetMember = deployedFleetMember as Obfuscated.DeployedFleetMember

            return taskManager.hasDirectOrders(fleetMember)
        }
}
