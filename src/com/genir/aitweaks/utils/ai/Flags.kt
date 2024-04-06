package com.genir.aitweaks.utils.ai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.utils.frameTracker

const val flagsKey = "aitweaks_ai_flags"

private data class FlagT<T>(val value: T, val timestamp: Int)

enum class FlagID {
    ATTACK_TARGET,
    AIM_POINT,
}

fun <T> ShipAPI.getAITFlag(flagID: FlagID, maxAge: Int = 60): T? {
    val allFlags = this.customData[flagsKey] as? Map<*, *> ?: return null
    val flag = allFlags[flagID] as? FlagT<*> ?: return null
    if (frameTracker - flag.timestamp > maxAge) return null
    return flag.value as T
}

fun <T> ShipAPI.setAITFlag(flagID: FlagID, value: T) {
    if (this.customData[flagsKey] == null)
        this.setCustomData(flagsKey, mutableMapOf<FlagID, Any>())

    val flags = this.customData[flagsKey] as MutableMap<FlagID, Any>
    flags[flagID] = FlagT(value, frameTracker)
}