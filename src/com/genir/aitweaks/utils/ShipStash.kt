package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.features.maneuver.Maneuver

private const val stashKey = "aitweaks_ship_stash"

const val maxAge = 0.5f

class ShipStash {
    /** Enemy target the ship is attacking. */
    var attackTarget: ShipAPI? = null
        get() = if (frameTracker - attackTargetT < maxAge) field else null
        set(value) {
            field = value; attackTargetT = frameTracker
        }

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
            this.setCustomData(stashKey, ShipStash())
        return this.customData[stashKey] as ShipStash
    }
