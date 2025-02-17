package com.genir.aitweaks.core.shipai.systems

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.EngineController
import com.genir.aitweaks.core.utils.Direction

abstract class SystemAI(val ai: CustomShipAI) {
    protected val ship: ShipAPI = ai.ship
    protected val system: ShipSystemAPI = ship.system

    abstract fun advance(dt: Float)

    open fun holdTargets(): Boolean = system.isOn

    /** Should custom system AI replace vanilla system AI. */
    open fun overrideVanillaSystemAI(): Boolean = true

    open fun overrideHeading(): EngineController.Destination? = null

    open fun overrideFacing(): Direction? = null
}