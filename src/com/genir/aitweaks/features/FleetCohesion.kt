package com.genir.aitweaks.features

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AssignmentTargetAPI
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatAssignmentType
import com.fs.starfarer.api.combat.CombatAssignmentType.AVOID
import com.fs.starfarer.api.combat.CombatAssignmentType.RALLY_TASK_FORCE
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.combat.tasks.CombatTaskManager
import com.genir.aitweaks.utils.extensions.*
import com.genir.aitweaks.utils.targetTracker
import lunalib.lunaSettings.LunaSettings
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

class FleetCohesion : BaseEveryFrameCombatPlugin() {
    private var impl: FleetCohesionImpl? = null
    private var isOn = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_fleet_cohesion_ai") ?: false

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        when {
            Global.getCurrentState() != GameState.COMBAT -> return
            Global.getCombatEngine().isSimulation -> return
            !isOn -> return
        }

        if (impl == null) impl = FleetCohesionImpl()

        impl!!.advance(dt)
    }
}

private class FleetCohesionImpl {
    private val cohesionAssignments: MutableSet<AssignmentKey> = mutableSetOf()
    private val cohesionWaypoints: MutableSet<AssignmentTargetAPI> = mutableSetOf()

    private val advanceInterval = IntervalUtil(0.75f, 1f)
    private var validGroups: List<Set<ShipAPI>> = listOf()
    private var taskManager: CombatTaskManager = Global.getCombatEngine().getFleetManager(0).getTaskManager(false) as CombatTaskManager

    fun advance(dt: Float) {
        advanceInterval.advance(dt)
        if (!advanceInterval.intervalElapsed()) return

        val engine = Global.getCombatEngine()
        if (engine.isPaused) return

        // Cleanup of previous iteration assignments and waypoints.
        clearAssignments()
        clearWaypoints()

        when {
            taskManager.isInFullRetreat -> return

            // Giving assignments disrupts the full assault.
            taskManager.isFullAssault -> return

            // Do not force targets if there's an avoid assignment active.
            taskManager.allAssignments.firstOrNull { it.type == AVOID } != null -> return

            // Battle is already won.
            engine.getFleetManager(1).getTaskManager(false).isInFullRetreat -> return
        }

        // Identify enemy battle groups.
        val enemyFleet = engine.ships.filter { it.owner == 1 && it.isValidTarget }
        if (enemyFleet.isEmpty()) return
        val groups = segmentFleet(enemyFleet.toTypedArray())
        val groupsFromLargest = groups.sortedBy { it.dpSum }.reversed()
        validGroups = groupsFromLargest.filter { it.dpSum * 4f >= groupsFromLargest.first().dpSum }

        val fog = engine.getFogOfWar(0)
        val primaryTargets = validGroups.first().filter { it.isBig && fog.isVisible(it) }

        val ships = engine.ships.filter {
            when {
                it.owner != 0 -> false
                !it.isAlive -> false
                it.isExpired -> false
                !it.isBig -> false
                it.isAlly -> false
                it.isStation -> false
                it.isStationModule -> false
                it == engine.playerShip && engine.isUIAutopilotOn -> false
                else -> true
            }
        }

        // Assign targets.
        val channelWasOpen = taskManager.isCommChannelOpen
        ships.forEach { manageAssignments(it, primaryTargets) }
        if (!channelWasOpen && taskManager.isCommChannelOpen) taskManager.closeCommChannel()
    }

    private fun manageAssignments(ship: ShipAPI, primaryTargets: List<ShipAPI>) {
        // Ship has foreign assignment.
        if (ship.assignment != null) {
            return
        }

        val target = targetTracker[ship] ?: return

        // Ship is engaging or planning to engage the primary group.
        if (validGroups.first().contains(target)) return

        // Ship is engaging a secondary group.
        if (validGroups.any { it.contains(target) } && closeToEnemy(ship, target)) return

        // Ship has wrong target. Find the closest valid target in the main enemy battle group.
        val closestTarget = primaryTargets.minByOrNull { (it.location - ship.location).lengthSquared() } ?: return

        // Create waypoint on the target ship. Make sure it follow the target even on the map edge.
        val fleetManager = Global.getCombatEngine().getFleetManager(ship.owner)
        val waypoint = fleetManager.createWaypoint(Vector2f(), true)
        waypoint.location.set(closestTarget.location)
        cohesionWaypoints.add(waypoint)

        // Assign target to ship.
        val doNotRefundCP = taskManager.isCommChannelOpen
        val assignment = taskManager.createAssignment(RALLY_TASK_FORCE, waypoint, doNotRefundCP)
        taskManager.giveAssignment(ship.deployedFleetMember, assignment, false)

        if (ship.assignment != null) {
            val key = AssignmentKey(ship, ship.assignment!!.target.location, ship.assignment!!.type)
            cohesionAssignments.add(key)
        }
    }

    private fun clearAssignments() {
        // Assignments given be fleet cohesion AI may have
        // already expired. Remove only the non-expired ones.
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

    // Divide fleet into separate battle groups.
    private fun segmentFleet(fleet: Array<ShipAPI>): List<Set<ShipAPI>> {
        val maxRange = 2000f

        // Assign targets to battle groups.
        val groups = IntArray(fleet.size) { it }
        for (i in fleet.indices) {
            for (j in fleet.indices) {
                when {
                    // Cannot attach to battle group via frigate.
                    fleet[j].isSmall -> continue

                    // Frigate already attached to battle group.
                    fleet[i].isSmall && groups[i] != i -> continue

                    // Both targets already in same battle group.
                    groups[i] == groups[j] -> continue

                    // Too large distance between targets to connect.
                    (fleet[i].location - fleet[j].location).lengthSquared() > maxRange * maxRange -> continue
                }

                // Merge battle groups.
                val toMerge = groups[i]
                for (k in groups.indices) {
                    if (groups[k] == toMerge) groups[k] = groups[j]
                }
            }
        }

        // Build battle groups.
        val sets: MutableMap<Int, MutableSet<ShipAPI>> = mutableMapOf()
        for (i in fleet.indices) {
            if (!sets.contains(groups[i])) sets[groups[i]] = mutableSetOf()

            sets[groups[i]]!!.add(fleet[i])
        }

        return sets.values.toList()
    }

    private fun closeToEnemy(ship: ShipAPI, target: ShipAPI): Boolean {
        val maxRange = ship.maxFiringRange * 1.5f
        return (ship.location - target.location).lengthSquared() <= maxRange * maxRange
    }
}

private data class AssignmentKey(val ship: ShipAPI, val location: Vector2f?, val type: CombatAssignmentType)

private val Set<ShipAPI>.dpSum: Float
    get() = this.sumOf { it.deploymentPoints.toDouble() }.toFloat()

private val ShipAPI.isSmall: Boolean
    get() = this.isFighter || (this.isFrigate && !this.isModule)

private val ShipAPI.isBig: Boolean
    get() = this.isCruiser || this.isCapital
