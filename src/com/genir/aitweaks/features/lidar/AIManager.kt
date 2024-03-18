package com.genir.aitweaks.features.lidar

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship.ShipAIWrapper
import com.genir.aitweaks.debug.drawEngineLines

const val lidarDataID = "aitweaks_lidar_data"

data class LidarData(val target: ShipAPI?, val range: Float)

class AIManager : BaseEveryFrameCombatPlugin() {
    private var aiStash: MutableMap<ShipAPI, ShipAIPlugin> = mutableMapOf()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val ships = Global.getCombatEngine().ships.filter { it.system?.specAPI?.id == "lidararray" }

        ships.forEach { ship ->
            if (ship.system.isOn) {
                if (ship.shipAI is BasicShipAI) {
                    val config = ship.customData[lidarDataID] as? LidarData ?: return
                    val target = config.target ?: return

                    ship.removeCustomData(lidarDataID)

                    aiStash[ship] = ship.shipAI
                    ship.shipAI = ShipAI(ship, target, config.range)
                }
                drawEngineLines(ship)
            } else if ((ship.ai as? ShipAIWrapper)?.ai is ShipAI) {
                // TODO release AI when target is dead
                ship.shipAI = aiStash[ship]
                aiStash.remove(ship)
            }
        }
    }
}

