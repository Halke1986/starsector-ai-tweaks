package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.extensions.allGroupedWeapons
import com.genir.aitweaks.core.extensions.rangeFromShipCenter
import com.genir.aitweaks.core.extensions.sumOf
import com.genir.aitweaks.core.extensions.totalCollisionRadius
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection

class ShipStats(private val ship: ShipAPI) {
    val significantWeapons: List<WeaponHandle> = findSignificantWeapons()
    val attackTargetSearchRange: Float = calculateAttackTargetSearchRange()
    val totalCollisionRadius: Float = ship.totalCollisionRadius
    val weaponGroups: List<WeaponGroup> = findWeaponGroups()

    private fun calculateAttackTargetSearchRange(): Float {
        val rangeEnvelope = 1.5f
        val totalMaxRange = significantWeapons.maxOfOrNull { it.slot.rangeFromShipCenter(0f.toDirection, it.engagementRange) } ?: 0f

        return totalMaxRange * rangeEnvelope
    }

    /** Weapons that can be used by the ship to conduct attacks, as opposed to PD, decoratives, etc. */
    private fun findSignificantWeapons(): List<WeaponHandle> {
        val allWeapons: List<WeaponHandle> = ship.allGroupedWeapons.filter { weapon ->
            when {
                weapon.isMissile -> false

                weapon.derivedStats.dps == 0f -> false

                else -> true
            }
        }

        // Filter out PD weapons, but only if there are non PD weapons available.
        val attackWeapons: List<WeaponHandle> = allWeapons.filter {
            when {
                // PD hardpoints are considered attack weapons
                it.slot.isHardpoint -> true

                // PD_ALSO are considered attack weapons
                it.hasAIHint(WeaponAPI.AIHints.PD_ALSO) -> true

                it.isPD -> false

                else -> true
            }
        }.ifEmpty { allWeapons }

        // Return active weapons.
        return attackWeapons.filter { weapon ->
            when {
                weapon.isDisabled -> false

                weapon.isPermanentlyDisabled -> false

                else -> true
            }
        }
    }

    /** A ship can have multiple valid weapon groups. */
    private fun findWeaponGroups(): List<WeaponGroup> {
        // Find dps for each possible firing arc.
        val attackAngles: Map<Direction, Float> = attackAngles(significantWeapons)

        // Assign lower weight to arcs more distant from the ship front.
        val weightedAngles: Map<Direction, Float> = attackAngles.mapValues { (direction, dps) ->
            val weight = if (direction.length <= 90f) 1f else 0.66f
            dps * weight
        }

        // Ignore firing arcs that are close to or fall outside front shield arc.
        val shield = ship.shield
        val validAngles: Map<Direction, Float> = if (shield?.type == ShieldAPI.ShieldType.FRONT) {
            val limit = shield.arc / 3
            weightedAngles.filter { it.key.length < limit }
        } else {
            weightedAngles
        }

        // Find attack angles with acceptable DPS.
        val bestDPS: Float = validAngles.maxWithOrNull(compareBy { it.value })?.value
            ?: return listOf(WeaponGroup(ship, listOf()))
        val validDPSAngles: Map<Direction, Float> = validAngles.filter { it.value >= bestDPS * Preset.weaponGroupPerformanceThreshold }

        // Build weapon groups.
        return validDPSAngles.map { (direction, _) ->
            WeaponGroup(ship, significantWeapons.filter { it.isAngleInArc(direction) })
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
