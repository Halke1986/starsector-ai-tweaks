package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipwideAIFlags

abstract class BaseShipAIPlugin : ShipAIPlugin {
    override fun setDoNotFireDelay(amount: Float) = Unit

    override fun forceCircumstanceEvaluation() = Unit

    override fun needsRefit(): Boolean = false

    override fun cancelCurrentManeuver() = Unit

    override fun getAIFlags(): ShipwideAIFlags = ShipwideAIFlags()

    override fun getConfig(): ShipAIConfig = ShipAIConfig()
}
