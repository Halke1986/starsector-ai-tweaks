package com.genir.aitweaks.core.features.shipai.systems

import com.genir.aitweaks.core.extensions.AIType
import com.genir.aitweaks.core.features.shipai.CustomShipAI
import com.genir.aitweaks.core.utils.ShipSystemAIType
import com.genir.aitweaks.core.utils.ShipSystemAIType.*

class SystemAIManager {
    companion object {
        fun overrideVanillaSystem(ai: CustomShipAI): SystemAI? {
            val aiType: ShipSystemAIType = ai.ship.system?.specAPI?.AIType ?: return null

            return when {
                aiType == BURN_DRIVE_TOGGLE -> BurnDriveToggle(ai)

                aiType == LIDAR_ARRAY -> LidarArray(ai)

                aiType == MANEUVERING_JETS && ai.ship.hullSpec.hullId == "sr_melvillei" -> SrBurstBoost(ai)

                else -> null
            }
        }
    }
}