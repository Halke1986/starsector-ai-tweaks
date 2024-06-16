package com.genir.aitweaks.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.features.shipai.ai.Maneuver

private const val stashKey = "aitweaks_ship_stash"

const val maxAge = 0.5f

class ShipStash(ship: ShipAPI) {
    /** Enemy target the ship is attacking, selected by Custom AI. */
    var attackTarget: ShipAPI? = null
        get() = if (elapsedTime() - attackTargetT < maxAge) field else null
        set(value) {
            field = value; attackTargetT = elapsedTime()
        }

    /** Current instance of Custom AI Maneuver controlling the ship.*/
    var maneuverAI: Maneuver? = null
        get() = if (elapsedTime() - maneuverAIT < maxAge) field else null
        set(value) {
            field = value; maneuverAIT = elapsedTime()
        }

    private var attackTargetT = 0f
    private var maneuverAIT = 0f

    private fun elapsedTime(): Float {
        return Global.getCombatEngine().getTotalElapsedTime(false)
    }
}

val ShipAPI.aitStash: ShipStash
    get() {
        if (!this.customData.containsKey(stashKey))
            this.setCustomData(stashKey, ShipStash(this))
        return this.customData[stashKey] as ShipStash
    }
