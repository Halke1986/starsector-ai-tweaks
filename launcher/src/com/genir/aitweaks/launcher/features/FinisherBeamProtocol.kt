package com.genir.aitweaks.launcher.features

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.WeaponAPI

class FinisherBeamProtocol : BaseHullMod() {
    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        if (ship == null) return

        ship.allWeapons.filter { it.isFinisherBeam() }.forEach { setFinisherBeamProtocol(it) }
    }

    override fun getDescriptionParam(index: Int, hullSize: HullSize?): String? = when (index) {
        0 -> "Tachyon Lance"
        1 -> "Phase Lance"
        2 -> "High Intensity Laser"
        3 -> "autofire"
        else -> null
    }

    private fun WeaponAPI.isFinisherBeam() = when {
        !this.spec.isBeam && !this.spec.isBurstBeam -> false
        else -> this.spec.hasTag("aitweaks_finisher_beam")
    }

    private fun setFinisherBeamProtocol(weapon: WeaponAPI) {
        weapon.ensureClonedSpec()
        weapon.spec.aiHints.clear()
        weapon.spec.aiHints.addAll(
            listOf(
                WeaponAPI.AIHints.USE_LESS_VS_SHIELDS,
                // STRIKE tag prevents wasting shots on fighters.
                WeaponAPI.AIHints.STRIKE,
                // USE_VS_FRIGATES is needed because STRIKE forbids attacking frigates.
                WeaponAPI.AIHints.USE_VS_FRIGATES,
            )
        )
    }
}
