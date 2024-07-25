package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.utils.extensions.isAngleInArc
import org.lazywizard.lazylib.MathUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.random.Random

class ShipStats(ship: ShipAPI) {
    val effectiveRange: Float = ship.effectiveRange(Preset.effectiveDpsThreshold)
    val minRange: Float = ship.minRange
    val maxRange: Float = ship.maxRange
    val totalCollisionRadius: Float = ship.totalCollisionRadius
    val broadsideFacing = calculateBroadsideFacing(ship)

    private fun calculateBroadsideFacing(ship: ShipAPI): Float {
        val weapons = ship.significantWeapons

        // Find firing arc boundary closes to ship front for each weapon,
        // or 0f for front facing weapons. Shuffle to randomly chose
        // one broadside for symmetric broadside ships.
        val angles = weapons.fold(setOf(0f)) { angles, weapon ->
            val facing = MathUtils.getShortestRotation(0f, weapon.arcFacing)
            val limitedArc = max(0f, weapon.arc - 2f * Preset.broadsideFacingPadding)

            angles + when {
                // Assume hardpoints have no arc at all.
                weapon.slot.isHardpoint -> facing

                // Ship front is within weapon arc.
                abs(facing) < limitedArc / 2f -> 0f

                else -> facing - facing.sign * (limitedArc / 2f)
            }
        }.filter { abs(it) <= Preset.maxBroadsideFacing }.shuffled(Random(ship.id.hashCode()))

        // Calculate DPS at each weapon arc boundary angle.
        val angleDPS: Map<Float, Float> = angles.associateWith { angle ->
            weapons.filter { it.isAngleInArc(angle) }.sumOf { it.derivedStats.dps.toDouble() }.toFloat()
        }

        val bestAngleDPS: Map.Entry<Float, Float> = angleDPS.maxByOrNull { it.value } ?: return 0f

        // Prefer non-broadside orientation.
        if (bestAngleDPS.value * Preset.broadsideDPSThreshold < angleDPS[0f]!!) return 0f

        return bestAngleDPS.key
    }
}