package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.allGroupedWeapons
import com.genir.aitweaks.core.extensions.rangeFromShipCenter
import com.genir.aitweaks.core.extensions.totalCollisionRadius
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.shipai.WeaponGroups.findGroups
import com.genir.aitweaks.core.shipai.autofire.Tag
import com.genir.aitweaks.core.shipai.autofire.hasAITag
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection

class ShipStats(private val ship: ShipAPI) {
    val primaryWeapons: List<WeaponHandle> = findPrimaryWeapons()
    val attackTargetSearchRange: Float = calculateAttackTargetSearchRange()
    val totalCollisionRadius: Float = ship.totalCollisionRadius
    val weaponGroups: List<WeaponGroup> = findGroups(ship, primaryWeapons)

    private fun calculateAttackTargetSearchRange(): Float {
        val rangeEnvelope = 1.5f
        val totalMaxRange = primaryWeapons.maxOfOrNull { it.slot.rangeFromShipCenter(0f.toDirection, it.engagementRange) } ?: 0f

        return totalMaxRange * rangeEnvelope
    }

    /** Weapons that can be used by the ship to conduct attacks, as opposed to PD, decoratives, etc. */
    private fun findPrimaryWeapons(): List<WeaponHandle> {
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

                it.hasAITag(Tag.PRIMARY_WEAPON) -> true

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
}
