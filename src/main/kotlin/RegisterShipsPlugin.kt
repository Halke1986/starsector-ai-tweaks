package com.genir.aitweaks

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.features.applyNeedlerAI
import com.genir.aitweaks.features.applyTargetLeadAI

var registerShipsCallbacks = mutableSetOf<(ShipAPI) -> Unit>(
    { s -> applyTargetLeadAI(s) },
    { s -> applyNeedlerAI(s) },
)

class RegisterShipsPlugin : BaseEveryFrameCombatPlugin() {
    private val knownShips = mutableSetOf<String>()

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)
        if (Global.getCurrentState() != GameState.COMBAT)
            return

        // List new ships, that have been added
        // to the battle since last call to advance().
        val engine = Global.getCombatEngine()
        val newShips = engine.ships.filter { ship ->
            ship.isAlive && !knownShips.contains(ship.id)
        }
        knownShips.addAll(newShips.map { it.id })

        // Register new ships.
        newShips.forEach { ship ->
            registerShipsCallbacks.forEach { fn -> fn(ship) }
        }
    }
}


