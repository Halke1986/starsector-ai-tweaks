package com.genir.aitweaks.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatAssignmentType
import com.fs.starfarer.api.combat.CombatAssignmentType.AVOID
import com.fs.starfarer.api.combat.CombatAssignmentType.RALLY_TASK_FORCE
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.debug.debugPlugin
import com.genir.aitweaks.debug.drawBattleGroup
import com.genir.aitweaks.utils.extensions.*
import com.genir.aitweaks.utils.targetTracker
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color.BLUE
import java.awt.Color.YELLOW

class ShipTarget : BaseEveryFrameCombatPlugin() {
    private val knownAssignments: MutableSet<AssignmentKey> = mutableSetOf()
    private val advanceInterval = IntervalUtil(0.75f, 1f)

    private var validGroups: List<Set<ShipAPI>> = listOf()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        for (i in validGroups.indices) {
            drawBattleGroup(validGroups[i], if (i == 0) YELLOW else BLUE)
        }

//        val shipsInit = Global.getCombatEngine().ships.filter {
//            when {
//                it.owner != 0 -> false
//                !it.isAlive -> false
//                it.isExpired -> false
//                !it.isBig -> false
//                else -> true
//            }
//        }
//
//        shipsInit.forEach {
////            debugPlugin[it] = "${it.hullSpec.hullId} ${targetTracker[it]}"
//            //${deathBall.contains(targetTracker[it])}
//
//            val t = targetTracker[it]
//            if (t != null)
//                debugVertices.add(Line(it.location, t.location, Color.RED))
//        }

        val engine = Global.getCombatEngine()
        if (engine.isPaused || engine.isSimulation) return

        advanceInterval.advance(dt)
        if (!advanceInterval.intervalElapsed()) return

        val ships = engine.ships.filter {
            when {
                it.owner != 0 -> false
                !it.isAlive -> false
                it.isExpired -> false
                !it.isBig -> false
                else -> true
            }
        }

        // Do not force targets if there's an avoid assignment active.
        val taskManager = engine.getFleetManager(0).getTaskManager(false)
        if (taskManager.allAssignments.firstOrNull { it.type == AVOID } != null) {
            ships.forEach { clearAssignment(it) }
            return
        }

        // Identify enemy battle groups.
        val enemyFleet = engine.ships.filter { it.owner == 1 && it.isValidTarget }
        if (enemyFleet.isEmpty()) return

        val groups = segmentFleet(enemyFleet.toTypedArray())
        val groupsFromLargest = groups.sortedBy { it.dpSum }.reversed()

        validGroups = groupsFromLargest.filter { it.dpSum * 4f >= groupsFromLargest.first().dpSum }

        debugPlugin.clear()
        for (i in groupsFromLargest.indices) {
            debugPlugin[i] = groupsFromLargest[i].dpSum
        }

        val fog = engine.getFogOfWar(0)
        val primaryTargets = validGroups.first().filter { it.isBig && fog.isVisible(it) }
        ships.forEach { manageAssignments(it, primaryTargets) }

        taskManager.clearEmptyWaypoints()
    }

    private fun manageAssignments(ship: ShipAPI, primaryTargets: List<ShipAPI>) {
        clearAssignment(ship)

        // Ship has foreign assignment.
        if (ship.assignment != null) {
            return
        }

        val target = targetTracker[ship] ?: return

        // Ship is engaging or planning to engage primary group.
        if (validGroups.first().contains(target)) return

        // Ship is engaging secondary groups.
        if (validGroups.any { it.contains(target) } && closeToEnemy(ship, target)) return

        // Ship has wrong target. Find the closest valid target in the main enemy battle group.
        val closestTarget = primaryTargets.minByOrNull { (it.location - ship.location).lengthSquared() }
            ?: return

        val fleetManager = Global.getCombatEngine().getFleetManager(ship.owner)
        val waypoint = fleetManager.createWaypoint(closestTarget.location, false)
        val assignment = ship.taskManager.createAssignment(RALLY_TASK_FORCE, waypoint, false)
        ship.taskManager.giveAssignment(ship.deployedFleetMember, assignment, false)

        val key = AssignmentKey(ship, ship.assignment!!.target.location, ship.assignment!!.type)
        knownAssignments.add(key)
    }

    private fun clearAssignment(ship: ShipAPI) {
        if (ship.assignment == null) return

        // Clear old assignment.
        val key = AssignmentKey(ship, ship.assignment!!.target?.location, ship.assignment!!.type)
        if (knownAssignments.contains(key)) {
            ship.taskManager.removeAssignment(ship.assignment)
            knownAssignments.remove(key)
        }
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
