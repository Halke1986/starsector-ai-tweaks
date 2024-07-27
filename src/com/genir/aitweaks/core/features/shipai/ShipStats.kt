package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.extensions.isAngleInArc
import com.genir.aitweaks.core.utils.extensions.isInFiringSequence
import com.genir.aitweaks.core.utils.extensions.isPD
import org.lazywizard.lazylib.MathUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

class ShipStats(private val ship: ShipAPI) {
    val significantWeapons: List<WeaponAPI> = findSignificantWeapons()
    val threatSearchRange: Float = calculateThreatSearchRange()
    val totalCollisionRadius: Float = ship.totalCollisionRadius
    val broadsides: List<Broadside> = calculateBroadsides()

    private fun calculateThreatSearchRange(): Float {
        val rangeEnvelope = 500f
        val totalMaxRange = significantWeapons.minOfOrNull { it.slotRange } ?: 0f
        return max(Preset.threatEvalRadius, totalMaxRange + rangeEnvelope)
    }

    /** Weapons that can be used by the ship to conduct attacks, as opposed to PD, decoratives, etc. */
    private fun findSignificantWeapons(): List<WeaponAPI> {
        return ship.allWeapons.filter { weapon ->
            when {
                weapon.slot.isHidden -> false
                weapon.slot.isDecorative -> false
                weapon.slot.isSystemSlot -> false
                weapon.slot.isStationModule -> false
                weapon.type == WeaponAPI.WeaponType.MISSILE -> false
                weapon.isPD -> false
                weapon.isDisabled -> false
                weapon.isPermanentlyDisabled -> false
                weapon.derivedStats.dps == 0f -> false
                weapon.isInLongReload -> false
                else -> true
            }
        }
    }

    private val WeaponAPI.isInLongReload: Boolean
        get() = maxReloadTime >= Preset.weaponMaxReloadTime && reloadTimeRemaining >= 2f && !isInFiringSequence

    /** A ship can have multiple broadside configurations, not necessarily symmetrical. */
    private fun calculateBroadsides(): List<Broadside> {
        // Special case for built-in front facing hardpoints, which are used for spinal mounted rail guns etc.
        if (significantWeapons.any { it.slot.isHardpoint && it.slot.isBuiltIn && it.arcFacing == 0f }) {
            return listOf(Broadside(significantWeapons, 0f))
        }

        // Find firing arc boundary closes to ship front for
        // each weapon, or 0f for front facing weapons.
        val attackAngles: Set<Float> = significantWeapons.flatMap { weapon ->
            val facing = MathUtils.getShortestRotation(0f, weapon.arcFacing)
            val limitedArc = max(0f, weapon.arc - 2f * Preset.broadsideFacingPadding)

            when {
                // Assume hardpoints have no arc at all.
                weapon.slot.isHardpoint -> listOf(facing)

                // Ship front is within weapon arc.
                abs(facing) < limitedArc / 2f -> listOf(0f)

                // Ship back is within weapon arc, return both angles.
                180f - abs(facing) < limitedArc / 2f -> listOf(facing - limitedArc / 2f, facing + limitedArc / 2f)

                // Return weapon arc boundary closer to ship front.
                else -> listOf(facing - facing.sign * (limitedArc / 2f))
            }
        }.toSet() + 0f

        data class AngleDPS(val angle: Float, val dps: Float)

        // Calculate DPS for each attack angle. Give bonus to frontal attack orientation.
        val anglesDPS: List<AngleDPS> = attackAngles.map { angle ->
            val dps = significantWeapons.filter { it.isAngleInArc(angle) }.sumOf { it.derivedStats.dps.toDouble() }
            val adjustedDps = if (angle == 0f) dps * Preset.frontAttackMultiplier else dps

            AngleDPS(angle, adjustedDps.toFloat())
        }

        // Find all broadside configurations with acceptable DPS.
        val bestBroadside: AngleDPS = anglesDPS.maxWithOrNull(compareBy { it.dps })!!
        val validBroadsides = anglesDPS.filter { it.dps >= bestBroadside.dps * Preset.validBroadsideDPSThreshold }
        return validBroadsides.map { Broadside(significantWeapons, it.angle) }
    }
}