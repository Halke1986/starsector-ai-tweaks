package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.debug.drawCircle
import java.awt.Color

class WrapperShipAI(val ship: ShipAPI) : Ship.ShipAIWrapper(Global.getSettings().createDefaultShipAI(ship, ShipAIConfig())) {
    val basicShipAI: BasicShipAI = super.getAI() as BasicShipAI

    override fun advance(amount: Float) {
        drawCircle(ship.location, ship.collisionRadius / 2f, Color.YELLOW)

        basicShipAI.advance(amount)
    }

    override fun setDoNotFireDelay(amount: Float) = basicShipAI.setDoNotFireDelay(amount)

    override fun forceCircumstanceEvaluation() = basicShipAI.forceCircumstanceEvaluation()

    override fun needsRefit(): Boolean = basicShipAI.needsRefit()

    override fun getAIFlags(): ShipwideAIFlags = basicShipAI.aiFlags

    override fun cancelCurrentManeuver() = basicShipAI.cancelCurrentManeuver()

    override fun getConfig(): ShipAIConfig = basicShipAI.config
}
