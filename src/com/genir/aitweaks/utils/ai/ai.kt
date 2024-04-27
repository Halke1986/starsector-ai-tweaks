package com.genir.aitweaks.utils.ai

import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.AI
import com.fs.starfarer.combat.entities.Ship

inline fun <reified T> ShipAPI.hasAIType(): Boolean = (this as Ship).shipAI?.let { it is T || (it as? Ship.ShipAIWrapper)?.ai is T }
    ?: false

val ShipAPI.hasVanillaAI: Boolean
    get() = this.ai is AI

/** Return personality preset used by vanilla ship AI. */
val ShipAPI.AIPersonality: String
    get() = (this.ai as? ShipAIPlugin)?.config?.personalityOverride ?: (this as Ship).personality

