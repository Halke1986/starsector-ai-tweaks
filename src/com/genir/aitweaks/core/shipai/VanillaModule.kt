package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.starfarer.combat.ai.BasicShipAI
import com.genir.starfarer.combat.ai.FighterPullbackModule
import com.genir.starfarer.combat.ai.ShieldAI
import com.genir.starfarer.combat.ai.ThreatEvaluator
import com.genir.starfarer.combat.ai.attack.AttackAIModule
import com.genir.starfarer.combat.ai.movement.FlockingAI
import com.genir.starfarer.combat.entities.Ship
import org.lwjgl.util.vector.Vector2f

/** AI modules carried over from vanilla ship AI. */
class VanillaModule(val ship: ShipAPI, overrideVanillaSystem: Boolean) {
    val basicShipAI = Global.getSettings().createDefaultShipAI(ship, ShipAIConfig()) as BasicShipAI
    private val flags: ShipwideAIFlags = basicShipAI.aiFlags
    val missileDangerDir: Vector2f?
        get() = flockingAI.flockingAI_getMissileDangerDir()

    // Vanilla AI elements.
    private val flockingAI: FlockingAI = basicShipAI.flockingAI
    private val threatEvaluator: ThreatEvaluator = basicShipAI.threatEvaluator
    private val shieldAI: ShieldAI? = basicShipAI.shieldAI
    private val systemAI = if (overrideVanillaSystem) null else basicShipAI.getPrivateField("systemAI") as? com.genir.starfarer.combat.ai.system.SystemAI
    private val fighterPullbackModule = basicShipAI.getPrivateField("fighterPullbackModule") as? FighterPullbackModule
    private val attackModule: AttackAIModule = basicShipAI.attackAI
    private val avoidMissiles = basicShipAI::class.java.getDeclaredMethod("avoidMissiles").also { it.setAccessible(true) }

    init {
        // Ensure AI Tweaks is in control of autofire management.
        AutofireManager.inject(ship, attackModule)
    }

    /** Advance AI subsystems carried over from the vanilla BasicShipAI. To work
     * correctly, the subsystems should be called in same order as in BasicShipAI. */
    fun advance(dt: Float, attackTarget: ShipAPI?, expectedVelocity: Vector2f, expectedFacing: Direction) {
        // Lock the personality to aggressive, ensuring AI elements
        // delegated to vanilla do not behave reckless.
        val personalityOverride = ShipPersonalityOverride()
        personalityOverride.set("aggressive")

        run {
            val target = attackTarget as? Ship

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

        // Revert to the original captain personality.
        // If this is omitted, the personality change will
        // persist in the campaign layer after the battle.
        personalityOverride.cleanup()
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

    private inner class ShipPersonalityOverride {
        private var personalityCache: String? = null

        fun set(personality: String) {
            when {
                // Ship personality is already as expected.
                ship.AIPersonality == personality -> {
                    return
                }

                ship.captain != null -> {
                    personalityCache = ship.captain.personalityAPI?.id
                    ship.captain.setPersonality(personality)
                }

                else -> {
                    (ship as Ship).setFallbackPersonalityId("aggressive")
                }
            }
        }

        fun cleanup() {
            if (personalityCache != null && ship.captain != null) {
                ship.captain.setPersonality(personalityCache)
            }

            personalityCache = null
        }
    }
}
