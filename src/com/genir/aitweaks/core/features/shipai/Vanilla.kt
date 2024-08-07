package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.genir.aitweaks.core.features.shipai.adapters.*
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.isZeroVector
import org.lwjgl.util.vector.Vector2f

/** AI modules carried over from vanilla ship AI. */
class Vanilla(ship: ShipAPI, overrideVanillaSystem: Boolean) {
    private val vanillaAI: BasicShipAI = Global.getSettings().createDefaultShipAI(ship, ShipAIConfig()) as BasicShipAI

    val flags = vanillaAI.aiFlags
    private val flockingAI: FlockingAI = FlockingAI(vanillaAI)
    private val threatEvalAI: ThreatEvalAI = ThreatEvalAI(vanillaAI)
    private val ventModule: VentModule = VentModule(vanillaAI)
    private val shieldAI: ShieldAI? = ShieldAI.getIfExists(vanillaAI)
    private val systemAI: SystemAI? = if (overrideVanillaSystem) null else SystemAI.getIfExists(vanillaAI)
    private val fighterPullbackModule: FighterPullbackModule? = FighterPullbackModule.getIfExists(vanillaAI)

    fun advance(dt: Float, attackTarget: ShipAPI?, expectedVelocity: Vector2f, expectedFacing: Float) {
        flags.advance(dt)
        threatEvalAI.advance(dt)

        // Vanilla ship systems read maneuvers planned by ship AI through the flockingAI.
        flockingAI.setDesiredHeading(if (expectedVelocity.isZeroVector()) Float.MAX_VALUE else expectedVelocity.getFacing())
        flockingAI.setDesiredSpeed(expectedVelocity.length())
        flockingAI.setDesiredFacing(expectedFacing)

        // TODO maybe implement?
        val missileDangerDir: Vector2f? = null
        val collisionDangerDir: Vector2f? = null

        ventModule.advance(dt, attackTarget)
        fighterPullbackModule?.advance(dt, attackTarget)
        shieldAI?.advance(dt, threatEvalAI, missileDangerDir, collisionDangerDir, attackTarget)
        systemAI?.advance(dt, missileDangerDir, collisionDangerDir, attackTarget)
    }
}