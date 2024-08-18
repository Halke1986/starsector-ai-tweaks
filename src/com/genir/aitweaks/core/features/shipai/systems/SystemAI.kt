package com.genir.aitweaks.core.features.shipai.systems

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.genir.aitweaks.core.features.shipai.CustomShipAI
import org.lwjgl.util.vector.Vector2f

abstract class SystemAI(val ai: CustomShipAI) {
    protected val ship: ShipAPI = ai.ship
    protected val system: ShipSystemAPI = ship.system

    abstract fun advance(dt: Float)

    open fun holdTargets(): Boolean = system.isOn

    open fun overrideHeading(): Vector2f? = null

    open fun overrideFacing(): Float? = null
}