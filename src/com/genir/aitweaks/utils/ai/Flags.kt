package com.genir.aitweaks.utils.ai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.utils.frameTracker

private const val flagsKey = "aitweaks_aiflags"

private data class FlagT<T>(val value: T, val timestamp: Int)

enum class FlagID {
    ATTACK_TARGET,
    AIM_POINT,
}

fun <T> ShipAPI.getAITFlag(flagID: FlagID, maxAge: Int = 60): T? {
    val flag = this.customData["${flagsKey}_$flagID"] as? FlagT<*> ?: return null
    if (frameTracker - flag.timestamp > maxAge) return null
    return flag.value as T
}

fun <T> ShipAPI.setAITFlag(flagID: FlagID, value: T) {
    this.setCustomData("${flagsKey}_$flagID", FlagT(value, frameTracker))
}
