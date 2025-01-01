package com.genir.aitweaks.core

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.defaultAIInterval

/** Segment enemy fleet into separate battle groups. */
class FleetSegmentation(private val side: Int) : BaseEveryFrameCombatPlugin() {
    private val enemy: Int = side xor 1

    private var validGroups: List<Set<ShipAPI>> = listOf()
    var primaryBigTargets: List<ShipAPI> = listOf()
    var primaryTargets: List<ShipAPI> = listOf()
    var allBigTargets: List<ShipAPI> = listOf()
    var allTargets: List<ShipAPI> = listOf()

    private val advanceInterval = defaultAIInterval()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val engine = Global.getCombatEngine()
        if (engine.isSimulation || engine.isPaused) {
            return
        }

        advanceInterval.advance(dt)
        if (advanceInterval.intervalElapsed()) {
            identifyBattleGroups()
        }
    }

    private fun identifyBattleGroups() {
        val engine = Global.getCombatEngine()
        val enemyFleet = engine.ships.filter { it.owner == enemy && it.isValidTarget && !it.isFighter }
        if (enemyFleet.isEmpty()) return

        val groups = segmentFleet(enemyFleet.toTypedArray())
        val groupsFromLargest = groups.sortedBy { -it.dpSum }
        val largestGroupDP = groupsFromLargest.first().dpSum

        validGroups = groupsFromLargest.filter { isValidGroup(it, largestGroupDP) }
        primaryTargets = validGroups.firstOrNull()?.toList() ?: listOf()
        primaryBigTargets = primaryTargets.filter { it.isBig }
        allTargets = validGroups.flatten()
        allBigTargets = allTargets.filter { it.isBig }
    }

    /** Divide fleet into separate battle groups. */
    private fun segmentFleet(fleet: Array<ShipAPI>): List<Set<ShipAPI>> {
        val maxRange = 2000f

        // Assign targets to battle groups.
        val groups = IntArray(fleet.size) { it }
        for (i in fleet.indices) {
            for (j in fleet.indices) {
                when {
                    // Cannot attach to battle group via frigate.
                    fleet[j].root.isFrigate -> continue

                    // Frigate already attached to battle group.
                    fleet[i].root.isFrigate && groups[i] != i -> continue

                    // Both targets already in same battle group.
                    groups[i] == groups[j] -> continue

                    // Too large distance between targets to connect.
                    (fleet[i].location - fleet[j].location).lengthSquared > maxRange * maxRange -> continue
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

    private fun isValidGroup(group: Set<ShipAPI>, largestGroupDP: Float): Boolean {
        return group.any { ship: ShipAPI -> ship.root.isCapital } || (group.dpSum * 4f >= largestGroupDP)
    }

    private val Set<ShipAPI>.dpSum: Float
        get() = sumOf { it.deploymentPoints }
}
