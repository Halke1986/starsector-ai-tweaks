package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
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

/** Analyzes the potential collision between projectile and target. Returns collision range.
 * Boolean return parameter is true if projectile will hit target shield; always false for
 * fighters and missiles. Null if no collision. */
fun analyzeHit(weapon: WeaponAPI, target: CombatEntityAPI, rangeLimit: Float): Hit? {
    val targetCircumference = if (hasShield(target)) targetShield(target as ShipAPI) else Target(target)
    val range = willHitCircumference(weapon, targetCircumference) ?: return null
    if (range > rangeLimit) return null

    // Simple circumference collision is enough for missiles and fighters.
    if (!target.isShip) return Hit(target, range, false)

    // Check shield hit.
    if (hasShield(target)) {
        val shieldRange = willHitShield(weapon, target as ShipAPI)
        if (shieldRange != null) return if (shieldRange <= rangeLimit) Hit(target, shieldRange, true) else null
    }

    // Check bounds hit.
    val boundsRange = willHitBounds(weapon, target as ShipAPI)
    return if (boundsRange != null && boundsRange <= rangeLimit) Hit(target, boundsRange, false) else null
}

/** Workaround for hulks retaining outdated ShieldAPI */
fun hasShield(target: CombatEntityAPI): Boolean = target.isShip && !(target as ShipAPI).isHulk