package com.genir.aitweaks.core.features.shipai.adapters

import com.fs.starfarer.combat.ai.BasicShipAI
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

class ThreatEvalAI(vanillaAI: BasicShipAI) {
    val threatEvalAI: Any = BasicShipAI::class.java.getMethod("getThreatEvaluator").invoke(vanillaAI)
    private val advance: MethodHandle

    init {
        val methods = threatEvalAI::class.java.methods
        val advance = methods.first { it.parameterTypes.contentEquals(arrayOf(Float::class.java)) }
        this.advance = MethodHandles.lookup().unreflect(advance)
    }

    fun advance(dt: Float) {
        advance.invoke(threatEvalAI, dt)
    }
}