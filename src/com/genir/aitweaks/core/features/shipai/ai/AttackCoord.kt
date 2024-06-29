package com.genir.aitweaks.core.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.features.shipai.CustomAIManager
import com.genir.aitweaks.core.features.shipai.ai.Preset.Companion.collisionBuffer
import com.genir.aitweaks.core.utils.aitStash
import com.genir.aitweaks.core.utils.angularSize
import com.genir.aitweaks.core.utils.extensions.hasAIType
import com.genir.aitweaks.core.utils.extensions.resized
import com.genir.aitweaks.core.utils.unitVector
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

/**
 * Attack Coordinator assigns attack positions to all ships attacking the
 * same target, so that the ships don't try to crowd in the same spot.
 */
class AttackCoord : BaseEveryFrameCombatPlugin() {
    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        if (CustomAIManager().getCustomAIClass() == null) return

        val squads = findSquads().filter { it.value.size > 1 }
        squads.forEach { coordinateSquad(mergeSquad(it.value)) }
    }

    // Divide attacking AIs into squads attacking the same target.
    private fun findSquads(): Map<ShipAPI, List<Group>> {
        val customAI = CustomAIManager().getCustomAIClass()
        val ships = Global.getCombatEngine().ships.asSequence()
        val ais = ships.filter { it.hasAIType(customAI) }.mapNotNull { it.aitStash.maneuverAI }

        val squads: MutableMap<ShipAPI, MutableList<Group>> = mutableMapOf()
        ais.forEach { attackerAI ->
            val target = attackerAI.maneuverTarget ?: return@forEach
            val proposedHeadingPoint = attackerAI.proposedHeadingPoint ?: return@forEach

            val ship = Ship(target, proposedHeadingPoint, attackerAI)

            val squad = squads[target]
            val group = Group(ship)

            if (squad == null) squads[target] = mutableListOf(group)
            else squad.add(group)
        }

        return squads
    }

    private fun coordinateSquad(squad: List<Group>) {
        val target = squad.first().ships.first().ai.maneuverTarget!!

        squad.forEach { group ->
            var facing = group.facing - group.angularSize / 2f

            group.ships.sortBy { MathUtils.getShortestRotation(group.facing, it.currentFacing) }

            group.ships.forEach { ship ->
                val dist = (ship.proposedHeadingPoint - target.location).length()

                val angle = facing + ship.angularSize / 2f
                val pos = target.location + unitVector(angle).resized(dist)

                facing += ship.angularSize
                ship.ai.reviewedHeadingPoint = pos
            }
        }
    }

    private fun mergeSquad(squad: List<Group>): List<Group> {
        return mergeGroups(squad.sortedBy { it.facing })
    }

    private fun mergeGroups(l: List<Group>): List<Group> {
        if (l.size == 1) return l

        val l2: MutableList<Group> = mutableListOf(l.first())
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
        else mergeGroups(l2)
    }

    private class Group(initialShip: Ship) {
        val ships: MutableList<Ship> = mutableListOf(initialShip)

        var angularSize = initialShip.angularSize
        var facing = initialShip.proposedFacing

        fun isOverlapping(other: Group): Boolean {
            val angleToOther = MathUtils.getShortestRotation(facing, other.facing)
            return abs(angleToOther) < (angularSize + other.angularSize) / 2f
        }

        fun merge(other: Group) {
            ships.addAll(other.ships)

            val newAngularSize = angularSize + other.angularSize
            val angleToOther = MathUtils.getShortestRotation(facing, other.facing)

            facing += (angleToOther * other.angularSize) / newAngularSize
            angularSize = newAngularSize
        }
    }

    private class Ship(target: ShipAPI, val proposedHeadingPoint: Vector2f, val ai: Maneuver) {
        val angularSize: Float
        val proposedFacing: Float
        val ship = ai.ship
        val currentFacing: Float = (ship.location - target.location).getFacing()

        init {
            val toShip = proposedHeadingPoint - target.location
            val distanceSqr = toShip.lengthSquared()

            angularSize = angularSize(distanceSqr, ship.totalCollisionRadius + collisionBuffer)
            proposedFacing = toShip.getFacing()
        }
    }
}