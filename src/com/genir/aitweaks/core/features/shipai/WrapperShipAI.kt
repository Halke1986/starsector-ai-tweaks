package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.combat.ai.BasicShipAI

class WrapperShipAI(val ship: ShipAPI) : ShipAIPlugin {
    val basicShipAI: BasicShipAI = Global.getSettings().createDefaultShipAI(ship, ShipAIConfig()) as BasicShipAI

    override fun advance(amount: Float) {
        TODO("Not yet implemented")
    }

    override fun setDoNotFireDelay(amount: Float) = basicShipAI.setDoNotFireDelay(amount)

    override fun forceCircumstanceEvaluation() = basicShipAI.forceCircumstanceEvaluation()

    override fun needsRefit(): Boolean = basicShipAI.needsRefit()

    override fun getAIFlags(): ShipwideAIFlags = basicShipAI.aiFlags

    override fun cancelCurrentManeuver() = basicShipAI.cancelCurrentManeuver()

    override fun getConfig(): ShipAIConfig = basicShipAI.config
}
