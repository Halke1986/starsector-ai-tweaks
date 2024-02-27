package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship

/** Return personality preset used by vanilla ship AI. */
val ShipAPI.AIPersonality: String
    get() = (this.ai as? BasicShipAI)?.config?.personalityOverride ?: (this as Ship).personality