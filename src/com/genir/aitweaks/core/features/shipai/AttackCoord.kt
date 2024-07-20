package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.combat.combatState
import com.genir.aitweaks.core.features.shipai.Preset.Companion.collisionBuffer
import com.genir.aitweaks.core.utils.angularSize
import com.genir.aitweaks.core.utils.extensions.resized
import com.genir.aitweaks.core.utils.unitVector
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

interface Coordinable {
    var proposedHeadingPoint: Vector2f?
    var reviewedHeadingPoint: Vector2f?
    val ai: AI
}

/**
 * Attack Coordinator assigns attack positions to all ships attacking the
 * same target, so that the ships don't try to crowd in the same spot.
 */
class AttackCoord : BaseEveryFrameCombatPlugin() {
    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        if (!combatState().customAIManager.customAIEnabled) return

        val ships = Global.getCombatEngine().ships.asSequence()
        val movements = ships.mapNotNull { it.customAI?.movement }
        val burnDrives = ships.mapNotNull { it.customAI?.burnDriveAI }

        buildTaskForces(movements).forEach { coordinateUnits(buildFormations(it.value)) }
        buildTaskForces(burnDrives).forEach { coordinateUnits(buildFormations(it.value)) }
    }

    // Divide attacking AIs into task forces attacking the same target.
    private fun buildTaskForces(coordinables: Sequence<Coordinable>): Map<ShipAPI, List<Unit>> {
        val taskForces: MutableMap<ShipAPI, MutableList<Unit>> = mutableMapOf()
        coordinables.forEach { coordinable ->
            val target = coordinable.ai.maneuverTarget ?: return@forEach
            val proposedHeadingPoint = coordinable.proposedHeadingPoint ?: return@forEach

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

        taskForce.forEach { formation ->
            var facing = formation.facing - formation.angularSize / 2f

            formation.units.sortBy { MathUtils.getShortestRotation(formation.facing, it.currentFacing) }

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
            val angleToOther = MathUtils.getShortestRotation(facing, other.facing)
            return abs(angleToOther) < (angularSize + other.angularSize) / 2f
        }

        fun merge(other: Formation) {
            units.addAll(other.units)

            val newAngularSize = angularSize + other.angularSize
            val angleToOther = MathUtils.getShortestRotation(facing, other.facing)

            facing += (angleToOther * other.angularSize) / newAngularSize
            angularSize = newAngularSize
        }
    }

    private class Unit(val target: ShipAPI, proposedHeadingPoint: Vector2f, val coordinable: Coordinable) {
        val ship: ShipAPI = coordinable.ai.ship
        val attackRange: Float = (proposedHeadingPoint - target.location).length()
        val angularSize: Float = angularSize(attackRange * attackRange, ship.totalCollisionRadius + collisionBuffer)
        val proposedFacing: Float = (proposedHeadingPoint - target.location).getFacing()
        val currentFacing: Float = (ship.location - target.location).getFacing()
    }
}
