package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

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

/** Analyzes the potential collision between projectile and target. Returns collision range.
 * Boolean return parameter is true if projectile will hit target shield; always false for
 * fighters and missiles. Null if no collision. */
fun analyzeHit(weapon: WeaponAPI, target: CombatEntityAPI): Hit? {
    // Simple circumference collision is enough for missiles and fighters.
    if (target !is ShipAPI || target.isFighter) {
        val range = willHitCircumference(weapon, Target(target)) ?: return null
        return Hit(target, range, false)
    }

    willHitShield(weapon, target)?.let { return Hit(target, it, true) }
    willHitBounds(weapon, target)?.let { return Hit(target, it, false) } ?: return null
}