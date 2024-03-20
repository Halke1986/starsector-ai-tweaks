package com.genir.aitweaks.features.lidar

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.genir.aitweaks.utils.extensions.hasAIType
import com.genir.aitweaks.utils.extensions.isValidTarget

const val lidarConfigID = "aitweaks_lidar_config"
const val lidarAIStashID = "aitweaks_lidar_ai_stash"

data class LidarConfig(val target: ShipAPI?, val range: Float)

class AIManager : BaseEveryFrameCombatPlugin() {
    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val ships = Global.getCombatEngine().ships.filter { it.system?.specAPI?.id == "lidararray" }

        ships.forEach { ship ->
            val config = ship.customData[lidarConfigID] as? LidarConfig
            val target = config?.target

            if (ship.hasAIType<BasicShipAI>()) {
                if (ship.system.isOn && target?.isValidTarget == true) {
                    // Deploy lidar ship AI
                    ship.setCustomData(lidarAIStashID, ship.shipAI)
                    ship.shipAI = LidarShipAI(ship, target, config.range)
                }
            } else if (ship.hasAIType<LidarShipAI>()) {
                if (!ship.system.isOn || target?.isValidTarget != true) {
                    // Redeploy vanilla AI
                    ship.shipAI = ship.customData[lidarAIStashID] as? ShipAIPlugin ?: return
                    ship.removeCustomData(lidarAIStashID)
                    ship.removeCustomData(lidarConfigID)
                }
            }
        }
    }
}

