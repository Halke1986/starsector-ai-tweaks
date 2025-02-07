package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.autofire.*
import com.genir.aitweaks.core.state.State
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import org.lwjgl.util.vector.Vector2f

/** A group of weapons that should be able to fire along a single attack vector. */
class WeaponGroup(val ship: ShipAPI, val weapons: List<WeaponAPI>) {
    val defaultFacing: Direction = defaultAttackFacing()
    val dps: Float = weapons.sumOf { it.effectiveDPS }
    private val rangeMap: Map<WeaponAPI, Float> = weaponRanges()
    val effectiveRange: Float = effectiveRange(Preset.effectiveDpsThreshold)
    val minRange: Float = weapons.minOfOrNull { it.rangeInGroup } ?: 0f
    val maxRange: Float = weapons.maxOfOrNull { it.rangeInGroup } ?: 0f

    /** Fraction of primary weapons DPS that can be delivered to the given range. */
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
    fun attackFacing(target: CombatEntityAPI, targetLocationOverride: Vector2f? = null): Direction {
        val ballisticTarget = BallisticTarget(
            targetLocationOverride ?: target.location,
            target.timeAdjustedVelocity,
            State.state.bounds.radius(target),
        )

        val targetFacing = (target.location - ship.location).facing
        val directFacing = targetFacing + defaultFacing

        // Calculate offset angle between default facing and intercept arc for each weapon.
        val facingStash = ship.facing
        val solutions: List<DPSArc> = try {
            // Aim the weapon as if the ship was facing the target directly.
            // This allows to eliminate velocity and location translation errors.
            ship.facing = directFacing.degrees

            val defaultFacingVector = defaultFacing.unitVector
            weapons.mapNotNull { weapon ->
                // Full arcs are not relevant when aiming.
                if (weapon.arc == 360f) {
                    return@mapNotNull null
                }

                val defaultAttackPoint = defaultFacingVector * weapon.rangeInGroup
                var defaultFacing: Direction = (defaultAttackPoint - weapon.slot.location).facing

                // Default weapon facing falls outside the slot firing arc.
                if (!weapon.isAngleInArc(defaultFacing)) {
                    val toSlotBoundaryStart = (weapon.arcFacing.direction - weapon.arc / 2) - defaultFacing
                    val toSlotBoundaryEnd = (weapon.arcFacing.direction + weapon.arc / 2) - defaultFacing

                    defaultFacing += if (toSlotBoundaryStart.length > toSlotBoundaryEnd.length) toSlotBoundaryEnd else toSlotBoundaryStart
                }

                val interceptArc = interceptArc(weapon, ballisticTarget, defaultBallisticParams)
                val relativeFacing = (interceptArc.facing - (defaultFacing + ship.facing.direction)).degrees
                val halArc = interceptArc.half

                // Prioritize aiming weapons with higher peak DPS over those with higher sustained DPS.
                // This is because it's easier and more beneficial to land high-damage shots even for short durations.
                // Also, prioritize aiming beam weapons, as they don’t require leading, making their targeting more stable.
                val dps = weapon.peakDPS * if (weapon.isBeam) 2f else 1f
                DPSArc(relativeFacing - halArc, relativeFacing + halArc, dps)
            }
        } finally {
            ship.facing = facingStash
        }

        // Face the target directly if no firing solutions are available.
        if (solutions.isEmpty()) {
            return directFacing
        }

        // Sorted boundaries between firing arc offsets.
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

        val offsetFromCurrentFacing = fun(arc: DPSArc): Float {
            val offset = (arc.arcEnd + arc.arcStart) / 2
            val proposedFacing = directFacing + offset
            return (ship.facing.direction - proposedFacing).length
        }

        // Find the firing arc with the best DPS. If there are multiple, select the one with the least facing change required.
        val optimalArc: DPSArc = subArcs.maxWithOrNull(compareBy<DPSArc> { it.dps }.thenBy { -offsetFromCurrentFacing(it) })!!
        val offset = (optimalArc.arcEnd + optimalArc.arcStart) / 2

        return directFacing + offset
    }

    private fun weaponRanges(): Map<WeaponAPI, Float> {
        return weapons.associateWith { weapon ->
            val rangeMod = if (weapon.hasAITag(Tag.APPROACH_CLOSER)) 0.8f else 1f
            weapon.rangeFromShipCenter(defaultFacing) * rangeMod
        }
    }

    private fun defaultAttackFacing(): Direction {
        val anglesDPS: Map<Direction, Float> = attackAngles(weapons)
        return anglesDPS.maxWithOrNull(compareBy { it.value })?.key ?: Direction(0f)
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
        fun attackAngles(weapons: List<WeaponAPI>): Map<Direction, Float> {
            val angles: List<Direction> = weapons.flatMap { weapon ->
                val facing: Direction = weapon.arcFacing.direction
                val arc = weapon.arc

                when {
                    // Assume hardpoints have no arc at all.
                    weapon.slot.isHardpoint -> listOf(facing)

                    // Ship front is within weapon arc.
                    facing.length < arc / 2f -> listOf(Direction(0f))

                    // Ship back is within weapon arc, return both angles.
                    180f - facing.length < arc / 2f -> listOf(facing - arc / 2f, facing + arc / 2f)

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
