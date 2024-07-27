package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.extensions.isAngleInArc
import com.genir.aitweaks.core.utils.extensions.isPD
import org.lazywizard.lazylib.MathUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.random.Random

class ShipStats(private val ship: ShipAPI) {
    val significantWeapons: List<WeaponAPI> = findSignificantWeapons()
    val threatSearchRange: Float = calculateThreatSearchRange()
    val totalCollisionRadius: Float = ship.totalCollisionRadius
    val broadsides: List<Broadside> = calculateBroadsides(ship)

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
                weapon.isPermanentlyDisabled -> false
                weapon.derivedStats.dps == 0f -> false
                weapon.maxReloadTime >= Preset.weaponMaxReloadTime && weapon.reloadTimeRemaining >= 2f -> false
                else -> true
            }
        }
    }

    /** A ship can have multiple broadside configurations, not necessarily symmetrical. */
    private fun calculateBroadsides(ship: ShipAPI): List<Broadside> {
        // Special case for built-in front facing hardpoints, which are used for spinal mounted rail guns etc.
        if (significantWeapons.any { it.slot.isHardpoint && it.slot.isBuiltIn && it.arcFacing == 0f }) return broadsides(0f)

        // Find firing arc boundary closes to ship front for
        // each weapon, or 0f for front facing weapons.
        val attackAngles: Set<Float> = significantWeapons.map { weapon ->
            val facing = MathUtils.getShortestRotation(0f, weapon.arcFacing)
            val limitedArc = max(0f, weapon.arc - 2f * Preset.broadsideFacingPadding)

            when {
                // Assume hardpoints have no arc at all.
                weapon.slot.isHardpoint -> facing

                // Ship front is within weapon arc.
                abs(facing) < limitedArc / 2f -> 0f

                // Return weapon arc boundary closer to ship front.
                else -> facing - facing.sign * (limitedArc / 2f)
            }
        }.toSet() + 0f

        // Calculate DPS for each attack angle.
        val angleDPS: Map<Float, Float> = attackAngles.associateWith { angle ->
            significantWeapons.filter { it.isAngleInArc(angle) }.sumOf { it.derivedStats.dps.toDouble() }.toFloat()
        }

        data class AttackAngle(val angle: Float, val dps: Float)

        val frontDPS: Float = angleDPS[0f]!!
        val sortedAttackAngles: List<AttackAngle> = angleDPS.toList().map { AttackAngle(it.first, it.second) }.sortedBy { -it.dps }
        val bestAttackAngle: AttackAngle = sortedAttackAngles[0]

        return when {
            // Only front broadside is always present.
            sortedAttackAngles.size == 1 -> broadsides(0f)

            // Prefer non-broadside orientation.
            bestAttackAngle.dps < frontDPS * Preset.frontAttackMultiplier -> broadsides(0f)

            // Two similar DPS broadsides are present.
            bestAttackAngle.dps * 0.92f <= sortedAttackAngles[1].dps -> {
                broadsides(bestAttackAngle.angle, sortedAttackAngles[1].angle).shuffled(Random(ship.id.hashCode()))
            }

            else -> broadsides(bestAttackAngle.angle)
        }
    }

    private fun broadsides(vararg angles: Float): List<Broadside> {
        return angles.map { Broadside(significantWeapons, it) }
    }
}