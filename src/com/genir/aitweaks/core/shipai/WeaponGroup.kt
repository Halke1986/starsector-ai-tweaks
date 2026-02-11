package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.shipai.autofire.Tag
import com.genir.aitweaks.core.shipai.autofire.ballistics.BallisticParams.Companion.defaultBallisticParams
import com.genir.aitweaks.core.shipai.autofire.ballistics.BallisticTarget
import com.genir.aitweaks.core.shipai.autofire.ballistics.interceptArc
import com.genir.aitweaks.core.shipai.autofire.hasAITag
import com.genir.aitweaks.core.utils.Bounds
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection
import org.lwjgl.util.vector.Vector2f

/** A group of weapons that should be able to fire along a single attack vector. */
class WeaponGroup(val ship: ShipAPI, val weapons: List<WeaponHandle>) {
    val defaultFacing: Direction = defaultAttackFacing() // Default group facing in ship frame of reference.
    val dps: Float = weapons.sumOf { it.effectiveDPS }
    private val rangeMap: Map<WeaponHandle, Float> = weaponRanges()
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
            if (it.rangeInGroup >= range) {
                inRange += dps
            }
        }

        return if (all != 0f) inRange / all else 0f
    }

    private data class DPSArc(val arc: Arc, val dps: Float)

    private fun weaponRanges(): Map<WeaponHandle, Float> {
        return weapons.associateWith { weapon ->
            val rangeMod = if (weapon.hasAITag(Tag.APPROACH_CLOSER)) 0.8f else 1f
            weapon.slot.rangeFromShipCenter(defaultFacing, weapon.range) * rangeMod
        }
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

    private val WeaponHandle.rangeInGroup: Float
        get() = rangeMap[this]!!

    private fun defaultAttackFacing(): Direction {
        val dpsArcs: Sequence<DPSArc> = weapons.asSequence().map { weapon ->
            DPSArc(weapon.arc, effectivePeakDPS(weapon))
        }

        val optimalArc: Arc = dpsArcs.maxWithOrNull(compareBy<DPSArc> { it.dps }.thenBy { -it.arc.distanceTo(0f.toDirection).length })?.arc
            ?: return 0f.toDirection

        return -optimalArc.distanceTo(0f.toDirection)
    }

    /** Calculate ship facing that maximizes DPS delivered
     * to the target when using the current weapon group. */
    fun shipAttackFacing(target: CombatEntityAPI, targetLocationOverride: Vector2f? = null): Direction {
        val directFacing: Direction = (target.location - ship.location).facing
        val defaultFacing: Direction = directFacing + this.defaultFacing

        // Use default facing if no firing solutions are available.
        if (weapons.isEmpty()) {
            return defaultFacing
        }

        val ballisticTarget = BallisticTarget(
            targetLocationOverride ?: target.location,
            target.timeAdjustedVelocity,
            Bounds.radius(target) * 0.7f,
            target,
        )

        val facingStash = ship.facing
        val offsets: MutableList<DPSArc> = mutableListOf()
        try {
            // Aim the weapon as if the ship was at the default facing.
            // This allows to eliminate velocity and location translation errors.
            ship.facing = defaultFacing.degrees

            // Calculate offset angle between weapon arc and intercept arc for each weapon.
            weapons.forEach { weapon ->
                val interceptArc: Arc = interceptArc(weapon, ballisticTarget, defaultBallisticParams)
                val weaponArc: Arc = weapon.absoluteArc

                // Ships attempt to orient themselves so all weapons
                // can target the enemy center point.
                val idealTrackingOffset = Arc(
                    angle = weaponArc.angle,
                    facing = interceptArc.facing - weaponArc.facing,
                )
                offsets.add(DPSArc(idealTrackingOffset, 1f))

                // If full center-point coverage is not possible, ships
                // rotate to maximize total DPS on the target.
                val extendedTrackingOffset = Arc(
                    angle = interceptArc.angle + weaponArc.angle,
                    facing = interceptArc.facing - weaponArc.facing,
                )
                offsets.add(DPSArc(extendedTrackingOffset, effectivePeakDPS(weapon)))
            }
        } finally {
            ship.facing = facingStash
        }

        // Add a negative weight for the arc opposite the default facing.
        // This filters out undesirable rear-facing orientation solutions.
        offsets.add(DPSArc(Arc(200f, 180f.toDirection), -1e9f))

        data class TrackingSolution(val offset: Direction, val dps: Float)

        val offsetSubArcs: Sequence<DPSArc> = splitArcs(offsets).filter { it.dps > 0f }
        val solutions: Sequence<TrackingSolution> = offsetSubArcs.map { subArc: DPSArc ->
            val trackingArc = Arc(
                angle = subArc.arc.angle - 2.5f, // Apply padding
                facing = defaultFacing + subArc.arc.facing
            )

            return@map TrackingSolution(trackingArc.distanceTo(defaultFacing), subArc.dps)
        }

        // Find the firing arc with the best DPS. If there are multiple,
        // select the one with the least facing change required.
        val optimalSolution: TrackingSolution = solutions.maxWithOrNull(compareBy<TrackingSolution> { it.dps }
            .thenBy { -it.offset.length })
            ?: return defaultFacing

        return defaultFacing - optimalSolution.offset
    }

    /** Splits a collection of potentially overlapping 2D arcs into non-overlapping sub-arcs.
     *
     * The function computes and returns sub-arcs that span the angular intervals between
     * adjacent arc boundaries ("arms") of the input arcs. */
    private fun splitArcs(arcs: List<DPSArc>): Sequence<DPSArc> {
        // Sorted boundaries between the arcs.
        val boundaries: List<Float> = arcs.flatMap { dpsArc -> dpsArc.arc.arms.toList() }.map { it.degrees }.sorted()

        // DPS sum of all weapons capable of firing in the given sub-arc.
        return boundaries.indices.asSequence().map { i ->
            val start = boundaries[i]
            val end = boundaries[(i + 1) % boundaries.size]

            // Handle wrap around.
            val angle = if (i + 1 < boundaries.size) end - start else 360f + end - start
            val facing = start + angle / 2

            val subArc = Arc(angle, facing.toDirection)

            val dps = arcs.filter { dpsArc -> dpsArc.arc.overlaps(subArc, tolerance = -0.1f) }.sumOf { it.dps }
            DPSArc(subArc, dps)
        }
    }

    /** Calculate weapon effective DPS for the purpose of arc selection.
     * Prioritize aiming weapons with higher peak DPS over those with higher
     * sustained DPS. This is because it's easier and more beneficial to land
     * high-damage shots even for short durations. Also, prioritize aiming beam
     * weapons, as they don’t require leading, making their targeting more stable. */
    private fun effectivePeakDPS(weapon: WeaponHandle): Float {
        return weapon.peakDPS(duration = 2f) * if (weapon.isBeam) 2f else 1f
    }
}
