package com.genir.aitweaks.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatAssignmentType
import com.fs.starfarer.api.combat.CombatAssignmentType.RALLY_TASK_FORCE
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.debug.Line
import com.genir.aitweaks.debug.debugPlugin
import com.genir.aitweaks.debug.debugVertices
import com.genir.aitweaks.utils.extensions.*
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ShipTarget : BaseEveryFrameCombatPlugin() {
    private val knownAssignments: MutableSet<assignmentKey> = mutableSetOf()
    private val advanceInterval = IntervalUtil(0.75f, 1f)

    private var deathball: Set<ShipAPI> = setOf()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        drawBattleGroup(deathball)

        advanceInterval.advance(dt)
        if (!advanceInterval.intervalElapsed()) return

        debugPlugin.clear()

        val targets = Global.getCombatEngine().ships.filter {
            it.owner == 1 && it.isValidTarget
        }

        debugPlugin[0] = knownAssignments.size

        val groups = battlegroups(targets.toTypedArray())

        deathball = groups.maxByOrNull { it.sumOf { ship -> ship.deploymentPoints.toDouble() } } ?: return

        val ships = Global.getCombatEngine().ships.filter {
            when {
                it.owner != 0 -> false
                !it.isAlive -> false
                it.isExpired -> false
                !it.isBig -> false
                else -> true
            }
        }

        ships.forEach {
            debugPlugin[it] = "${it.hullSpec.hullId} ${it.maneuverTarget}"
        }

        val bigTargets = deathball.filter { it.isBig }
        if (bigTargets.isEmpty()) return


        ships.forEach {
            manageAssignments(it, deathball, bigTargets)
        }

        Global.getCombatEngine().getFleetManager(0).getTaskManager(false).clearEmptyWaypoints()
    }

    private fun manageAssignments(ship: ShipAPI, deathBall: Set<ShipAPI>, bigTargets: List<ShipAPI>) {
        val fleetManager = Global.getCombatEngine().getFleetManager(ship.owner)
        val taskManager = fleetManager.getTaskManager(ship.isAlly)

        if (ship.assignment != null) {
            val key = assignmentKey(ship, ship.assignment!!.target.location, ship.assignment!!.type)

            if (knownAssignments.contains(key)) {
                taskManager.removeAssignment(ship.assignment)
                knownAssignments.remove(key)
            } else {
                // Ship has foreign assignment.
                return
            }
        }

        // Ship has wrong target.
        if (!deathBall.contains(ship.maneuverTarget)) {
            // Find the closest valid target in the main enemy battle group.
            val closestTarget = bigTargets.minByOrNull { target -> (target.location - ship.location).lengthSquared() }
                ?: return

            val waypoint = fleetManager.createWaypoint(closestTarget.location, false)
            val assignment = taskManager.createAssignment(RALLY_TASK_FORCE, waypoint, false)
            taskManager.giveAssignment(ship.deployedFleetMember, assignment, false)

            val key = assignmentKey(ship, ship.assignment!!.target.location, ship.assignment!!.type)
            knownAssignments.add(key)
        }
    }


    private fun battlegroups(targets: Array<ShipAPI>): List<Set<ShipAPI>> {
        val maxRange = 2000f

        // Assign targets to battle groups.
        val groups = IntArray(targets.size) { it }
        for (i in targets.indices) {
            for (j in targets.indices) {
                when {
                    // Cannot attach to battle group via frigate.
                    targets[j].isSmall -> continue

                    // Frigate already attached to battle group.
                    targets[i].isSmall && groups[i] != i -> continue

                    // Both targets already in same battle group.
                    groups[i] == groups[j] -> continue

                    // Too large distance between targets to connect.
                    (targets[i].location - targets[j].location).lengthSquared() > maxRange * maxRange -> continue
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
        for (i in targets.indices) {
            if (!sets.contains(groups[i])) sets[groups[i]] = mutableSetOf()

            sets[groups[i]]!!.add(targets[i])
        }

        return sets.values.toList()
    }

    private fun closeToEnemy(ship: ShipAPI, enemies: List<ShipAPI>): Boolean {
        val maxRange = ship.maxFiringRange + 500f
        return enemies.firstOrNull { (it.location - ship.location).length() <= maxRange } != null
    }
}

data class assignmentKey(val ship: ShipAPI, val location: Vector2f, val type: CombatAssignmentType)

private val ShipAPI.isSmall: Boolean
    get() = this.isFighter || (this.isFrigate && !this.isModule)

private val ShipAPI.isBig: Boolean
    get() = this.isCruiser || this.isCapital

val ShipAPI.maxFiringRange: Float
    get() = this.allWeapons.maxOfOrNull { it.range } ?: 0f

fun drawBattleGroup(group: Set<ShipAPI>) {
    val ts = group.toTypedArray()
    val es: MutableList<Edge> = mutableListOf()

    for (i in ts.indices) {
        for (j in i + 1 until ts.size) {
            val w = (ts[i].location - ts[j].location).lengthSquared()
            es.add(Edge(i, j, w))
        }
    }

    kruskal(es, ts.size).forEach {
        debugVertices.add(Line(ts[it.src].location, ts[it.dest].location, Color.YELLOW))
    }
}

data class Edge(val src: Int, val dest: Int, val weight: Float)

fun kruskal(graph: List<Edge>, numVertices: Int): List<Edge> {
    val sortedEdges = graph.sortedBy { it.weight }
    val disjointSet = IntArray(numVertices) { -1 }
    val mst = mutableListOf<Edge>()

    fun find(parents: IntArray, i: Int): Int {
        if (parents[i] < 0) return i
        return find(parents, parents[i]).also { parents[i] = it }
    }

    fun union(parents: IntArray, i: Int, j: Int) {
        val root1 = find(parents, i)
        val root2 = find(parents, j)
        if (root1 != root2) {
            if (parents[root1] < parents[root2]) {
                parents[root1] += parents[root2]
                parents[root2] = root1
            } else {
                parents[root2] += parents[root1]
                parents[root1] = root2
            }
        }
    }

    for (edge in sortedEdges) {
        if (mst.size >= numVertices - 1) break
        if (find(disjointSet, edge.src) != find(disjointSet, edge.dest)) {
            union(disjointSet, edge.src, edge.dest)
            mst.add(edge)
        }
    }
    return mst
}