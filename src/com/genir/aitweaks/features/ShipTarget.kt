package com.genir.aitweaks.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatAssignmentType
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize.CAPITAL_SHIP
import com.fs.starfarer.api.combat.ShipAPI.HullSize.CRUISER
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.genir.aitweaks.debug.debugPlugin
import com.genir.aitweaks.utils.extensions.*
import org.lazywizard.lazylib.ext.minus

class ShipTarget : BaseEveryFrameCombatPlugin() {
    private val assignments: MutableMap<DeployedFleetMemberAPI, DeployedFleetMemberAPI> = mutableMapOf()


    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val bigShips = Global.getCombatEngine().ships.filter {
            when {
                !it.isAlive -> false
                it.isExpired -> false
//                it.isVastBulk -> false
                it.hullSize != CRUISER && it.hullSize != CAPITAL_SHIP && !it.isModule -> false
                else -> true
            }
        }

        val enemyForces: Map<Int, List<ShipAPI>> = mapOf(
            0 to bigShips.filter { it.owner == 1 },
            1 to bigShips.filter { it.owner == 0 },
        )

        val hasWrongTarget = bigShips.filter {
            when {
                it.assignment != null -> false
                !it.hasAIType<BasicShipAI>() -> false
                closeToEnemy(it, enemyForces[it.owner]!!) -> false

                enemyForces[it.owner]!!.isEmpty() -> false
                enemyForces[it.owner]!!.contains(it.maneuverTarget) -> false

                else -> true
            }
        }

        debugPlugin.clear()

        hasWrongTarget.forEach { ship ->


            val target = enemyForces[ship.owner]!!.minByOrNull { (it.location - ship.location).length() }

            val engine = Global.getCombatEngine()
            val targetWrapper = engine.getFleetManager(ship.owner xor 1).getDeployedFleetMember(target)
            val shipWrapper = engine.getFleetManager(ship.owner).getDeployedFleetMember(ship)

            if (!assignments.contains(shipWrapper)) {

                val taskManager = engine.getFleetManager(ship.owner).getTaskManager(ship.isAlly)
                val assignment = taskManager.createAssignment(CombatAssignmentType.INTERCEPT, targetWrapper, false)
                taskManager.giveAssignment(shipWrapper, assignment, false)

                assignments[shipWrapper] = targetWrapper
            }

            debugPlugin[ship] = "${ship.hullSpec.hullId} ${target}"
        }

//        bigShips.filter { it.owner == 0 }.forEach { debugPlugin[it] = "${it.hullSpec.hullId} ${it.assignment?.type}" }


    }

    private fun closeToEnemy(ship: ShipAPI, enemies: List<ShipAPI>): Boolean {
        val maxRange = ship.maxFiringRange + 500f
        return enemies.firstOrNull { (it.location - ship.location).length() <= maxRange } != null
    }
}

val ShipAPI.maxFiringRange: Float
    get() = this.allWeapons.maxOfOrNull { it.range } ?: 0f