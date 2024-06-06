package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.features.shipai.ai.Maneuver

private const val stashKey = "aitweaks_ship_stash"

const val maxAge = 0.5f

class ShipStash(ship: ShipAPI) {
    /** Enemy target the ship is attacking. */
    var attackTarget: ShipAPI? = null
        get() = if (frameTracker - attackTargetT < maxAge) field else null
        set(value) {
            field = value; attackTargetT = frameTracker
        }

    /** Current instance of Maneuver AI controlling the ship.*/
    var maneuverAI: Maneuver? = null
        get() = if (frameTracker - maneuverAIT < maxAge) field else null
        set(value) {
            field = value; maneuverAIT = frameTracker
        }

    private var attackTargetT = 0f
    private var maneuverAIT = 0f
}

val ShipAPI.AITStash: ShipStash
    get() {
        if (!this.customData.containsKey(stashKey))
            this.setCustomData(stashKey, ShipStash(this))
        return this.customData[stashKey] as ShipStash
    }
