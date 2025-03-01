package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import kotlin.math.max

class ShipStats(private val ship: ShipAPI) {
    val significantWeapons: List<WeaponAPI> = findSignificantWeapons()
    val threatSearchRange: Float = calculateThreatSearchRange()
    val totalCollisionRadius: Float = ship.totalCollisionRadius
    val weaponGroups: List<WeaponGroup> = findWeaponGroups()

    private fun calculateThreatSearchRange(): Float {
        val rangeEnvelope = 1.5f
        val totalMaxRange = significantWeapons.maxOfOrNull { it.rangeFromShipCenter(Direction(0f)) } ?: 0f

        return max(Preset.threatSearchRange, totalMaxRange * rangeEnvelope)
    }

    /** Weapons that can be used by the ship to conduct attacks, as opposed to PD, decoratives, etc. */
    private fun findSignificantWeapons(): List<WeaponAPI> {
        val weapons = ship.allGroupedWeapons.filter { weapon ->
            when {
                weapon.type == WeaponAPI.WeaponType.MISSILE -> false
                weapon.derivedStats.dps == 0f -> false
                else -> true
            }
        }

        // Filter out PD weapons, but only if there are non PD weapons available.
        val attackWeapons = weapons.filter { !it.isPD || it.slot.isHardpoint }.ifEmpty { weapons }

        // Return active weapons.
        return attackWeapons.filter { weapon ->
            when {
                weapon.isDisabled -> false
                weapon.isPermanentlyDisabled -> false
                weapon.isInLongReload -> false
                else -> true
            }
        }
    }

    /** A ship can have multiple valid weapon groups. */
    private fun findWeaponGroups(): List<WeaponGroup> {
        when {
            significantWeapons.isEmpty() -> return listOf(WeaponGroup(ship, listOf()))

            // Special case for front facing hardpoints.
            significantWeapons.any { it.slot.isHardpoint && it.arcFacing.direction.isZero } -> {
                listOf(WeaponGroup(ship, significantWeapons.filter { it.isFrontFacing }))
            }
        }

        // Find dps for each possible firing arc.
        val attackAngles: Map<Direction, Float> = attackAngles(significantWeapons)

        // Assign lower weight to arcs more distant from the ship front.
        val weightedAngles = attackAngles.mapValues { (direction, dps) ->
            val weight = if (direction.length <= 90f) 1f else 0.5f
            dps * weight
        }

        // Ignore firing arcs that are close to or fall outside front shield arc.
        val shield = ship.shield
        val validAngles = if (shield?.type == ShieldAPI.ShieldType.FRONT) {
            val limit = shield.arc / 3
            weightedAngles.filter { it.key.length < limit }
        } else {
            weightedAngles
        }

        // Find all weapon groups with acceptable DPS.
        val bestWeaponGroup = validAngles.maxWithOrNull(compareBy { it.value }) ?: return listOf()
        val validWeaponGroups = validAngles.filter { it.value >= bestWeaponGroup.value * Preset.validWeaponGroupDPSThreshold }

        return validWeaponGroups.map { (angle, _) ->
            WeaponGroup(ship, significantWeapons.filter { it.isAngleInArc(angle) })
        }
    }

    /** Find firing arc angles closest to ship front for each weapon,
     * or 0f for front facing weapons. Associate the angles with total
     * DPS for the provided weapon list.*/
    private fun attackAngles(weapons: List<WeaponAPI>): Map<Direction, Float> {
        val angles: List<Direction> = weapons.flatMap { weapon ->
            val facing: Direction = weapon.arcFacing.direction
            val arc = weapon.arc

            when {
                // Assume hardpoints have no arc at all.
                weapon.slot.isHardpoint -> listOf(facing)

                // Ship front is within weapon arc.
                facing.length < arc / 2f -> listOf(Direction(0f))

                // Ship back is within weapon arc, return both angles.
                180f - facing.length < arc / 2f -> listOf(facing - arc / 2f, facing + arc / 2f)

                // Return weapon arc boundary closer to ship front.
                else -> listOf(facing - facing.sign * (arc / 2f))
            }
        }

        return angles.toSet().associateWith { angle ->
            weapons.filter { it.isAngleInArc(angle) }.sumOf { it.effectiveDPS }
        }
    }
}
