package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.extensions.isAngleInArc
import com.genir.aitweaks.core.utils.extensions.isInFiringSequence
import com.genir.aitweaks.core.utils.extensions.isPDSpec
import com.genir.aitweaks.core.utils.extensions.sumOf
import com.genir.aitweaks.core.utils.shortestRotation
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

class ShipStats(private val ship: ShipAPI) {
    val significantWeapons: List<WeaponAPI> = findSignificantWeapons()
    val threatSearchRange: Float = calculateThreatSearchRange()
    val totalCollisionRadius: Float = ship.totalCollisionRadius
    val weaponGroups: List<WeaponGroup> = findWeaponGroups()

    private fun calculateThreatSearchRange(): Float {
        val rangeEnvelope = 1000f
        val totalMaxRange = significantWeapons.maxOfOrNull { it.slotRange } ?: 0f
        return max(Preset.threatEvalRadius, totalMaxRange + rangeEnvelope)
    }

    /** Weapons that can be used by the ship to conduct attacks, as opposed to PD, decoratives, etc. */
    private fun findSignificantWeapons(): List<WeaponAPI> {
        val weapons = ship.allWeapons.filter { weapon ->
            when {
                weapon.slot.isHidden -> false
                weapon.slot.isDecorative -> false
                weapon.slot.isSystemSlot -> false
                weapon.slot.isStationModule -> false
                weapon.type == WeaponAPI.WeaponType.MISSILE -> false
                weapon.derivedStats.dps == 0f -> false
                else -> true
            }
        }

        // Filter out PD weapons, but only if there are non PD weapons available.
        // Do not count weapons made PD by S-modded Integrated Point Defense AI.
        val attackWeapons = weapons.filter { !it.isPDSpec || it.slot.isHardpoint }.ifEmpty { weapons }

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

    private val WeaponAPI.isInLongReload: Boolean
        get() = maxReloadTime >= Preset.weaponMaxReloadTime && reloadTimeRemaining >= 2f && !isInFiringSequence

    /** A ship can have multiple valid weapon groups. */
    private fun findWeaponGroups(): List<WeaponGroup> {
        // Special case for front facing hardpoints.
        if (significantWeapons.any { it.slot.isHardpoint && it.arcFacing == 0f }) {
            return listOf(WeaponGroup(ship, significantWeapons, 0f))
        }

        // Find firing arc boundary closes to ship front for
        // each weapon, or 0f for front facing weapons.
        val attackAngles: Set<Float> = significantWeapons.flatMap { weapon ->
            val facing = shortestRotation(0f, weapon.arcFacing)
            val arc = weapon.arc

            when {
                // Assume hardpoints have no arc at all.
                weapon.slot.isHardpoint -> listOf(facing)

                // Ship front is within weapon arc.
                abs(facing) < arc / 2f -> listOf(0f)

                // Ship back is within weapon arc, return both angles.
                180f - abs(facing) < arc / 2f -> listOf(facing - arc / 2f, facing + arc / 2f)

                // Return weapon arc boundary closer to ship front.
                else -> listOf(facing - facing.sign * (arc / 2f))
            }
        }.toSet() + 0f

        data class AngleDPS(val angle: Float, val dps: Float)

        // Calculate DPS for each attack angle.
        val anglesDPS: List<AngleDPS> = attackAngles.map { angle ->
            val dps = significantWeapons.filter { it.isAngleInArc(angle) }.sumOf { it.derivedStats.dps }
            AngleDPS(angle, dps)
        }

        // Find all weapon groups with acceptable DPS.
        val bestWeaponGroup: AngleDPS = anglesDPS.maxWithOrNull(compareBy { it.dps })!!
        val validWeaponGroups = anglesDPS.filter { it.dps >= bestWeaponGroup.dps * Preset.validWeaponGroupDPSThreshold }
        return validWeaponGroups.map { WeaponGroup(ship, significantWeapons, it.angle) }
    }
}