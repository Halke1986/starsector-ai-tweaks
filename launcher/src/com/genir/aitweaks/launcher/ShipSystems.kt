package com.genir.aitweaks.launcher

import com.fs.starfarer.api.combat.*
import org.lwjgl.util.vector.Vector2f
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

class HighEnergyFocusAI : ShipSystemAIAdapter("com.genir.aitweaks.features.HighEnergyFocusAI")

class LidarArrayAI : ShipSystemAIAdapter("com.genir.aitweaks.features.LidarArrayAI")

open class ShipSystemAIAdapter(classPath: String) : ShipSystemAIScript {
    private val impl: ShipSystemAIScript
    private val initHandle: MethodHandle
    private val advanceHandle: MethodHandle

    init {
        val systemClass = reloader.loadClass(classPath)
        impl = systemClass.newInstance() as ShipSystemAIScript

        val initType = MethodType.methodType(Void.TYPE, ShipAPI::class.java, ShipSystemAPI::class.java, ShipwideAIFlags::class.java, CombatEngineAPI::class.java)
        initHandle = MethodHandles.lookup().findVirtual(systemClass, "init", initType)

        val advanceType = MethodType.methodType(Void.TYPE, Float::class.java, Vector2f::class.java, Vector2f::class.java, ShipAPI::class.java)
        advanceHandle = MethodHandles.lookup().findVirtual(systemClass, "advance", advanceType)
    }


    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        initHandle.invoke(impl, ship, system, flags, engine)
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        advanceHandle.invoke(impl, amount, missileDangerDir, collisionDangerDir, target)
    }
}
