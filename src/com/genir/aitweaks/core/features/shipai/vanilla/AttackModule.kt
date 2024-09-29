package com.genir.aitweaks.core.features.shipai.vanilla

import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.ai.attack.AttackAIModule
import org.lwjgl.util.vector.Vector2f
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

class AttackModule(basicShipAI: BasicShipAI, threatEvalAI: ThreatEvalAI) {
    private val attackModule: AttackAIModule = basicShipAI.attackAI
    private val advance: MethodHandle

    init {
        // Find advance method.
        val methods = attackModule::class.java.methods
        val advanceParams = arrayOf(Float::class.java, threatEvalAI.threatEvalAI::class.java, Vector2f::class.java)
        val advance = methods.first { it.parameterTypes.contentEquals(advanceParams) }
        this.advance = MethodHandles.lookup().unreflect(advance)
    }

    fun advance(dt: Float, threatEvalAI: ThreatEvalAI, missileDangerDir: Vector2f?) {
        advance.invoke(attackModule, dt, threatEvalAI.threatEvalAI, missileDangerDir)
    }
}
