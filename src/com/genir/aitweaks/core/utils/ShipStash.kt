package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.features.shipai.ai.Maneuver

private const val stashKey = "aitweaks_ship_stash"

const val maxAge = 0.5f

class ShipStash(ship: ShipAPI) {
    /** Current instance of Custom AI Maneuver controlling the ship.*/
    var maneuverAI: Maneuver? = null
        get() = if (elapsedTime() - maneuverAIT < maxAge) field else null
        set(value) {
            field = value; maneuverAIT = elapsedTime()
        }

    private var maneuverAIT = 0f

    private fun elapsedTime(): Float {
        return Global.getCombatEngine().getTotalElapsedTime(false)
    }
}

val ShipAPI.aitStash: ShipStash
    get() {
        (this.customData[stashKey] as? ShipStash)?.let { return it }

        val newStash = ShipStash(this)
        this.setCustomData(stashKey, newStash)
        return newStash
    }
