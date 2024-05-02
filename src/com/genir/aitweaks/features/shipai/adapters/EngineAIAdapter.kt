package com.genir.aitweaks.features.shipai.adapters

import com.fs.starfarer.combat.ai.movement.BasicEngineAI
import com.fs.starfarer.combat.ai.movement.EngineAI
import com.fs.starfarer.combat.entities.Ship

class EngineAIAdapter(val ship: Ship) : EngineAI {
    private var vanillaAI = BasicEngineAI(ship)

    override fun advance(dt: Float) = Unit

    override fun render() = vanillaAI.render()

    override fun isAlwaysAccelerate() = vanillaAI.isAlwaysAccelerate

    override fun setAlwaysAccelerate(p0: Boolean) {
        vanillaAI.isAlwaysAccelerate = p0
    }

    override fun setDesiredHeading(p0: Float, p1: Float) = vanillaAI.setDesiredHeading(p0, p1)

    override fun setDesiredFacing(p0: Float) = vanillaAI.setDesiredFacing(p0)

    override fun setAvoidingCollision(p0: Boolean) {
        vanillaAI.isAvoidingCollision = p0
    }
}