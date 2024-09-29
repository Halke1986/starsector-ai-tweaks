package com.genir.aitweaks.core.features.shipai.vanilla

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field

class VentModule(basicShipAI: BasicShipAI) {
    private val ventModule: Any
    private val advance: MethodHandle

    init {
        val field: Field = basicShipAI::class.java.getDeclaredField("ventModule").also { it.setAccessible(true) }
        ventModule = field.get(basicShipAI)

        // Find advance method.
        // There's only one VentModule method taking (Float, Ship) parameters.
        val methods = ventModule::class.java.methods
        val advanceParams = arrayOf(Float::class.java, Ship::class.java)
        val advance = methods.first { it.parameterTypes.contentEquals(advanceParams) }
        this.advance = MethodHandles.lookup().unreflect(advance)
    }

    fun advance(dt: Float, attackTarget: ShipAPI?) {
        advance.invoke(ventModule, dt, attackTarget)
    }
}
