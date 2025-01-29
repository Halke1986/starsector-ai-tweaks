package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.autofire.*
import com.genir.aitweaks.core.utils.shortestRotation
import com.genir.aitweaks.core.utils.smoothCap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/** A group of weapons that should be able to fire along a single attack vector. */
class WeaponGroup(val ship: ShipAPI, val weapons: List<WeaponAPI>) {
    val defaultFacing: Float = defaultAttackFacing()
    val dps: Float = weapons.sumOf { it.effectiveDPS }
    private val rangeMap: Map<WeaponAPI, Float> = weaponRanges()
    val effectiveRange: Float = effectiveRange(Preset.effectiveDpsThreshold)
    val minRange: Float = weapons.minOfOrNull { it.rangeInGroup } ?: 0f
    val maxRange: Float = weapons.maxOfOrNull { it.rangeInGroup } ?: 0f

    /** Fraction of primary weapons DPS that can be delivered at the given range. */
    fun dpsFractionAtRange(range: Float): Float {
        var all = 0f
        var inRange = 0f

        weapons.forEach {
            val dps = it.effectiveDPS
            all += dps
            if (it.rangeInGroup >= range) inRange += dps
        }

        return if (all != 0f) inRange / all else 0f
    }

    data class DPSArc(val arcStart: Float, val arcEnd: Float, val dps: Float) {
        fun contains(angle: Float): Boolean {
            return angle in arcStart..arcEnd
        }
    }

    /** Calculate facing that maximizes DPS delivered to the target. */
    fun attackFacing(target: BallisticTarget): Float {
        // Firing solutions per weapon, with angles as offsets from
        // the ship's current facing, not absolute directions.
        val solutions: List<DPSArc> = weapons.mapNotNull { weapon ->
            // Full arcs are not relevant when aiming.
            if (weapon.arc == 360f) {
                return@mapNotNull null
            }

            // Return angles in relation to weapon current arc facing.
            val intercept = intercept(weapon, target, defaultBallisticParams)
            val relativeFacing = shortestRotation(weapon.absoluteArcFacing, intercept.facing)
            val halArc = weapon.arc / 2

            DPSArc(relativeFacing - halArc, relativeFacing + halArc, weapon.derivedStats.dps)
        }

        // Sorted boundaries between firing solution.
        val boundaries: List<Float> = solutions.flatMap { solution ->
            listOf(solution.arcStart, solution.arcEnd)
        }.sorted()

        // DPS sum of all weapons capable of firing in the given sub-arc.
        val indices = boundaries.indices.reversed().asSequence().drop(1)
        val subArcs: Sequence<DPSArc> = indices.map { i ->
            val start = boundaries[i]
            val end = boundaries[i + 1]
            val arcFacing = (start + end) / 2
            val dps = solutions.filter { solution -> solution.contains(arcFacing) }.sumOf { it.dps }

            DPSArc(boundaries[i], boundaries[i + 1], dps)
        }

        // Find the firing arc with the best DPS.
        // Face the target directly if no firing arc was identified.
        val targetFacing = (target.location - ship.location).facing
        val optimalArc: DPSArc = subArcs.maxWithOrNull(compareBy { it.dps }) ?: return targetFacing

        // Calculate ship facing that centers the selected firing arc on the target.
        val padding = min(2f, (optimalArc.arcEnd - optimalArc.arcStart) / 2)
        val startOffset = shortestRotation(targetFacing, ship.facing + optimalArc.arcStart + padding)
        val endOffset = shortestRotation(targetFacing, ship.facing + optimalArc.arcEnd - padding)
        val minOffset = min(startOffset, endOffset)
        val maxOffset = max(startOffset, endOffset)
        val optimalOffset = (minOffset + maxOffset) / 2

        // Cap the offset between ship facing and target facing. This ensures
        // ships like the Conquest don't go full-on age-of-sails broadside.
        val smoothOffset = smoothCap(optimalOffset, 30f)
        if (smoothOffset in minOffset..maxOffset) {
            return smoothOffset + targetFacing
        }

        return minOffset + targetFacing
    }

    private fun weaponRanges(): Map<WeaponAPI, Float> {
        return weapons.associateWith { weapon ->
            val rangeMod = if (weapon.hasAITag(Tag.APPROACH_CLOSER)) 0.8f else 1f
            weapon.rangeFromShipCenter(defaultFacing) * rangeMod
        }
    }

    private fun defaultAttackFacing(): Float {
        val anglesDPS: Map<Float, Float> = attackAngles(weapons)
        return anglesDPS.maxWithOrNull(compareBy { it.value })?.key ?: 0f
    }

    /** Range at which the ship can deliver at least
     * `dpsFraction` of its primary weapons DPS. */
    private fun effectiveRange(effectiveDpsThreshold: Float): Float {
        if (dps == 0f) return 0f

        var dpsInRange = dps
        weapons.sortedWith(compareBy { it.rangeInGroup }).forEach { weapon ->
            dpsInRange -= weapon.effectiveDPS
            if (dpsInRange / dps <= effectiveDpsThreshold) {
                return weapon.rangeInGroup
            }
        }

        return 0f
    }

    private val WeaponAPI.rangeInGroup: Float
        get() = rangeMap[this]!!

    companion object {
        /** Find firing arc angles closest to ship front for each weapon,
         * or 0f for front facing weapons. Associate the angles with total
         * DPS for the provided weapon list.*/
        fun attackAngles(weapons: List<WeaponAPI>): Map<Float, Float> {
            val angles: List<Float> = weapons.flatMap { weapon ->
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
            }

            return angles.toSet().associateWith { angle ->
                weapons.filter { it.isAngleInArc(angle) }.sumOf { it.effectiveDPS }
            }
        }
    }
}
