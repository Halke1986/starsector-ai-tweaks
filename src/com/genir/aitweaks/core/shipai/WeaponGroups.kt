package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.sumOf
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection

object WeaponGroups {
    /** Divide weapons into groups that can fire along common angle. */
    fun findGroups(ship: ShipAPI, weapons: List<WeaponHandle>): List<WeaponGroup> {
        // Find dps for each possible attack angles.
        val attackAngles: Map<Direction, Float> = attackAngles(weapons)

        // Assign lower weight to angles more distant from the ship front.
        val weightedAngles: Map<Direction, Float> = attackAngles.mapValues { (direction, dps) ->
            val weight = if (direction.length <= 90f) 1f else 0.66f
            dps * weight
        }

        // Ignore firing arcs that are close to or fall outside front shield arc.
        val shield = ship.shield
        val validAngles: Map<Direction, Float> = if (shield?.type == ShieldAPI.ShieldType.FRONT) {
            val limit: Float = (shield.arc / 3).coerceAtLeast(20f)
            weightedAngles.filter { it.key.length < limit }
        } else {
            weightedAngles
        }

        // Find attack angles with acceptable DPS.
        val bestDPS: Float = validAngles.maxWithOrNull(compareBy { it.value })?.value
            ?: return listOf(WeaponGroup(ship, listOf(), 0f.toDirection))
        val validDPSAngles: Map<Direction, Float> = validAngles.filter { it.value >= bestDPS * Preset.weaponGroupPerformanceThreshold }

        // Build weapon groups.
        return validDPSAngles.map { (direction, _) ->
            WeaponGroup(ship, weapons.filter { it.isAngleInArc(direction) }, direction)
        }
    }

    /** Find firing arc angles closest to ship front for each weapon,
     * or 0f for front facing weapons. Associate the angles with total
     * DPS for the provided weapon list. */
    private fun attackAngles(weapons: List<WeaponHandle>): Map<Direction, Float> {
        val angles: List<Direction> = weapons.flatMap { weapon ->
            val facing: Direction = weapon.arcFacing.toDirection
            val halfArc = weapon.arc.halfAngle

            when {
                // Assume hardpoints have no arc at all.
                weapon.slot.isHardpoint -> listOf(facing)

                // Ship front is within weapon arc.
                facing.length < halfArc -> listOf(0f.toDirection)

                // Ship back is within weapon arc, return both angles.
                180f - facing.length < halfArc -> listOf(facing - halfArc.toDirection, facing + halfArc.toDirection)

                // Return weapon arc boundary closer to ship front.
                else -> listOf(facing - halfArc.toDirection * facing.sign)
            }
        }

        return angles.toSet().associateWith { angle ->
            weapons.filter { it.isAngleInArc(angle) }.sumOf { it.estimatedDPS }
        }
    }

    /**
     * Estimates DPS when selecting the active weapon group.
     *
     * Weapons in a long reload are treated as having 0 DPS so the ship can
     * prioritize other groups. They still qualify as significant (unlike
     * disabled weapons) because many can fire partial shots mid-reload and
     * thus influence the calculated attack range.
     */
    private val WeaponHandle.estimatedDPS: Float
        get() = when {
            isInLongReload -> 0f

            else -> effectiveDPS
        }
}
