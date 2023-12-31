package com.genir.aitweaks

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.features.*
import com.genir.aitweaks.features.autofire.applyFireOnlyOnTargetAI
import com.genir.aitweaks.features.autofire.applyFocusOnTargetAI
import com.genir.aitweaks.features.autofire.applyTargetLeadAI
import com.genir.aitweaks.features.autofire.applyTargetLeadHardpointAI

var registerShipsCallbacks = mutableSetOf<(ShipAPI) -> Unit>(
    { s -> applyFocusOnTargetAI(s) },
    { s -> applyTargetLeadAI(s) },
    { s -> applyTargetLeadHardpointAI(s) },
    { s -> applyNeedlerAI(s) },
    { s -> applyFireOnlyOnTargetAI(s) },
)

class RegisterShipsPlugin : BaseEveryFrameCombatPlugin() {
    private val knownShips = mutableSetOf<String>()

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)
        if (Global.getCurrentState() != GameState.COMBAT) return

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
