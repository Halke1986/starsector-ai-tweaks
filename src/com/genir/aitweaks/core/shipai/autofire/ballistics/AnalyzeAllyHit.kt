package com.genir.aitweaks.core.shipai.autofire.ballistics

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.facing
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.shipai.autofire.firingCycle
import com.genir.aitweaks.core.state.Config.Companion.config
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction

/** Estimates whether a weapon attacking the given target might cause friendly fire on the specified ally.
 *
 * This function is more conservative than `analyzeHit`, accounting for the entire weapon burst
 * rather than a single idealized shot. This helps minimize the risk of friendly fire. */
fun analyzeAllyHit(weapon: WeaponHandle, target: CombatEntityAPI, ally: ShipAPI, params: BallisticParams): Hit? {
    return when {
        weapon.noFF -> {
            null
        }

        config.fireThroughShields && weapon.isPlainBeam -> {
            return analyzeHit(weapon, ally, params)?.let { hit ->
                Hit(hit.target, hit.range, Hit.Type.ALLY)
            }
        }

        !canHitAlly(weapon, target, ally, params) -> {
            null
        }

        else -> {
            Hit(ally, closestHitRange(weapon, BallisticTarget.shieldRadius(ally), params), Hit.Type.ALLY)
        }
    }
}

/** Estimates if an inaccurate projectile may collide with allay ship.
 * Takes entire weapon burst into account. */
private fun canHitAlly(weapon: WeaponHandle, target: CombatEntityAPI, ally: ShipAPI, params: BallisticParams): Boolean {
    val (_, burstEnd) = weaponBurstInterval(weapon)
    val startParams = BallisticParams(params.accuracy, params.delay)
    val endParams = BallisticParams(params.accuracy, params.delay + burstEnd)

    val ballisticTarget = BallisticTarget.collisionRadius(target)
    val ballisticAlly = BallisticTarget.shieldRadius(ally)

    // Arc occupied by the ally during the duration of the weapon burst.
    val allyArc = Arc.union(
        interceptArc(weapon, ballisticAlly, startParams),
        interceptArc(weapon, ballisticAlly, endParams),
    )

    // Expected weapon facing towards the enemy at the end of burst.
    val enemyArc = Arc.fromTo(
        weapon.currAngle.direction,
        intercept(weapon, ballisticTarget, endParams).facing,
    )

    val spread = weapon.spec.maxSpread + 2f
    return allyArc.addAngle(spread).overlaps(enemyArc)
}

/** Calculates the time intervals between receiving a fire command
 * and the start and end of a burst for the given weapon. */
private fun weaponBurstInterval(weapon: WeaponHandle): Pair<Float, Float> {
    // Don't bother with interruptible burst weapons.
    // They are rare and difficult to account for.
    if (weapon.spec.isInterruptibleBurst) return Pair(0f, 0f)

    val cycle = weapon.firingCycle
    return Pair(cycle.warmupDuration, cycle.warmupDuration + cycle.burstDuration)
}
