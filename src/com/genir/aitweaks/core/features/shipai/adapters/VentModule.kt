package com.genir.aitweaks.core.features.shipai.adapters

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field

class VentModule(vanillaAI: BasicShipAI) {
    private val ventModule: Any
    private val advance: MethodHandle

    init {
        val field: Field = vanillaAI::class.java.getDeclaredField("ventModule").also { it.setAccessible(true) }
        ventModule = field.get(vanillaAI)

        val methods = ventModule::class.java.methods
        val advanceParams = arrayOf(Float::class.java, Ship::class.java)
        val advance = methods.first { it.parameterTypes.contentEquals(advanceParams) }
        this.advance = MethodHandles.lookup().unreflect(advance)
    }

    fun advance(dt: Float, attackTarget: ShipAPI?) {
        advance.invoke(ventModule, dt, attackTarget)
    }
}