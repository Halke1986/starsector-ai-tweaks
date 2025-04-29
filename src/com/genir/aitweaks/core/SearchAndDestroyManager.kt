package com.genir.aitweaks.core

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.deployedFleetMember
import com.genir.aitweaks.core.extensions.isModule
import com.genir.aitweaks.core.extensions.isUnderManualControl
import com.genir.aitweaks.core.extensions.taskManager

class SearchAndDestroyManager : BaseEveryFrameCombatPlugin() {
    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val engine = Global.getCombatEngine()

        val ships = engine.ships.filter { ship ->
            when {
                ship.owner != 0 -> false
                ship.isAlly -> false
                !ship.isAlive -> false
                ship.isExpired -> false
                ship.isStation -> false
                ship.isModule -> false
                ship.isFighter -> false
                ship.isUnderManualControl -> false
                !ship.variant.hasHullMod("aitweaks_search_and_destroy") -> false
                else -> true
            }
        }

        ships.forEach { ship ->
            val taskManager = ship.taskManager as Obfuscated.CombatTaskManager
            val fleetMember = ship.deployedFleetMember as Obfuscated.DeployedFleetMember

            if (!(taskManager).hasDirectOrders(fleetMember)) {
                ship.taskManager.orderSearchAndDestroy(ship.deployedFleetMember, false)
            }
        }
    }
}
