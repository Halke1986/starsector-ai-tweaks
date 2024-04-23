package com.genir.aitweaks.features.maneuver

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.debug.debugPlugin
import com.genir.aitweaks.utils.extensions.isShip
import com.genir.aitweaks.utils.extensions.isValidTarget
import com.genir.aitweaks.utils.shipGrid
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sign

class FindTarget(val ship: ShipAPI, val range: Float) {
    val target: ShipAPI?
        get() {
            val opportunities = findTargetOpportunities().toList()

            debugPlugin.clear()
            opportunities.forEach {
                debugPlugin[it] = "${evaluateTarget(it)} ${it.hullSpec.hullId}"
            }

//            opportunities.filter { targetAngleCategory(it) == 4 }.forEach {
//                debugVertices.add(Line(ship.location, it.location, Color.BLUE))
//            }

            return opportunities.maxWithOrNull(TargetComparator())
        }

    inner class TargetComparator : Comparator<ShipAPI> {
        override fun compare(a: ShipAPI, b: ShipAPI): Int {
            val aAngle = targetAngleCategory(a)
            val bAngle = targetAngleCategory(b)

            // Choose target based on its evaluation.
            val aEval = evaluateTarget(a) + aAngle
            val bEval = evaluateTarget(b) + bAngle
            if (aEval != bEval) return (aEval - bEval).sign

            // Choose target closest to ship facing, by category.
            if (aAngle != bAngle) return (aAngle - bAngle).sign

            // Choose target closest to ship.
            val aDist = (a.location - ship.location).lengthSquared()
            val bDist = (b.location - ship.location).lengthSquared()
            return sign(aDist - bDist).toInt()
        }
    }

    /** Find all potential enemy targets in or close to ships weapon range. */
    private fun findTargetOpportunities(): Sequence<ShipAPI> {
        val radius = range * 2f
        val allShips = shipGrid().getCheckIterator(ship.location, radius, radius).asSequence().filterIsInstance<ShipAPI>()
        val opportunities = allShips.filter {
            when {
                it.owner == ship.owner -> false
                !it.isValidTarget -> false
                !it.isShip -> false
                isOutOfRange(it, range + it.collisionRadius * 2f) -> false
                else -> true
            }
        }

        return opportunities
    }

    private fun evaluateTarget(t: ShipAPI): Int {
        var eval = 0

        // Bonus to close targets, unless they are frigates.
        val distance = (ship.location - t.location).length()
        if (distance <= range / 2f && !t.isFrigate) {
            eval++
        }

        // Avoid hunting low flux phase ships.
        if (t.phaseCloak?.specAPI?.isPhaseCloak == true) {
            when {
                t.fluxLevel < 0.25f -> eval -= 2
                t.fluxLevel < 0.50f -> eval -= 1
                t.fluxLevel > 0.75f -> eval += 1
            }
        }

        // Prioritize ships high on flux.
        if (t.fluxLevel > 0.75f) {
            eval++
        }

        // Finish damaged ships.
        if (t.hullLevel < 0.4f) {
            eval++
        }

        return eval
    }

    private fun targetAngleCategory(target: ShipAPI): Int {
        val facingToTarget = (target.location - ship.location).getFacing()
        val angle = abs(MathUtils.getShortestRotation(ship.facing, facingToTarget))

        val categoryNumber = 5
        val categoryAngle = 180f / (categoryNumber - 1)

        return (categoryNumber - 1) - floor(angle / categoryAngle + 0.5f).toInt()
    }

    private fun isOutOfRange(target: ShipAPI, range: Float): Boolean {
        return (target.location - ship.location).lengthSquared() > range * range
    }
}
