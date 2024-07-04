package com.genir.aitweaks.core.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI

private const val stashKey = "aitweaks_custom_ai_stash"
private const val maxAge = 0.5f

private data class Stash(val ai: Maneuver, val timestamp: Float)

/** Returns last instance of Custom AI maneuver that was controlling the ship. */
val ShipAPI.customAI: Maneuver?
    get() {
        val s = customData[stashKey] as? Stash

        return when {
            s == null -> null

            currentTimestamp() - s.timestamp > maxAge -> null

            else -> s.ai
        }
    }

fun ShipAPI.storeCustomAI(ai: Maneuver) {
    setCustomData(stashKey, Stash(ai, currentTimestamp()))
}

private fun currentTimestamp(): Float {
    return Global.getCombatEngine().getTotalElapsedTime(false)
}