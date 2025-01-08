package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.angularSize
import com.genir.aitweaks.core.utils.shortestRotation
import com.genir.aitweaks.core.utils.unitVector
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

/**
 * Attack Coordinator assigns attack positions to all ships attacking the
 * same target, so that the ships don't try to crowd in the same spot.
 */
class AttackCoordinator : BaseEveryFrameCombatPlugin() {
    interface Coordinable {
        var proposedHeadingPoint: Vector2f?
        var reviewedHeadingPoint: Vector2f? // will be null is ship requires no coordination
        val ai: CustomShipAI
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val ships = Global.getCombatEngine().ships.asSequence().mapNotNull { it.customShipAI }

        val movements = ships.map { it.movement }
        val burnDrives = ships.mapNotNull { it.systemAI as? Coordinable }

        buildTaskForces(movements).forEach { coordinateUnits(buildFormations(it.value)) }
        buildTaskForces(burnDrives).forEach { coordinateUnits(buildFormations(it.value)) }
    }

    // Divide attacking AIs into task forces attacking the same target.
    private fun buildTaskForces(coordinables: Sequence<Coordinable>): Map<ShipAPI, List<Unit>> {
        val taskForces: MutableMap<ShipAPI, MutableList<Unit>> = mutableMapOf()
        coordinables.forEach { coordinable ->
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

            if (taskForce == null) taskForces[target] = mutableListOf(unit)
            else taskForce.add(unit)
        }

        return taskForces
    }

    private fun buildFormations(taskForce: List<Unit>): List<Formation> {
        return mergeFormations(taskForce.map { Formation(it) }.sortedBy { it.facing })
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

    private fun coordinateUnits(taskForce: List<Formation>) {
        val target = taskForce.first().units.first().target

        // Formations with only one unit do not require coordination.
        val formationsToCoordinate = taskForce.filter { formation -> formation.units.size > 1 }

        formationsToCoordinate.forEach { formation ->
            var facing = formation.facing - formation.angularSize / 2f

            formation.units.sortBy { shortestRotation(formation.facing, it.currentFacing) }

            formation.units.forEach { entity ->
                val angle = facing + entity.angularSize / 2f
                val pos = target.location + unitVector(angle).resized(entity.attackRange)

                facing += entity.angularSize
                entity.coordinable.reviewedHeadingPoint = pos
            }
        }
    }

    private class Formation(initialUnit: Unit) {
        val units: MutableList<Unit> = mutableListOf(initialUnit)

        var angularSize = initialUnit.angularSize
        var facing = initialUnit.proposedFacing

        fun isOverlapping(other: Formation): Boolean {
            val angleToOther = shortestRotation(facing, other.facing)
            return abs(angleToOther) < (angularSize + other.angularSize) / 2f
        }

        fun merge(other: Formation) {
            units.addAll(other.units)

            val newAngularSize = angularSize + other.angularSize
            val angleToOther = shortestRotation(facing, other.facing)

            facing += (angleToOther * other.angularSize) / newAngularSize
            angularSize = newAngularSize
        }
    }

    private class Unit(val target: ShipAPI, proposedHeadingPoint: Vector2f, val coordinable: Coordinable) {
        val ship: ShipAPI = coordinable.ai.ship
        val attackRange: Float = (proposedHeadingPoint - target.location).length
        val angularSize: Float = angularSize(attackRange * attackRange, ship.totalCollisionRadius * 1.4f)
        val proposedFacing: Float = (proposedHeadingPoint - target.location).facing
        val currentFacing: Float = (ship.location - target.location).facing
    }
}
