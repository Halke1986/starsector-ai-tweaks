package com.genir.aitweaks.utils.ai

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.asm.combat.ai.AssemblyShipAI

inline fun <reified T> ShipAPI.hasAIType(): Boolean = (this as Ship).shipAI?.let { it is T || (it as? Ship.ShipAIWrapper)?.ai is T }
    ?: false

val ShipAPI.hasVanillaAI: Boolean
    get() = this.hasAIType<BasicShipAI>() || this.hasAIType<AssemblyShipAI>()

/** Return personality preset used by vanilla ship AI. */
val ShipAPI.AIPersonality: String
    get() = (this.ai as? ShipAIPlugin)?.config?.personalityOverride ?: (this as Ship).personality

fun newVanillaAI(ship: ShipAPI, config: ShipAIConfig = ShipAIConfig()): ShipAIPlugin {
//    if (!ship.isFrigate)
        return AssemblyShipAI(ship as Ship, config)

    return BasicShipAI(ship as Ship, config)
}