package com.genir.aitweaks.core.features.shipai

import com.genir.aitweaks.core.features.shipai.systems.BurnDrive
import com.genir.aitweaks.core.features.shipai.systems.SrBurstBoost
import com.genir.aitweaks.core.utils.ShipSystemAiType
import com.genir.aitweaks.core.utils.ShipSystemAiType.BURN_DRIVE_TOGGLE
import com.genir.aitweaks.core.utils.ShipSystemAiType.MANEUVERING_JETS
import com.genir.aitweaks.core.utils.extensions.AIType

class SystemAIManager {
    companion object {
        fun overrideVanillaSystem(ai: AI): SystemAI? {
            val aiType: ShipSystemAiType = ai.ship.system?.specAPI?.AIType ?: return null

            return when {
                aiType == BURN_DRIVE_TOGGLE -> BurnDrive(ai)

                aiType == MANEUVERING_JETS && ai.ship.hullSpec.hullId == "sr_melvillei" -> SrBurstBoost(ai)

                else -> null
            }
        }
    }
}