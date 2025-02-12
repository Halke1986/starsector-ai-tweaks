package com.genir.aitweaks.core.shipai.systems

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.utils.Direction
import org.lwjgl.util.vector.Vector2f

abstract class SystemAI(val ai: CustomShipAI) {
    protected val ship: ShipAPI = ai.ship
    protected val system: ShipSystemAPI = ship.system

    abstract fun advance(dt: Float)

    open fun holdTargets(): Boolean = system.isOn

    /** Should custom system AI replace vanilla system AI. */
    open fun overrideVanillaSystemAI(): Boolean = true

    open fun overrideHeading(): Vector2f? = null

    open fun overrideFacing(): Direction? = null
}