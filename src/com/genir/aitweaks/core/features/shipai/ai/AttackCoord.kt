package com.genir.aitweaks.core.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.debug.drawCircle
import com.genir.aitweaks.core.debug.drawLine
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
import java.awt.Color
import kotlin.math.abs

class AttackCoord : BaseEveryFrameCombatPlugin() {
    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        if (CustomAIManager().getCustomAIClass() == null) return

        val squads = findSquads().filter { it.value.size > 1 }
        squads.forEach { coordinateSquad(it.value.toMutableSet()) }
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

    private fun coordinateSquad(squad: MutableSet<Group>) {
        while (true) {
            val toBeRemoved = findOverlappingGroup(squad)
            if (toBeRemoved != null) squad.remove(toBeRemoved)
            else break
        }

        val target = squad.first().ships.first().ai.maneuverTarget!!

        squad.forEach { group ->
            var facing = group.facing - group.angularSize / 2f

            group.ships.sortBy { it.currentFacing }



            group.ships.forEach { ship ->
                val dist = (ship.proposedHeadingPoint - target.location).length()

                val angle = facing + ship.angularSize / 2f

//                debugPlugin[ship.ship] = "$dist $r ${ship.angularSize}"

                val pos = target.location + unitVector(angle).resized(dist)

//                val pos = ship.headingPoint
//                drawLine(ship.ship.location, pos, Color.YELLOW)

                facing += ship.angularSize


//                drawCircle(pos, r, Color.CYAN)
                drawCircle(pos, ship.ship.collisionRadius, Color.CYAN)
                drawLine(ship.ship.location, pos, Color.YELLOW)
                drawLine(ship.ship.location, ship.proposedHeadingPoint, Color.BLUE)

                ship.ai.reviewedHeadingPoint = pos
            }
        }
    }

    private fun findOverlappingGroup(squad: Set<Group>): Group? {
        squad.forEach { a ->
            squad.forEach { b ->
                if (a != b) {
                    val dist = abs(MathUtils.getShortestRotation(a.facing, b.facing))
                    if (dist < (a.angularSize + b.angularSize) / 2f) {
                        a.merge(b)
                        return b
                    }
                }
            }
        }

        return null
    }

    private class Group(initialShip: Ship) {
        val ships: MutableList<Ship> = mutableListOf(initialShip)

        var angularSize = initialShip.angularSize
        var facing = initialShip.proposedFacing

        fun merge(other: Group) {
            ships.addAll(other.ships)
            facing = (facing * angularSize + other.facing * other.angularSize) / (angularSize + other.angularSize)
            angularSize += other.angularSize
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