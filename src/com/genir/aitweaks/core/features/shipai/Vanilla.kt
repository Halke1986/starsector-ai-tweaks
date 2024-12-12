package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.combat.ai.BasicShipAI
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.extensions.facing
import com.genir.aitweaks.core.extensions.getPrivateField
import com.genir.aitweaks.core.extensions.isZero
import org.lwjgl.util.vector.Vector2f
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

/** AI modules carried over from vanilla ship AI. */
class Vanilla(val ship: ShipAPI, overrideVanillaSystem: Boolean) {
    val basicShipAI = Global.getSettings().createDefaultShipAI(ship, ShipAIConfig()) as BasicShipAI
    val flags: ShipwideAIFlags = basicShipAI.aiFlags
    val missileDangerDir: Vector2f?
        get() = flockingAI.flockingAI_getMissileDangerDir()

    private val obfBasicShipAI = basicShipAI as Obfuscated.BasicShipAI

    private val flockingAI: Obfuscated.FlockingAI = obfBasicShipAI.flockingAI
    private val threatEvaluator: Obfuscated.ThreatEvaluator = obfBasicShipAI.threatEvaluator

    //    private val ventModule = basicShipAI.getPrivateField("ventModule") as Obfuscated.VentModule
    private val shieldAI: Obfuscated.ShieldAI? = obfBasicShipAI.shieldAI
    private val systemAI = if (overrideVanillaSystem) null else obfBasicShipAI.getPrivateField("systemAI") as? Obfuscated.SystemAI
    private val fighterPullbackModule = basicShipAI.getPrivateField("fighterPullbackModule") as? Obfuscated.FighterPullbackModule
    private val attackModule: Obfuscated.AttackAIModule = obfBasicShipAI.attackAI

    private val avoidMissiles: MethodHandle

    init {
        val avoidMissiles = basicShipAI::class.java.getDeclaredMethod("avoidMissiles").also { it.setAccessible(true) }
        this.avoidMissiles = MethodHandles.lookup().unreflect(avoidMissiles)

        // Ensure AI Tweaks is in control of autofire management.
        AutofireManagerOverride.inject(ship, basicShipAI.attackAI)
    }

    /** Advance AI subsystems carried over from the vanilla BasicShipAI. To work
     * correctly, the subsystems should be called in same order as in BasicShipAI. */
    fun advance(dt: Float, attackTarget: ShipAPI?, expectedVelocity: Vector2f, expectedFacing: Float) {
        val target = attackTarget as? Obfuscated.Ship

        flags.advance(dt)
        threatEvaluator.threatEvaluator_advance(dt)
        avoidMissiles.invoke(basicShipAI)

//        ventModule.ventModule_advance(dt, target)
        fighterPullbackModule?.fighterPullbackModule_advance(dt, target)

        // Vanilla ship systems read maneuvers planned by ship AI through the flockingAI.
        flockingAI.flockingAI_setDesiredHeading(if (expectedVelocity.isZero) Float.MAX_VALUE else expectedVelocity.facing)
        flockingAI.flockingAI_setDesiredSpeed(expectedVelocity.length())
        flockingAI.flockingAI_setDesiredFacing(expectedFacing)
        flockingAI.flockingAI_advanceCollisionAnalysisModule(dt)

        val missileDangerDir: Vector2f? = flockingAI.flockingAI_getMissileDangerDir()
        val collisionDangerDir: Vector2f? = null // TODO maybe implement?

        attackModule.attackAIModule_advance(dt, threatEvaluator, missileDangerDir)
        shieldAI?.shieldAI_advance(dt, threatEvaluator, missileDangerDir, collisionDangerDir, target)
        systemAI?.systemAI_advance(dt, missileDangerDir, collisionDangerDir, target)
    }
}
