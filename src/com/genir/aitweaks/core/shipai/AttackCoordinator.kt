package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.angularSize
import org.lwjgl.util.vector.Vector2f

/**
 * Attack Coordinator assigns attack positions to all ships attacking the
 * same target, so that the ships don't try to crowd in the same spot.
 */
class AttackCoordinator : BaseEveryFrameCombatPlugin() {
    interface Coordinatable {
        var proposedHeadingPoint: Vector2f?
        var reviewedHeadingPoint: Vector2f? // will be null is ship requires no coordination
        val ai: CustomShipAI
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        // Running when paused would lead to de-synchronization
        // with ship AI. In the first frame after un-pausing,
        // ship AI would observe null reviewedHeadingPoint.
        if (Global.getCombatEngine().isPaused) {
            return
        }

        val ships = Global.getCombatEngine().ships.asSequence()
        val coordinatables = ships.mapNotNull { it.customShipAI?.movement }

        val taskForces: Collection<List<Unit>> = buildTaskForces(coordinatables)
        val structuredTaskForces: Collection<List<Formation>> = taskForces.map { buildFormations(it) }

        structuredTaskForces.forEach { coordinateTaskForce(it) }
    }

    // Divide attacking coordinatables into task forces attacking the same target.
    private fun buildTaskForces(coordinatables: Sequence<Coordinatable>): Collection<List<Unit>> {
        val taskForces: MutableMap<ShipAPI, MutableList<Unit>> = mutableMapOf()

        coordinatables.forEach { coordinable ->
            val target = coordinable.ai.maneuverTarget
            val proposedHeadingPoint = coordinable.proposedHeadingPoint

            // Clean previous frame results.
            coordinable.reviewedHeadingPoint = null
            coordinable.proposedHeadingPoint = null

            if (target == null || proposedHeadingPoint == null) {
                return@forEach
            }

            val unit = Unit(target, proposedHeadingPoint, coordinable)
            val taskForce = taskForces[target]

            if (taskForce == null) {
                taskForces[target] = mutableListOf(unit)
            } else {
                taskForce.add(unit)
            }
        }

        return taskForces.values
    }

    private fun buildFormations(taskForce: List<Unit>): List<Formation> {
        return mergeFormations(taskForce.map { Formation(it) }.sortedBy { it.facing.degrees })
    }

    private fun mergeFormations(l: List<Formation>): List<Formation> {
        if (l.size == 1) return l

        val l2: MutableList<Formation> = mutableListOf(l.first())
        for (i in 1 until l.size) {
            if (l2.last().isOverlapping(l[i])) l2.last().merge(l[i])
            else l2.add(l[i])
        }

        if (l2.size == 1) return l2
        if (l2.first().isOverlapping(l2.last())) {
            l2.first().merge(l2.last())
            l2.removeLast()
        }

        return if (l.size == l2.size) l2
        else mergeFormations(l2)
    }

    private fun coordinateTaskForce(taskForce: List<Formation>) {
        // Formations with only one unit do not require coordination.
        val formationsToCoordinate = taskForce.filter { formation -> formation.units.size > 1 }

        formationsToCoordinate.forEach { coordinateFormation(it) }
    }

    private fun coordinateFormation(formation: Formation) {
        val target: ShipAPI = formation.units.first().target
        var facingOffset: Float = -formation.angularSize / 2f

        formation.units.sortBy { (it.currentFacing - formation.facing).degrees }

        formation.units.forEach { entity ->
            // Cap the angle offset, so that very large formations don't wrap
            // around the target, resulting in crossing heading vectors.
            val cappedOffset = (facingOffset + entity.angularSize / 2f).coerceIn(-130f, 130f)
            val angle = formation.facing + cappedOffset
            val pos = target.location + angle.unitVector.resized(entity.attackRange)

            facingOffset += entity.angularSize
            entity.coordinable.reviewedHeadingPoint = pos
        }
    }

    private class Formation(initialUnit: Unit) {
        val units: MutableList<Unit> = mutableListOf(initialUnit)

        var angularSize = initialUnit.angularSize
        var facing = initialUnit.proposedFacing

        fun isOverlapping(other: Formation): Boolean {
            val angleToOther: Direction = other.facing - facing
            return angleToOther.length < (angularSize + other.angularSize) / 2f
        }

        fun merge(other: Formation) {
            units.addAll(other.units)

            val newAngularSize: Float = angularSize + other.angularSize
            val angleToOther: Direction = other.facing - facing

            facing += angleToOther * (other.angularSize / newAngularSize)
            angularSize = newAngularSize
        }
    }

    private class Unit(val target: ShipAPI, proposedHeadingPoint: Vector2f, val coordinable: Coordinatable) {
        val ship: ShipAPI = coordinable.ai.ship
        val attackRange: Float = (proposedHeadingPoint - target.location).length
        val angularSize: Float = angularSize(attackRange * attackRange, ship.totalCollisionRadius * 1.6f)
        val proposedFacing: Direction = (proposedHeadingPoint - target.location).facing
        val currentFacing: Direction = (ship.location - target.location).facing
    }
}
