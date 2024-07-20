package com.genir.aitweaks.core.features.shipai.adapters

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import org.lwjgl.util.vector.Vector2f
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

class ShieldAI(private val shieldAI: Any) {
    private val advance: MethodHandle

    init {
        // Find advance method.
        val methods = shieldAI::class.java.methods
        val advance = methods.first { it.parameterTypes.firstOrNull() == Float::class.java }
        this.advance = MethodHandles.lookup().unreflect(advance)
    }

    fun advance(dt: Float, threatEvalAI: ThreatEvalAI, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, attackTarget: ShipAPI?) {
        advance(shieldAI, dt, threatEvalAI.threatEvalAI, missileDangerDir, collisionDangerDir, attackTarget)
    }

    companion object {
        fun getIfExists(vanillaAI: BasicShipAI): ShieldAI? {
            val shieldAI: Any? = BasicShipAI::class.java.getMethod("getShieldAI").invoke(vanillaAI)

            return shieldAI?.let { ShieldAI(it) }
        }
    }
}