package com.genir.aitweaks.features.maneuver

import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.combat.ai.movement.BasicEngineAI
import com.fs.starfarer.combat.ai.movement.EngineAI
import com.fs.starfarer.combat.entities.Ship

class OverrideEngineAI(val ship: Ship) : EngineAI {
    private var vanillaAI = BasicEngineAI(ship)
    var AIIsAvoidingCollision = false

    override fun advance(dt: Float) {
        if (!ship.aiFlags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF)) {
            vanillaAI.advance(dt)
        }
    }

    override fun render() = vanillaAI.render()

    override fun isAlwaysAccelerate() = vanillaAI.isAlwaysAccelerate

    override fun setAlwaysAccelerate(p0: Boolean) = vanillaAI.setAlwaysAccelerate(p0)

    override fun setDesiredHeading(p0: Float, p1: Float) = vanillaAI.setDesiredHeading(p0, p1)

    override fun setDesiredFacing(p0: Float) = vanillaAI.setDesiredFacing(p0)

    override fun setAvoidingCollision(p0: Boolean) {
        AIIsAvoidingCollision = p0
        vanillaAI.isAvoidingCollision = p0
    }
}