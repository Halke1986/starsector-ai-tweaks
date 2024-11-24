package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.features.shipai.autofire.Hit.Type.*
import com.genir.aitweaks.core.utils.extensions.isShip

data class Hit(val target: CombatEntityAPI, val range: Float, val type: Type) {
    enum class Type {
        SHIELD,
        HULL,
        ALLY,
        ROTATE_BEAM // placeholder type for mock hit used by beams rotating to a new target
    }
}

/** Analyzes the potential collision between projectile and target. Null if no collision. */
fun analyzeHit(weapon: WeaponAPI, target: CombatEntityAPI, params: BallisticParams): Hit? {
    // Simple circumference collision is enough for missiles and fighters.
    if (!target.isShip) return willHitCircumference(weapon, BallisticTarget.entity(target), params)?.let { Hit(target, it, HULL) }

    // Check shield hit.
    if (hasShield(target)) willHitShield(weapon, target as ShipAPI, params)?.let { return Hit(target, it, SHIELD) }

    // Check bounds hit.
    return willHitBounds(weapon, target as ShipAPI, params)?.let { Hit(target, it, HULL) }
}

fun analyzeAllyHit(weapon: WeaponAPI, ally: ShipAPI, params: BallisticParams): Hit? {
    val target = BallisticTarget.shield(ally)
    return when {
        weapon.projectileCollisionClass == CollisionClass.PROJECTILE_FIGHTER -> null
        weapon.projectileCollisionClass == CollisionClass.RAY_FIGHTER -> null
        !willHitCautious(weapon, target, params) -> null
        else -> Hit(ally, closestHitRange(weapon, target, params), ALLY)
    }
}

/** Workaround for hulks retaining outdated ShieldAPI. */
private fun hasShield(target: CombatEntityAPI): Boolean = target.isShip && !(target as ShipAPI).isHulk
