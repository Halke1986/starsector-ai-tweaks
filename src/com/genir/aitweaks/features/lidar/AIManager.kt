package com.genir.aitweaks.features.lidar

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.debug.drawEngineLines

class AIManager : BaseEveryFrameCombatPlugin() {

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val ships = Global.getCombatEngine().ships.filter { it.system?.specAPI?.id == "lidararray" }

        ships.forEach { if (it.system.isOn) drawEngineLines(it) }
    }
}