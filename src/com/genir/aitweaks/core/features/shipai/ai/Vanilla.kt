package com.genir.aitweaks.core.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.combat.ai.BasicShipAI
import com.genir.aitweaks.core.features.shipai.adapters.ShieldAI
import com.genir.aitweaks.core.features.shipai.adapters.ThreatEvalAI

class Vanilla(ship: ShipAPI) {
    private val vanillaAI: BasicShipAI = Global.getSettings().createDefaultShipAI(ship, ShipAIConfig()) as BasicShipAI

    private val threatEvalAI: ThreatEvalAI = ThreatEvalAI(vanillaAI)
    private val shieldAI: ShieldAI? = if (ship.shield != null) ShieldAI(vanillaAI) else null

    fun advance(dt: Float, attackTarget: ShipAPI?) {
        vanillaAI.aiFlags.advance(dt)
        threatEvalAI.advance(dt)
        shieldAI?.advance(dt, threatEvalAI, attackTarget)
    }

    fun flags(): ShipwideAIFlags = vanillaAI.aiFlags
}