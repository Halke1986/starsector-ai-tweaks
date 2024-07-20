package com.genir.aitweaks.core.features.shipai.adapters

import com.fs.starfarer.combat.ai.BasicShipAI
import org.lwjgl.util.vector.Vector2f
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

class FlockingAI(vanillaAI: BasicShipAI) {
    val flockingAI: Any = BasicShipAI::class.java.getMethod("getFlockingAI").invoke(vanillaAI)
    private val getCollisionVector: MethodHandle

    init {
        val methods = flockingAI::class.java.methods
        val getCollisionVector = methods.last { it.returnType == Vector2f::class.java }
        this.getCollisionVector = MethodHandles.lookup().unreflect(getCollisionVector)
    }

    fun getCollisionVector(): Vector2f {
        return getCollisionVector.invoke(flockingAI) as Vector2f
    }
}