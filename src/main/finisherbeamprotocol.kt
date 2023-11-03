package com.genir.aitweaks

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class FinisherBeamProtocol : BaseHullMod() {
    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        if (ship == null)
            return

        ship.allWeapons
            .filter { isFinisherBeam(it) }
            .forEach { setFinisherBeamProtocol(it) }
    }
}

fun setFinisherBeamProtocol(weapon: WeaponAPI) {
    weapon.ensureClonedSpec()
    weapon.spec.aiHints.clear()
    weapon.spec.aiHints.addAll(
        listOf(
            WeaponAPI.AIHints.USE_LESS_VS_SHIELDS,
            // STRIKE tag prevents wasting shots at fighters.
            WeaponAPI.AIHints.STRIKE,
            // USE_VS_FRIGATES is needed because STRIKE forbids attacking frigates.
            WeaponAPI.AIHints.USE_VS_FRIGATES,
        )
    )
}

// Beam weapon is considered a finisher beam when it's:
// - Non-PD burst beam
// - Non-PD anti-armor beam
fun isFinisherBeam(weapon: WeaponAPI) = when {
    weapon.type != WeaponAPI.WeaponType.ENERGY -> false
    !weapon.isBeam -> false
    weapon.hasAIHint(WeaponAPI.AIHints.USE_LESS_VS_SHIELDS) -> false
    weapon.hasAIHint(WeaponAPI.AIHints.PD) -> false
    weapon.isBurstBeam -> true
    weapon.damageType == DamageType.HIGH_EXPLOSIVE -> true
    else -> false
}

