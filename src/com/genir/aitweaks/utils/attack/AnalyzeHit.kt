package com.genir.aitweaks.utils.attack

import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.utils.extensions.isShip

data class Hit(val target: CombatEntityAPI, val range: Float, val shieldHit: Boolean) {
    override fun toString(): String {
        val name = when {
            target is MissileAPI -> "missile"
            (target as ShipAPI).isHulk -> "hulk"
            target.name == null -> target.hullSpec.hullId
            else -> target.name
        }

        return "($name $range $shieldHit)"
    }
}

/** Analyzes the potential collision between projectile and target. Null if no collision. */
fun analyzeHit(weapon: WeaponAPI, target: CombatEntityAPI, params: BallisticParams): Hit? {
    // Simple circumference collision is enough for missiles and fighters.
    if (!target.isShip) return willHitCircumference(weapon, AttackTarget(target), params)?.let { Hit(target, it, false) }

    // Check shield hit.
    if (hasShield(target)) willHitShield(weapon, target as ShipAPI, params)?.let { return Hit(target, it, true) }

    // Check bounds hit.
    return willHitBounds(weapon, target as ShipAPI, params)?.let { Hit(target, it, false) }
}

fun analyzeAllyHit(weapon: WeaponAPI, ally: ShipAPI, params: BallisticParams): Hit? = when {
    weapon.projectileCollisionClass == CollisionClass.PROJECTILE_FIGHTER -> null
    weapon.projectileCollisionClass == CollisionClass.RAY_FIGHTER -> null
    !willHitShieldCautious(weapon, ally, params) -> null
    else -> Hit(ally, closestHitRange(weapon, targetShield(ally), params)!!, false)
}

/** Workaround for hulks retaining outdated ShieldAPI */
private fun hasShield(target: CombatEntityAPI): Boolean = target.isShip && !(target as ShipAPI).isHulk