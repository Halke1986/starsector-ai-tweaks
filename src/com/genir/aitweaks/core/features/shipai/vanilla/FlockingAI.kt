package com.genir.aitweaks.core.features.shipai.vanilla

import com.fs.starfarer.combat.ai.BasicShipAI
import com.genir.aitweaks.launcher.loading.Bytecode
import org.lwjgl.util.vector.Vector2f
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodType

class FlockingAI(basicShipAI: BasicShipAI) {
    private val flockingAI: Any = BasicShipAI::class.java.getMethod("getFlockingAI").invoke(basicShipAI)

    private val setDesiredHeading: MethodHandle
    private val setDesiredFacing: MethodHandle
    private val setDesiredSpeed: MethodHandle
    private val getMissileDangerDir: MethodHandle
    private val advanceCollisionAnalysisModule: MethodHandle

    init {
        val c = flockingAI::class.java
        val lookup: Lookup = MethodHandles.lookup()
        val bytecodeMethods: List<Bytecode.Method> = Bytecode.getMethodsInOrder(c)

        val setterNames = bytecodeMethods.filter { it.desc == "(F)V" }
        val setterType = MethodType.methodType(Void.TYPE, Float::class.java)
        this.setDesiredHeading = lookup.findVirtual(c, setterNames[1].name, setterType)
        this.setDesiredFacing = lookup.findVirtual(c, setterNames[2].name, setterType)
        this.setDesiredSpeed = lookup.findVirtual(c, setterNames[4].name, setterType)
        this.advanceCollisionAnalysisModule = lookup.findVirtual(c, setterNames[3].name, setterType)

        val getVectorNames = bytecodeMethods.filter { it.desc == "()Lorg/lwjgl/util/vector/Vector2f;" }
        val getVectorType = MethodType.methodType(Vector2f::class.java)
        this.getMissileDangerDir = lookup.findVirtual(c, getVectorNames[0].name, getVectorType)
    }

    fun setDesiredHeading(heading: Float) {
        setDesiredHeading.invoke(flockingAI, heading)
    }

    fun setDesiredFacing(facing: Float) {
        setDesiredFacing.invoke(flockingAI, facing)
    }

    fun setDesiredSpeed(speed: Float) {
        setDesiredSpeed.invoke(flockingAI, speed)
    }

    fun getMissileDangerDir(): Vector2f? {
        return getMissileDangerDir.invoke(flockingAI) as? Vector2f
    }

    fun advanceCollisionAnalysisModule(dt: Float) {
        advanceCollisionAnalysisModule.invoke(flockingAI, dt)
    }
}