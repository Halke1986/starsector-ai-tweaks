package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.facing
import com.genir.aitweaks.core.extensions.hasShield
import com.genir.aitweaks.core.extensions.isHit
import com.genir.aitweaks.core.extensions.isShip
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.shipai.autofire.Hit.Type.*
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction

data class Hit(val target: CombatEntityAPI, val range: Float, val type: Type) {
    enum class Type {
        SHIELD,
        HULL,
        ALLY,
        ROTATE_BEAM // placeholder type for mock hit used by beams rotating to a new target
    }
}

/** Analyzes the potential collision between projectile and target. Null if no collision. */
fun analyzeHit(weapon: WeaponHandle, target: CombatEntityAPI, params: BallisticParams): Hit? {
    // Simple circumference collision is enough for missiles and fighters.
    if (!target.isShip) return willHitCircumference(weapon, BallisticTarget.collisionRadius(target), params)?.let { Hit(target, it, HULL) }

    // Check shield hit.
    if (target.hasShield) willHitShield(weapon, target as ShipAPI, params)?.let { return Hit(target, it, SHIELD) }

    // Check bounds hit.
    return willHitBounds(weapon, target as ShipAPI, params)?.let { Hit(target, it, HULL) }
}

fun analyzeAllyHit(weapon: WeaponHandle, target: CombatEntityAPI, ally: ShipAPI, params: BallisticParams): Hit? {
    return when {
        weapon.noFF -> null
        !canHitAlly(weapon, target, ally, params) -> null
        else -> Hit(ally, closestHitRange(weapon, BallisticTarget.shieldRadius(ally), params), ALLY)
    }
}

fun willIdealShotHitShields(weapon: WeaponHandle, target: CombatEntityAPI, params: BallisticParams): Boolean {
    if (!target.hasShield) {
        return false
    }

    val hitPoint = closestHit(weapon, BallisticTarget.shieldRadius(target as ShipAPI), params)
    return target.shield.isHit(hitPoint)
}

/** Calculates if an inaccurate projectile may collide with allay ship */
private fun canHitAlly(weapon: WeaponHandle, target: CombatEntityAPI, ally: ShipAPI, params: BallisticParams): Boolean {
    val (_, burstEnd) = weaponBurstInterval(weapon)
    val startParams = BallisticParams(params.accuracy, params.delay)
    val endParams = BallisticParams(params.accuracy, params.delay + burstEnd)

    val ballisticTarget = BallisticTarget.collisionRadius(target)
    val ballisticAlly = BallisticTarget.shieldRadius(ally)

    val allyArc = Arc.union(
        interceptArc(weapon, ballisticAlly, startParams),
        interceptArc(weapon, ballisticAlly, endParams),
    )

    val enemyArc = Arc.fromTo(weapon.currAngle.direction, intercept(weapon, ballisticTarget, endParams).facing)

    val spread = weapon.spec.maxSpread + 2f
    return allyArc.extendedBy(spread).overlaps(enemyArc)
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
