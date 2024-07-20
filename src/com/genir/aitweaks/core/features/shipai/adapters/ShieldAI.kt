package com.genir.aitweaks.core.features.shipai.adapters

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

class ShieldAI(vanillaAI: BasicShipAI) {
    private val shieldAI: Any = BasicShipAI::class.java.getMethod("getShieldAI").invoke(vanillaAI)
    private val advance: MethodHandle

    init {
        val methods = shieldAI::class.java.methods
        val advance = methods.first { it.parameterTypes.firstOrNull() == Float::class.java }
        this.advance = MethodHandles.lookup().unreflect(advance)
    }

    fun advance(dt: Float, threatEvalAI: ThreatEvalAI, attackTarget: ShipAPI?) {
        advance(shieldAI, dt, threatEvalAI.threatEvalAI, null, null, attackTarget)
    }
}