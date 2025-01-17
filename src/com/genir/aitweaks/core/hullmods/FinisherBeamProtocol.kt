package com.genir.aitweaks.core.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.shipai.autofire.Tag
import com.genir.aitweaks.core.shipai.autofire.hasAITag

class FinisherBeamProtocol : BaseHullMod() {
    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        if (ship == null) return

        ship.allWeapons.filter { it.isFinisherBeam() }.forEach { setFinisherBeamProtocol(it) }
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? = when (index) {
        0 -> "Tachyon Lance"
        1 -> "Phase Lance"
        2 -> "High Intensity Laser"
        3 -> "autofire"
        else -> null
    }

    private fun WeaponAPI.isFinisherBeam() = when {
        !this.spec.isBeam && !this.spec.isBurstBeam -> false
        else -> hasAITag(Tag.FINISHER_BEAM)
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
