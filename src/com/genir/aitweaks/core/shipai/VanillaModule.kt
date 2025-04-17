package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.types.Direction
import org.lwjgl.util.vector.Vector2f

/** AI modules carried over from vanilla ship AI. */
class VanillaModule(val ship: ShipAPI, overrideVanillaSystem: Boolean) {
    val basicShipAI = Global.getSettings().createDefaultShipAI(ship, ShipAIConfig()) as BasicShipAI
    val flags: ShipwideAIFlags = basicShipAI.aiFlags
    val missileDangerDir: Vector2f?
        get() = flockingAI.flockingAI_getMissileDangerDir()

    private val obfBasicShipAI = basicShipAI as Obfuscated.BasicShipAI

    // Vanilla AI elements.
    private val flockingAI: Obfuscated.FlockingAI = obfBasicShipAI.flockingAI
    private val threatEvaluator: Obfuscated.ThreatEvaluator = obfBasicShipAI.threatEvaluator
    private val shieldAI: Obfuscated.ShieldAI? = obfBasicShipAI.shieldAI
    private val systemAI = if (overrideVanillaSystem) null else obfBasicShipAI.getPrivateField("systemAI") as? Obfuscated.SystemAI
    private val fighterPullbackModule = basicShipAI.getPrivateField("fighterPullbackModule") as? Obfuscated.FighterPullbackModule
    private val attackModule: Obfuscated.AttackAIModule = obfBasicShipAI.attackAI
    private val avoidMissiles = basicShipAI::class.java.getDeclaredMethod("avoidMissiles").also { it.setAccessible(true) }

    init {
        // Ensure AI Tweaks is in control of autofire management.
        AutofireManager.inject(ship, attackModule)

        // Lock the personality to aggressive, ensuring AI elements
        // delegated to vanilla do not behave reckless.
        when {
            ship.AIPersonality == "aggressive" -> Unit

            ship.captain != null -> ship.captain.setPersonality("aggressive")

            else -> (ship as Ship).setFallbackPersonalityId("aggressive")
        }
    }

    /** Advance AI subsystems carried over from the vanilla BasicShipAI. To work
     * correctly, the subsystems should be called in same order as in BasicShipAI. */
    fun advance(dt: Float, attackTarget: ShipAPI?, expectedVelocity: Vector2f, expectedFacing: Direction) {
        val target = attackTarget as? Obfuscated.Ship

        flags.advance(dt)
        threatEvaluator.threatEvaluator_advance(dt)
        avoidMissiles.invoke(basicShipAI)
        fighterPullbackModule?.fighterPullbackModule_advance(dt, target)

        // Vanilla ship systems read maneuvers planned by ship AI through the flockingAI.
        flockingAI.flockingAI_setDesiredHeading(if (expectedVelocity.isZero) Float.MAX_VALUE else expectedVelocity.facing.degrees)
        flockingAI.flockingAI_setDesiredSpeed(expectedVelocity.length)
        flockingAI.flockingAI_setDesiredFacing(expectedFacing.degrees)

        flockingAI.flockingAI_advanceCollisionAnalysisModule(dt)

        val missileDangerDir: Vector2f? = flockingAI.flockingAI_getMissileDangerDir()
        val collisionDangerDir: Vector2f? = null // TODO maybe implement?

        attackModule.attackAIModule_advance(dt, threatEvaluator, missileDangerDir)
        ship.shipTarget = attackTarget // Vanilla AttackModule changes ship target. Switch it back to the proper one.

        shieldAI?.shieldAI_advance(dt, threatEvaluator, missileDangerDir, collisionDangerDir, target)
        systemAI?.systemAI_advance(dt, missileDangerDir, collisionDangerDir, target)
    }

    /** Advance the entire BasicShipAI, effectively giving control over the ship to vanilla AI. */
    fun advanceBasicShipAI(dt: Float) {
        ship as Ship
        val thisCustomAI = ship.ai

        // Make the BasicShipAI think it's in control,
        // otherwise it will throw an exception.
        ship.ai = basicShipAI
        basicShipAI.advance(dt)
        ship.ai = thisCustomAI
    }
}
