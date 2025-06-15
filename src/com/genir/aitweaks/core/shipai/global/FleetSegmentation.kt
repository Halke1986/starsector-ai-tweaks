package com.genir.aitweaks.core.shipai.global

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.defaultAIInterval

/** Segment enemy fleet into separate battle groups. */
class FleetSegmentation(side: Int) : BaseEveryFrameCombatPlugin() {
    private val enemy: Int = side xor 1

    private var primaryTargets: List<ShipAPI> = listOf()
    private var allTargets: List<ShipAPI> = listOf()

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

    fun primaryTargets(includeSmallTargets: Boolean = true): Sequence<ShipAPI> {
        return primaryTargets.asSequence().filter { it.isValidTarget && (includeSmallTargets || it.isBig) }
    }

    fun allTargets(includeSmallTargets: Boolean = true): Sequence<ShipAPI> {
        return allTargets.asSequence().filter { it.isValidTarget && (includeSmallTargets || it.isBig) }
    }

    private fun identifyBattleGroups() {
        val engine = Global.getCombatEngine()
        val enemyFleet = engine.ships.filter { it.owner == enemy && it.isValidTarget && !it.isFighter }
        if (enemyFleet.isEmpty()) {
            return
        }

        val groups = segmentFleet(enemyFleet.toTypedArray())
        val groupsFromLargest = groups.sortedBy { -it.dpSum }
        val largestGroupDP = groupsFromLargest.first().dpSum

        val validGroups = groupsFromLargest.filter { isValidGroup(it, largestGroupDP) }
        primaryTargets = validGroups.firstOrNull() ?: listOf()
        allTargets = validGroups.flatten()
    }

    /** Divide fleet into separate battle groups. */
    private fun segmentFleet(fleet: Array<ShipAPI>): List<List<ShipAPI>> {
        val maxRange = 2000f

        // Assign targets to battle groups.
        val groups = IntArray(fleet.size) { it }
        for (i in fleet.indices) {
            for (j in fleet.indices) {
                when {
                    // Cannot attach to battle group via a frigate.
                    fleet[j].root.isFrigate -> continue

                    // Frigate already attached to battle group.
                    fleet[i].root.isFrigate && groups[i] != i -> continue

                    // Both targets already in the same battle group.
                    groups[i] == groups[j] -> continue

                    // Too large distance between targets to connect.
                    (fleet[i].location - fleet[j].location).lengthSquared > maxRange * maxRange -> continue
                }

                // Merge battle groups.
                val toMerge = groups[i]
                for (k in groups.indices) {
                    if (groups[k] == toMerge) {
                        groups[k] = groups[j]
                    }
                }
            }
        }

        // Build battle groups.
        val lists: MutableMap<Int, MutableList<ShipAPI>> = mutableMapOf()
        for (i in fleet.indices) {
            val group = groups[i]
            if (group !in lists) {
                lists[group] = mutableListOf()
            }

            lists[group]!!.add(fleet[i])
        }

        return lists.values.toList()
    }

    private fun isValidGroup(group: List<ShipAPI>, largestGroupDP: Float): Boolean {
        return group.any { ship: ShipAPI -> ship.root.isCapital } || (group.dpSum * 4f >= largestGroupDP)
    }

    private val List<ShipAPI>.dpSum: Float
        get() = sumOf { it.deploymentPoints }
}