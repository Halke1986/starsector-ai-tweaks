package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.extensions.isAngleInArc

/** Attack characteristics of an individual broadside configuration. */
class Broadside(significantWeapons: List<WeaponAPI>, val facing: Float) {
    private val weapons = significantWeapons.filter { it.isAngleInArc(facing) }

    val effectiveRange: Float = effectiveRange(Preset.effectiveDpsThreshold)
    val minRange: Float = weapons.minOfOrNull { it.slotRange } ?: 0f
    val maxRange: Float = weapons.maxOfOrNull { it.slotRange } ?: 0f

    /** Fraction of primary weapons DPS that can be delivered at the given range. */
    fun dpsFractionAtRange(range: Float): Float {
        var all = 0f
        var inRange = 0f

        weapons.forEach {
            val dps = it.derivedStats.dps
            all += dps
            if (it.slotRange >= range) inRange += dps
        }

        return if (all != 0f) inRange / all else 0f
    }

    /** Range at which the ship can deliver at least
     * `dpsFraction` of its primary weapons DPS. */
    private fun effectiveRange(effectiveDpsThreshold: Float): Float {
        val dps = weapons.sumOf { it.derivedStats.dps.toDouble() }.toFloat()

        if (dps == 0f) return 0f

        var dpsInRange = dps
        weapons.sortedWith(compareBy { it.slotRange }).forEach { weapon ->
            dpsInRange -= weapon.derivedStats.dps
            if (dpsInRange / dps <= effectiveDpsThreshold) {
                return weapon.slotRange
            }
        }

        return 0f
    }
}