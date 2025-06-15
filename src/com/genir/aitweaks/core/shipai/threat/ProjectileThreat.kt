package com.genir.aitweaks.core.shipai.threat

import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.sumOf
import com.genir.aitweaks.core.state.State.Companion.state
import kotlin.math.pow

class ProjectileThreat(val ship: ShipAPI) {
    fun potentialDamage(): Float {
        val projectiles: Sequence<DamagingProjectileAPI> = state.projectileTracker.getThreats(ship)

        val shipHp = ship.hitpoints * ship.hullLevel

        return projectiles.asIterable().sumOf { projectile ->
            val damageBase = projectile.damageAmount * projectile.damageType.armorMult
            if (damageBase >= shipHp) {
                return@sumOf damageBase
            }

            val weight = 1f - (1f - damageBase / shipHp).pow(32)

            damageBase * weight
        }
    }
}