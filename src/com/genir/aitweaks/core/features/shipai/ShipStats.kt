package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.extensions.isAngleInArc
import com.genir.aitweaks.core.utils.extensions.isFrontFacing
import com.genir.aitweaks.core.utils.extensions.isInFiringSequence
import com.genir.aitweaks.core.utils.extensions.isPD
import kotlin.math.abs
import kotlin.math.max

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

    private val WeaponAPI.isInLongReload: Boolean
        get() = maxReloadTime >= Preset.weaponMaxReloadTime && reloadTimeRemaining >= 2f && !isInFiringSequence

    /** A ship can have multiple valid weapon groups. */
    private fun findWeaponGroups(): List<WeaponGroup> {
        when {
            significantWeapons.isEmpty() -> return listOf(WeaponGroup(ship, listOf()))

            // Special case for front facing hardpoints.
            significantWeapons.any { it.slot.isHardpoint && it.arcFacing == 0f } -> {
                listOf(WeaponGroup(ship, significantWeapons.filter { it.isFrontFacing }))
            }
        }

        // Find firing arc boundary closest to ship front for
        // each weapon, or 0f for front facing weapons.
        val attackAngles: Map<Float, Float> = WeaponGroup.attackAngles(significantWeapons).ifEmpty { return listOf() }

        // Find all weapon groups with acceptable DPS.
        val bestWeaponGroup = attackAngles.maxWithOrNull(compareBy<Map.Entry<Float, Float>> { it.value }.thenBy { -abs(it.key) })!!
        val validWeaponGroups = attackAngles.filter { it.value >= bestWeaponGroup.value * Preset.validWeaponGroupDPSThreshold }
        return validWeaponGroups.map { (angle, _) ->
            WeaponGroup(ship, significantWeapons.filter { it.isAngleInArc(angle) })
        }
    }
}
