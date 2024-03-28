package com.genir.aitweaks.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize.*
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.debug.Line
import com.genir.aitweaks.debug.debugPlugin
import com.genir.aitweaks.debug.debugVertices
import com.genir.aitweaks.utils.extensions.deploymentPoints
import com.genir.aitweaks.utils.extensions.isModule
import com.genir.aitweaks.utils.extensions.isValidTarget
import org.lazywizard.lazylib.ext.minus
import java.awt.Color

class ShipTarget : BaseEveryFrameCombatPlugin() {
    private val assignments: MutableMap<DeployedFleetMemberAPI, DeployedFleetMemberAPI> = mutableMapOf()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {

        val targets = Global.getCombatEngine().ships.filter {
            when {
                it.owner != 1 -> false
                !it.isValidTarget -> false
                it.isModule -> false
                it.hullSize != CRUISER && it.hullSize != CAPITAL_SHIP && it.hullSize != DESTROYER -> false
                it.deploymentPoints == 0f -> false
                else -> true
            }
        }

        val allTargets = Global.getCombatEngine().ships.filter {
            when {
                it.owner != 1 -> false
                !it.isValidTarget -> false
                it.deploymentPoints == 0f -> false
                else -> true
            }
        }

//        span2(targets)

        val groups = battlegroups(allTargets.toTypedArray())

        debugPlugin.clear()

        groups.forEach {
            debugPlugin[it] = it.size
            drawBattleGroup(it)
        }

//        centralTarget(1)
//        val bigShips = Global.getCombatEngine().ships.filter {
//            when {
//                !it.isAlive -> false
//                it.isExpired -> false
////                it.isVastBulk -> false
//                it.hullSize != CRUISER && it.hullSize != CAPITAL_SHIP && !it.isModule -> false
//                else -> true
//            }
//        }
//
//        val enemyForces: Map<Int, List<ShipAPI>> = mapOf(
//            0 to bigShips.filter { it.owner == 1 },
//            1 to bigShips.filter { it.owner == 0 },
//        )
//
//        val hasWrongTarget = bigShips.filter {
//            when {
//                it.assignment != null -> false
//                !it.hasAIType<BasicShipAI>() -> false
//                closeToEnemy(it, enemyForces[it.owner]!!) -> false
//
//                enemyForces[it.owner]!!.isEmpty() -> false
//                enemyForces[it.owner]!!.contains(it.maneuverTarget) -> false
//
//                else -> true
//            }
//        }
//
//        debugPlugin.clear()
//
//        hasWrongTarget.forEach { ship ->
//
//
//            val target = enemyForces[ship.owner]!!.minByOrNull { (it.location - ship.location).length() }
//
//            val engine = Global.getCombatEngine()
//            val targetWrapper = engine.getFleetManager(ship.owner xor 1).getDeployedFleetMember(target)
//            val shipWrapper = engine.getFleetManager(ship.owner).getDeployedFleetMember(ship)
//
//            if (!assignments.contains(shipWrapper)) {
//
//                val taskManager = engine.getFleetManager(ship.owner).getTaskManager(ship.isAlly)
//                val assignment = taskManager.createAssignment(CombatAssignmentType.INTERCEPT, targetWrapper, false)
//                taskManager.giveAssignment(shipWrapper, assignment, false)
//
//                assignments[shipWrapper] = targetWrapper
//            }
//
//            debugPlugin[ship] = "${ship.hullSpec.hullId} ${target}"
//        }

//        bigShips.filter { it.owner == 0 }.forEach { debugPlugin[it] = "${it.hullSpec.hullId} ${it.assignment?.type}" }


    }

//    private fun centralTarget(targets: List<ShipAPI>): ShipAPI? {
//
//
//        val barycenter = targets.fold(Pair(Vector2f(), 0f)) { (s, w), ship ->
//            val dp = ship.deploymentPoints; Pair(s + (ship.location * dp), w + dp)
//        }.let { (s, w) -> s / w }
//
//        val ps = Global.getCombatEngine().playerShip ?: return null
//
//        debugVertices.add(Line(ps.location, barycenter, Color.YELLOW))
//
//
//        return null
//    }

//    private fun span(targets: List<ShipAPI>) {
//        val ts = targets.toTypedArray()
//        val sets = IntArray(ts.size) { it }
//
//
//        val es: MutableList<Edge> = mutableListOf()
//
//        val maxRange = 1500f
//
//        for (i in ts.indices) {
//            for (j in i + 1 until ts.size) {
//                val w = (ts[i].location - ts[j].location).lengthSquared()
//                if (w <= maxRange * maxRange) {
//                    es.add(Edge(i, j, w))
//                }
//            }
//        }
//
//        kruskal(es, ts.size).forEach {
//            debugVertices.add(Line(ts[it.src].location, ts[it.dest].location, Color.YELLOW))
//        }
//    }

//    private fun span2(targets: List<ShipAPI>) {
//        val ts = targets.toTypedArray()
//        val sets = IntArray(ts.size) { it }
//
//        val maxRange = 2000f
//
//        for (i in ts.indices) {
//            for (j in i + 1 until ts.size) {
//                if (sets[i] == sets[j]) continue
//
//                val w = (ts[i].location - ts[j].location).lengthSquared()
//                if (w > maxRange * maxRange) continue
//
//                for (k in sets.indices) {
//                    if (sets[k] == sets[j])
//                        sets[k] = i
//                }
//            }
//        }
//
//        val maps: MutableMap<Int, MutableList<ShipAPI>> = mutableMapOf()
//
//        for (i in ts.indices) {
//            if (!maps.contains(sets[i]))
//                maps[sets[i]] = mutableListOf()
//
//            maps[sets[i]]!!.add(ts[i])
//        }
//
//        debugPlugin.clear()
//
//        maps.forEach {
//            debugPlugin[it.key] = it.value.size
//
//            drawBattleGroup(it.value)
//        }
//    }

    private fun battlegroups(targets: Array<ShipAPI>): List<List<ShipAPI>> {
        val maxRange = 2000f

        // Assign targets to battle groups.
        val groups = IntArray(targets.size) { it }
        for (i in targets.indices) {
            for (j in targets.indices) {
                when {
                    // Cannot attach to battle group via frigate.
                    targets[j].isFrigate -> continue

                    // Frigate already attached to battle group.
                    targets[i].isFrigate && groups[i] != i -> continue

                    // Both targets already in same battle group.
                    groups[i] == groups[j] -> continue

                    // Too large distance between targets to connect.
                    (targets[i].location - targets[j].location).lengthSquared() > maxRange * maxRange -> continue
                }

                // Merge battle groups.
                val toMerge = groups[i]
                for (k in groups.indices) {
                    if (groups[k] == toMerge)
                        groups[k] = groups[j]
                }
            }
        }

        // Build battle groups.
        val sets: MutableMap<Int, MutableList<ShipAPI>> = mutableMapOf()
        for (i in targets.indices) {
            if (!sets.contains(groups[i]))
                sets[groups[i]] = mutableListOf()

            sets[groups[i]]!!.add(targets[i])
        }

        return sets.values.toList()
    }

    private fun closeToEnemy(ship: ShipAPI, enemies: List<ShipAPI>): Boolean {
        val maxRange = ship.maxFiringRange + 500f
        return enemies.firstOrNull { (it.location - ship.location).length() <= maxRange } != null
    }
}

fun drawBattleGroup(group: List<ShipAPI>) {
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

val ShipAPI.maxFiringRange: Float
    get() = this.allWeapons.maxOfOrNull { it.range } ?: 0f

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