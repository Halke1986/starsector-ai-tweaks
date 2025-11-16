package com.genir.aitweaks.core.shipai.systems

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.movement.EngineController
import com.genir.aitweaks.core.utils.types.Direction

abstract class CustomSystemAI(val ai: CustomShipAI) {
    protected val ship: ShipAPI = ai.ship
    protected val system: ShipSystemAPI = ship.system

    abstract fun advance(dt: Float)

    open fun holdTargets(): Boolean = false

    /** Should vanilla system AI be advanced this frame. */
    open fun advanceVanillaSystemAI(): Boolean = false

    open fun overrideHeading(): EngineController.Destination? = null

    open fun overrideFacing(): Direction? = null
}