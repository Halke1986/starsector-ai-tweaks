package com.genir.aitweaks.core.shipai.global

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.state.Config
import com.genir.aitweaks.core.utils.closestEntity
import com.genir.aitweaks.core.utils.isCloseToEnemy
import com.genir.starfarer.combat.tasks.CombatTaskManager
import org.lwjgl.util.vector.Vector2f

/** FleetCohesionAI attempts to keep vanilla-AI controlled ships as a cohesive unit.
 * Vanilla AI, if left unsupervised, will let th ships scatter randomly around the
 * battlefield. */
class FleetCohesionAI(private val side: Int, private val globalAI: GlobalAI) : BaseEveryFrameCombatPlugin() {
    private val enemy: Int = side xor 1

    private val cohesionAssignments: MutableSet<AssignmentKey> = mutableSetOf()
    private val cohesionWaypoints: MutableSet<AssignmentTargetAPI> = mutableSetOf()

    private val advanceInterval = IntervalUtil(0.75f, 1f)

    private data class AssignmentKey(val ship: ShipAPI, val location: Vector2f?, val type: CombatAssignmentType)

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val engine = Global.getCombatEngine()
        when {
            !Config.config.enableFleetCohesion -> return

            engine.isSimulation -> return

            engine.isPaused -> return
        }

        advanceInterval.advance(dt)
        if (!advanceInterval.intervalElapsed()) return

        // Cleanup of previous iteration assignments and waypoints.
        clearAssignments()
        clearWaypoints()

        val taskManager = getTaskManager()
        when {
            taskManager.isInFullRetreat -> return

            // Giving assignments disrupts the full assault.
            taskManager.isFullAssault -> return

            // Do not force targets if there's an avoid assignment active.
            taskManager.allAssignments.firstOrNull { it.type == CombatAssignmentType.AVOID } != null -> return

            // Battle is already won.
            engine.getFleetManager(enemy).getTaskManager(false).isInFullRetreat -> return
        }

        // Don't give orders to enemy side
        // to not interfere with AdmiralAI.
        if (side == 1) return

        val ships = engine.ships.filter { ship ->
            when {
                !ship.canReceiveOrders -> false
                ship.owner != side -> false
                !ship.isBig -> false
                ship.isAlly -> false
                ship.basicShipAI == null -> false
                ship.isSkirmisher -> false
                else -> true
            }
        }

        // Assign targets.
        val channelWasOpen = taskManager.isCommChannelOpen
        ships.forEach { manageAssignments(it) }
        if (!channelWasOpen && taskManager.isCommChannelOpen) taskManager.closeCommChannel()
    }

    private fun findValidTarget(ship: ShipAPI, currentTarget: ShipAPI?): ShipAPI? {
        val segmentation = globalAI.fleetSegmentation[side]
        return when {
            // Ship is engaging or planning to engage the primary group.
            currentTarget in segmentation.primaryTargets() -> currentTarget

            // Ship is engaging a secondary group.
            currentTarget != null && currentTarget in segmentation.allTargets() && isCloseToEnemy(ship, currentTarget) -> currentTarget

            // Ship has wrong target. Find the closest valid target in the main enemy battle group.
            else -> {
                val fog: FogOfWarAPI? = Global.getCombatEngine().getFogOfWar(side)
                val targets: Sequence<ShipAPI> = segmentation.primaryTargets(false)
                val visibleTargets: Sequence<CombatEntityAPI> = fog?.filter(targets) ?: targets
                closestEntity(visibleTargets, ship.location) ?: currentTarget
            }
        }
    }

    private fun manageAssignments(ship: ShipAPI) {
        // Ship has foreign assignment.
        val currentAssignmentType: CombatAssignmentType? = ship.assignment?.type
        if (currentAssignmentType != null) {
            when {
                ship.variant.hasHullMod("aitweaks_search_and_destroy") && currentAssignmentType != CombatAssignmentType.SEARCH_AND_DESTROY -> {
                    return
                }

                !ship.variant.hasHullMod("aitweaks_search_and_destroy") -> {
                    return
                }
            }
        }

        val target = ship.attackTarget ?: return
        val validTarget = findValidTarget(ship, target)
        if (validTarget == null || validTarget == target) return

        // Create waypoint on the target ship. Make sure it follows the target even on the map edge.
        val fleetManager = Global.getCombatEngine().getFleetManager(ship.owner)
        val waypoint = fleetManager.createWaypoint(Vector2f(), true)
        waypoint.location.set(validTarget.location)
        cohesionWaypoints.add(waypoint)

        // Assign target to ship.
        val taskManager = getTaskManager()
        val doNotRefundCP = taskManager.isCommChannelOpen
        val assignment = taskManager.createAssignment(CombatAssignmentType.RALLY_TASK_FORCE, waypoint, doNotRefundCP)
        taskManager.giveAssignment(ship.deployedFleetMember, assignment, false)

        if (ship.assignment != null) {
            val key = AssignmentKey(ship, ship.assignment!!.target.location, ship.assignment!!.type)
            cohesionAssignments.add(key)
        }
    }

    private fun clearAssignments() {
        // Assignments given be fleet cohesion AI may have
        // already expired. Remove only the non-expired ones.
        val taskManager = getTaskManager()
        cohesionAssignments.forEach { assignment ->
            val ship = assignment.ship
            when {
                ship.isExpired -> Unit
                !ship.isAlive -> Unit
                ship.assignment == null -> Unit
                assignment != AssignmentKey(ship, ship.assignment!!.target?.location, ship.assignment!!.type) -> Unit

                else -> taskManager.removeAssignment(ship.assignment)
            }
        }

        cohesionAssignments.clear()
    }

    private fun clearWaypoints() {
        cohesionWaypoints.forEach {
            Global.getCombatEngine().removeObject(it)
        }

        cohesionWaypoints.clear()
    }

    private fun getTaskManager(): CombatTaskManager {
        return Global.getCombatEngine().getFleetManager(side).getTaskManager(false) as CombatTaskManager
    }
}