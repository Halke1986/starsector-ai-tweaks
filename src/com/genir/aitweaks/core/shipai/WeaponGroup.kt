package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.autofire.BallisticTarget
import com.genir.aitweaks.core.shipai.autofire.defaultBallisticParams
import com.genir.aitweaks.core.shipai.autofire.intercept
import com.genir.aitweaks.core.utils.averageFacing
import com.genir.aitweaks.core.utils.shortestRotation
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/** A group of weapons that should be able to focus on a single attack angle. */
class WeaponGroup(val ship: ShipAPI, val weapons: List<WeaponAPI>) {
    val defaultFacing: Float = defaultAttackFacing()
    val dps: Float = weapons.sumOf { it.effectiveDPS }
    val effectiveRange: Float = effectiveRange(Preset.effectiveDpsThreshold)
    val minRange: Float = weapons.minOfOrNull { it.slotRange } ?: 0f
    val maxRange: Float = weapons.maxOfOrNull { it.slotRange } ?: 0f

    /** Fraction of primary weapons DPS that can be delivered at the given range. */
    fun dpsFractionAtRange(range: Float): Float {
        var all = 0f
        var inRange = 0f

        weapons.forEach {
            val dps = it.effectiveDPS
            all += dps
            if (it.slotRange >= range) inRange += dps
        }

        return if (all != 0f) inRange / all else 0f
    }

    /** Calculate facing that maximizes DPS delivered to target, according to ballistic calculations. */
    fun attackFacing(target: BallisticTarget): Float {
        // Prioritize hardpoints if there are any in the weapon group.
        val weapons: List<WeaponAPI> = weapons.filter { it.slot.isHardpoint }.ifEmpty { weapons }

        val solutions: Map<WeaponAPI, Float> = weapons.associateWith { weapon ->
            intercept(weapon, target, defaultBallisticParams).facing
        }

        // Aim directly at target if no weapon firing solution is available.
        if (solutions.isEmpty()) return (target.location - ship.location).facing - defaultFacing

        // Start with aiming the weapon group at the average intercept point.
        val averageIntercept: Float = averageFacing(solutions.values)
        val defaultShipFacing: Float = averageIntercept - defaultFacing

        // Fine tune the facing to minimize the amount of weapons
        // not able to aim at their calculated intercept point.
        var offsetNegative = 0f
        var offsetPositive = 0f
        solutions.forEach {
            val weapon = it.key
            val localIntercept: Float = it.value - defaultShipFacing
            if (weapon.isAngleInArc(localIntercept)) return@forEach

            // Angle to nearest arc boundary.
            val halfArc = weapon.arc / 2f
            val angleArc1 = shortestRotation(weapon.arcFacing + halfArc, localIntercept)
            val angleArc2 = shortestRotation(weapon.arcFacing - halfArc, localIntercept)
            val outOfArc = if (abs(angleArc1) > abs(angleArc2)) angleArc2 else angleArc1

            // Calculate offset if firing solution is outside weapon firing arc.
            when {
                outOfArc < 0f -> offsetNegative = min(offsetNegative, outOfArc)
                outOfArc > 0f -> offsetPositive = max(offsetPositive, outOfArc)
            }
        }

        return defaultShipFacing + offsetNegative + offsetPositive
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
        weapons.sortedWith(compareBy { it.slotRange }).forEach { weapon ->
            dpsInRange -= weapon.effectiveDPS
            if (dpsInRange / dps <= effectiveDpsThreshold) {
                return weapon.slotRange
            }
        }

        return 0f
    }

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
