package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.features.shipai.autofire.Hit.Type.*
import com.genir.aitweaks.core.features.shipai.autofire.ballistics.BallisticParams
import com.genir.aitweaks.core.features.shipai.autofire.ballistics.Ballistics
import com.genir.aitweaks.core.features.shipai.autofire.ballistics.Target
import com.genir.aitweaks.core.utils.Arc
import com.genir.aitweaks.core.utils.extensions.facing
import com.genir.aitweaks.core.utils.extensions.isShip
import com.genir.aitweaks.core.utils.extensions.noFF

data class Hit(val target: CombatEntityAPI, val range: Float, val type: Type) {
    enum class Type {
        SHIELD,
        HULL,
        ALLY,
        ROTATE_BEAM // placeholder type for mock hit used by beams rotating to a new target
    }
}

/** Analyzes the potential collision between projectile and target. Null if no collision. */
fun analyzeHit(ballistics: Ballistics, target: CombatEntityAPI, params: BallisticParams): Hit? {
    // Simple circumference collision is enough for missiles and fighters.
    if (!target.isShip) return ballistics.willHitCircumference(Target.entity(target), params)?.let { Hit(target, it, HULL) }

    // Check shield hit.
    if (hasShield(target)) ballistics.willHitShield(target as ShipAPI, params)?.let { return Hit(target, it, SHIELD) }

    // Check bounds hit.
    return ballistics.willHitBounds(target as ShipAPI, params)?.let { Hit(target, it, HULL) }
}

fun analyzeAllyHit(ballistics: Ballistics, target: CombatEntityAPI, ally: ShipAPI, params: BallisticParams): Hit? {
    return when {
        ballistics.weapon.noFF -> null
        !canHitAlly(ballistics, target, ally, params) -> null
        else -> Hit(ally, ballistics.closestHitRange(Target.shield(ally), params), ALLY)
    }
}

/** Calculates if an inaccurate projectile may collide with allay ship */
private fun canHitAlly(ballistics: Ballistics, target: CombatEntityAPI, ally: ShipAPI, params: BallisticParams): Boolean {
    val (_, burstEnd) = weaponBurstInterval(ballistics.weapon)
    val startParams = BallisticParams(params.accuracy, params.delay)
    val endParams = BallisticParams(params.accuracy, params.delay + burstEnd)

    val ballisticTarget = Target.entity(target)
    val ballisticAlly = Target.shield(ally)

    val allyArc = Arc.merge(
        ballistics.interceptArc(ballisticAlly, startParams),
        ballistics.interceptArc(ballisticAlly, endParams),
    )

    val enemyArc = Arc.fromTo(ballistics.weapon.currAngle, ballistics.intercept(ballisticTarget, endParams).facing)

    val spread = ballistics.weapon.spec.maxSpread + 2f
    return allyArc.increasedBy(spread).overlaps(enemyArc)
}

/** Calculates the time intervals between receiving a fire command
 * and the start and end of a burst for the given weapon. */
private fun weaponBurstInterval(weapon: WeaponAPI): Pair<Float, Float> {
    // Don't bother with interruptible burst weapons.
    // They are rare and difficult to account for.
    if (weapon.spec.isInterruptibleBurst) return Pair(0f, 0f)

    val cycle = weapon.firingCycle
    return Pair(cycle.warmupDuration, cycle.warmupDuration + cycle.burstDuration)
}

/** Workaround for hulks retaining outdated ShieldAPI. */
private fun hasShield(target: CombatEntityAPI): Boolean = target.isShip && !(target as ShipAPI).isHulk
