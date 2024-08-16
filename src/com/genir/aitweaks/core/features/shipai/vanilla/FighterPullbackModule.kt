package com.genir.aitweaks.core.features.shipai.vanilla

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field

class FighterPullbackModule(private val fighterPullbackModule: Any) {
    private val advance: MethodHandle

    init {
        // Find advance method.
        // There's only one FighterPullbackModule method taking (Float, Ship) parameters.
        val methods = fighterPullbackModule::class.java.methods
        val advanceParams = arrayOf(Float::class.java, Ship::class.java)
        val advance = methods.first { it.parameterTypes.contentEquals(advanceParams) }
        this.advance = MethodHandles.lookup().unreflect(advance)
    }

    fun advance(dt: Float, attackTarget: ShipAPI?) {
        advance.invoke(fighterPullbackModule, dt, attackTarget)
    }

    companion object {
        fun getIfExists(basicShipAI: BasicShipAI): FighterPullbackModule? {
            val field: Field = BasicShipAI::class.java.getDeclaredField("fighterPullbackModule").also { it.setAccessible(true) }
            val fighterPullbackModule: Any? = field.get(basicShipAI)

            return fighterPullbackModule?.let { FighterPullbackModule(it) }
        }
    }
}