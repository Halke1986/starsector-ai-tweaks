package com.genir.aitweaks.core.features.shipai.vanilla

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import org.lwjgl.util.vector.Vector2f
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field

class SystemAI(private val systemAI: Any) {
    private val advance: MethodHandle

    init {
        // Find advance method.
        val methods = systemAI::class.java.methods
        val advanceParams = arrayOf(Float::class.java, Vector2f::class.java, Vector2f::class.java, Ship::class.java)
        val advance = methods.first { it.parameterTypes.contentEquals(advanceParams) }

        // Advance is private in case of inner class defined
        // in ShipSystemSpec as a wrapper for CUSTOM system AI.
        advance.setAccessible(true)
        this.advance = MethodHandles.lookup().unreflect(advance)
    }

    fun advance(dt: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, attackTarget: ShipAPI?) {
        advance.invoke(systemAI, dt, missileDangerDir, collisionDangerDir, attackTarget)
    }

    companion object {
        fun getIfExists(basicShipAI: BasicShipAI): SystemAI? {
            val field: Field = basicShipAI::class.java.getDeclaredField("systemAI").also { it.setAccessible(true) }
            return field.get(basicShipAI)?.let { SystemAI(it) }
        }
    }
}