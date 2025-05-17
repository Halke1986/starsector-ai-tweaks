package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.starfarer.combat.ai.AI

abstract class BaseShipAI : ShipAIPlugin, AI {
    override fun setDoNotFireDelay(amount: Float) = Unit

    override fun forceCircumstanceEvaluation() = Unit

    override fun needsRefit(): Boolean = false

    override fun cancelCurrentManeuver() = Unit

    override fun getAIFlags(): ShipwideAIFlags = ShipwideAIFlags()

    override fun getConfig(): ShipAIConfig = ShipAIConfig()

    override fun render() = Unit
}
