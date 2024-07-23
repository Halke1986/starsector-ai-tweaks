package com.genir.aitweaks.core.features.shipai.adapters

import com.fs.starfarer.combat.ai.BasicShipAI
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

class FlockingAI(vanillaAI: BasicShipAI) {
    private val flockingAI: Any = BasicShipAI::class.java.getMethod("getFlockingAI").invoke(vanillaAI)

    private val setDesiredHeading: MethodHandle
    private val setDesiredFacing: MethodHandle
    private val setDesiredSpeed: MethodHandle

    init {
        val methods = flockingAI::class.java.methods
        val setters = methods.filter { it.returnType == Void.TYPE && it.parameterTypes.contentEquals(arrayOf(Float::class.java)) }

        this.setDesiredHeading = MethodHandles.lookup().unreflect(setters[0])
        this.setDesiredFacing = MethodHandles.lookup().unreflect(setters[1])
        this.setDesiredSpeed = MethodHandles.lookup().unreflect(setters[2])
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
}