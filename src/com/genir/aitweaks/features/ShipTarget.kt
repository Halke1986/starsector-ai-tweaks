package com.genir.aitweaks.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatAssignmentType
import com.fs.starfarer.api.combat.CombatAssignmentType.RALLY_TASK_FORCE
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.debug.drawBattleGroup
import com.genir.aitweaks.utils.extensions.*
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

class ShipTarget : BaseEveryFrameCombatPlugin() {
    private val knownAssignments: MutableSet<assignmentKey> = mutableSetOf()
    private val advanceInterval = IntervalUtil(0.75f, 1f)

    private var deathball: Set<ShipAPI> = setOf()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        drawBattleGroup(deathball)

        advanceInterval.advance(dt)
        if (!advanceInterval.intervalElapsed()) return

        // Identify enemy deathball.
        val enemyFleet = Global.getCombatEngine().ships.filter { it.owner == 1 && it.isValidTarget }
        val groups = segmentFleet(enemyFleet.toTypedArray())
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

        val fog = Global.getCombatEngine().getFogOfWar(0)
        val bigTargets = deathball.filter { it.isBig && fog.isVisible(it) }
        ships.forEach { manageAssignments(it, deathball, bigTargets) }

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
        if (ship.maneuverTarget != null && !deathBall.contains(ship.maneuverTarget)) {
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

    private fun closeToEnemy(ship: ShipAPI, enemies: List<ShipAPI>): Boolean {
        val maxRange = ship.maxFiringRange + 500f
        return enemies.firstOrNull { (it.location - ship.location).length() <= maxRange } != null
    }
}

data class assignmentKey(val ship: ShipAPI, val location: Vector2f, val type: CombatAssignmentType)

val ShipAPI.maxFiringRange: Float
    get() = this.allWeapons.maxOfOrNull { it.range } ?: 0f

private val ShipAPI.isSmall: Boolean
    get() = this.isFighter || (this.isFrigate && !this.isModule)

private val ShipAPI.isBig: Boolean
    get() = this.isCruiser || this.isCapital
