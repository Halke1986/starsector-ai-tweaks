package com.genir.aitweaks.core.shipai.systems

import com.genir.aitweaks.core.extensions.AIType
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.utils.ShipSystemAIType
import com.genir.aitweaks.core.utils.ShipSystemAIType.*

class SystemAIManager {
    companion object {
        fun overrideVanillaSystem(ai: CustomShipAI): SystemAI? {
            val aiType: ShipSystemAIType = ai.ship.system?.specAPI?.AIType ?: return null

            return when (aiType) {
                BURN_DRIVE_TOGGLE -> BurnDriveToggle(ai)

                BURN_DRIVE -> BurnDrive(ai)

                LIDAR_ARRAY -> LidarArray(ai)

                TEMPORAL_SHELL -> TemporalShell(ai)

                MANEUVERING_JETS -> {
                    if (ai.ship.hullSpec.hullId == "sr_melvillei") SrBurstBoost(ai) else null
                }

                else -> null
            }
        }
    }
}