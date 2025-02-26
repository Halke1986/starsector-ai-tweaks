package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.autofire.*
import com.genir.aitweaks.core.utils.*
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

    private data class DPSArc(val arc: Arc, val dps: Float)

    private fun weaponRanges(): Map<WeaponAPI, Float> {
        return weapons.associateWith { weapon ->
            val rangeMod = if (weapon.hasAITag(Tag.APPROACH_CLOSER)) 0.8f else 1f
            weapon.rangeFromShipCenter(defaultFacing) * rangeMod
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

    private val WeaponAPI.rangeInGroup: Float
        get() = rangeMap[this]!!

    private fun defaultAttackFacing(): Direction {
        val dpsArcs: Sequence<DPSArc> = staticArcsInShipFrameOfReference(1e5f, 0f)
        val optimalArc: Arc = dpsArcs.maxWithOrNull(compareBy<DPSArc> { it.dps }.thenBy { -it.arc.distanceTo(0f.direction).length })?.arc
            ?: return 0f.direction
        return -optimalArc.distanceTo(0f.direction)
    }

    /** Calculate facing that maximizes DPS delivered to the target. */
    fun attackFacing(target: CombatEntityAPI, targetLocationOverride: Vector2f? = null): Direction {
        val ballisticTarget = BallisticTarget(
            targetLocationOverride ?: target.location,
            target.timeAdjustedVelocity,
            Bounds.radius(target) * 0.7f,
        )

        val toTarget = (target.location - ship.location)
        val targetFacing = toTarget.facing
        val coarseFacing = staticAttackFacing(ballisticTarget)
        val directFacing = targetFacing - coarseFacing

        // Face the target directly if no firing solutions are available.
        if (weapons.isEmpty()) {
            return targetFacing
        }

        // Calculate offset angle between default facing and intercept arc for each weapon.
        val facingStash = ship.facing
        val solutions: List<DPSArc> = try {
            // Aim the weapon as if the ship was facing the target directly.
            // This allows to eliminate velocity and location translation errors.
            ship.facing = directFacing.degrees

            weapons.map { weapon ->
                val weaponCoarseFacing: Direction = (target.location - weapon.location).facing
                val outOfArcCorrection = weapon.absoluteArc.distanceTo(weaponCoarseFacing)
                val interceptArc = interceptArc(weapon, ballisticTarget, defaultBallisticParams)
                val offset = interceptArc.facing - weaponCoarseFacing + outOfArcCorrection

                DPSArc(Arc(interceptArc.angle, offset), effectivePeakDPS(weapon))
            }
        } finally {
            ship.facing = facingStash
        }

        // DPS sum of all weapons capable of firing in the given sub-arc.
        val subArcs: Sequence<DPSArc> = splitArcs(solutions)

        val offsetFromCurrentFacing = fun(dpsArc: DPSArc): Float {
            val proposedFacing = directFacing + dpsArc.arc.facing
            return (ship.facing.direction - proposedFacing).length
        }

        // Find the firing arc with the best DPS. If there are multiple, select the one with the least facing change required.
        val optimalArc: DPSArc = subArcs.maxWithOrNull(compareBy<DPSArc> { it.dps }.thenBy { -offsetFromCurrentFacing(it) })
            ?: return directFacing

        return directFacing + optimalArc.arc.facing
    }

    /** Returns ship facing that maximizes DPS delivered to the target,
     * under the assumption the target is stationary and no leading is required. */
    private fun staticAttackFacing(target: BallisticTarget): Direction {
        val toTarget = (target.location - ship.location)
        val subArcs: Sequence<DPSArc> = staticArcsInShipFrameOfReference(toTarget.length, target.radius)

        val offsetFromTarget = fun(arcFacing: Direction): Float {
            return (toTarget.facing - (arcFacing + ship.facing)).length
        }

        // Find the firing arc with the best DPS. If there are multiple, select the one with the least facing change required.
        val optimalArc: DPSArc = subArcs.maxWithOrNull(compareBy<DPSArc> { it.dps }.thenBy { -offsetFromTarget(it.arc.facing) })
            ?: return 0f.direction

        return -optimalArc.arc.distanceTo(0f.direction)
    }

    private fun staticArcsInShipFrameOfReference(range: Float, targetRadius: Float): Sequence<DPSArc> {
        val targetSize: Float = angularSize(range * range, targetRadius).coerceAtLeast(1f)

        val dpsArcs: List<DPSArc> = weapons.map { weapon ->
            val weaponArc = weapon.Arc.extendedBy(targetSize)
            val shipArc = weaponArcInShipFrameOfReference(weapon.slot.location, weaponArc, range)
            DPSArc(shipArc, effectivePeakDPS(weapon))
        }

        return splitArcs(dpsArcs)
    }

    /** Computes the angular span of the weapon's firing range as seen
     * from the ship's center. Because the weapon is offset from the center,
     * the visible arc differs from its actual firing arc. */
    private fun weaponArcInShipFrameOfReference(slotLocation: Vector2f, arc: Arc, range: Float): Arc {
        val fullArc = Arc(360f, 180f.direction)
        if (arc.angle == 360f) {
            return fullArc
        }

        // Arms of the weapon firing arc.
        val arm1 = arc.arms.first.unitVector
        val arm2 = arc.arms.second.unitVector

        // Distance from the weapon slot at which the arms cross the range threshold.
        val dist1 = solve(slotLocation, arm1, range) ?: return fullArc
        val dist2 = solve(slotLocation, arm2, range) ?: return fullArc

        // Limits of the weapon arc, as seen from the ship's center.
        val point1 = arm1 * dist1 + slotLocation
        val point2 = arm2 * dist2 + slotLocation

        val angle = (point2.facing - arc.facing).degrees - (point1.facing - arc.facing).degrees
        return Arc(angle, point1.facing + angle / 2)
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

            val subArc = Arc(angle, facing.direction)

            val dps = arcs.filter { dpsArc -> dpsArc.arc.overlaps(subArc, tolerance = -0.1f) }.sumOf { it.dps }
            DPSArc(subArc, dps)
        }
    }

    /** Calculate weapon effective DPS for the purpose of arc selection.
     * Prioritize aiming weapons with higher peak DPS over those with higher
     * sustained DPS. This is because it's easier and more beneficial to land
     * high-damage shots even for short durations. Also, prioritize aiming beam
     * weapons, as they don’t require leading, making their targeting more stable. */
    private fun effectivePeakDPS(weapon: WeaponAPI): Float {
        return weapon.peakDPS * if (weapon.isBeam) 2f else 1f
    }
}
