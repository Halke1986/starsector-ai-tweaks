package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.extensions.isAngleInArc
import com.genir.aitweaks.core.utils.extensions.isPD
import org.lazywizard.lazylib.MathUtils
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random

class ShipStats(ship: ShipAPI) {
    val effectiveRange: Float = ship.effectiveRange(Preset.effectiveDpsThreshold)
    val minRange: Float = ship.minRange
    val maxRange: Float = ship.maxRange
    val totalCollisionRadius: Float = ship.totalCollisionRadius
    val broadsideFacing = calculateBroadsideFacing(ship)

    private fun calculateBroadsideFacing(ship: ShipAPI): Float {
        // Find all important weapons.
        val weapons = ship.allWeapons.filter { weapon ->
            when {
                weapon.type == WeaponAPI.WeaponType.MISSILE -> false
                weapon.derivedStats.dps == 0f -> false
                weapon.isPD -> false
                else -> true
            }
        }

        // Find firing arc boundary closes to ship front for each weapon,
        // or 0f for front facing weapons. Shuffle to randomly chose
        // one broadside for symmetric broadside ships.
        val angles = weapons.fold(setOf(0f)) { angles, weapon ->
            val facing = MathUtils.getShortestRotation(0f, weapon.arcFacing)

            val angle = if (weapon.isAngleInArc(0f)) 0f
            else facing - facing.sign * (weapon.arc / 2f - 0.1f)

            angles + angle
        }.filter { abs(it) <= Preset.maxBroadsideFacing }.shuffled(Random(ship.id.hashCode()))

        // Calculate DPS at each weapon arc boundary angle.
        val angleDPS: Map<Float, Float> = angles.associateWith { angle ->
            weapons.filter { it.isAngleInArc(angle) }.sumOf { it.derivedStats.dps.toDouble() }.toFloat()
        }

        val bestAngleDPS: Map.Entry<Float, Float> = angleDPS.maxByOrNull { it.value } ?: return 0f

        // Prefer non-broadside orientation.
        if (bestAngleDPS.value * Preset.broadsideDPSThreshold < angleDPS[0f]!!) return 0f

        return bestAngleDPS.key + bestAngleDPS.key.sign * Preset.broadsideFacingPadding
    }
}