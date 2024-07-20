package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.combat.ai.BasicShipAI
import com.genir.aitweaks.core.features.shipai.adapters.ShieldAI
import com.genir.aitweaks.core.features.shipai.adapters.SystemAI
import com.genir.aitweaks.core.features.shipai.adapters.ThreatEvalAI
import com.genir.aitweaks.core.features.shipai.adapters.VentModule
import com.genir.aitweaks.core.utils.ShipSystemAiType
import com.genir.aitweaks.core.utils.extensions.AIType
import org.lwjgl.util.vector.Vector2f

/** AI modules carried over from vanilla ship AI. */
class Vanilla(ship: ShipAPI, systemBlacklist: List<ShipSystemAiType> = listOf()) {
    private val vanillaAI: BasicShipAI = Global.getSettings().createDefaultShipAI(ship, ShipAIConfig()) as BasicShipAI

    private val threatEvalAI: ThreatEvalAI = ThreatEvalAI(vanillaAI)
    private val ventModule: VentModule = VentModule(vanillaAI)
    private val shieldAI: ShieldAI? = ShieldAI.getIfExists(vanillaAI)
    private val systemAI: SystemAI? = if (systemBlacklisted(ship, systemBlacklist)) null else SystemAI.getIfExists(vanillaAI)

    fun advance(dt: Float, attackTarget: ShipAPI?) {
        // TODO maybe implement?
        val missileDangerDir: Vector2f? = null
        val collisionDangerDir: Vector2f? = null

        vanillaAI.aiFlags.advance(dt)

        threatEvalAI.advance(dt)
        ventModule.advance(dt, attackTarget)
        shieldAI?.advance(dt, threatEvalAI, missileDangerDir, collisionDangerDir, attackTarget)
        systemAI?.advance(dt, missileDangerDir, collisionDangerDir, attackTarget)
    }

    fun flags(): ShipwideAIFlags = vanillaAI.aiFlags

    private fun systemBlacklisted(ship: ShipAPI, systemBlacklist: List<ShipSystemAiType>): Boolean {
        return systemBlacklist.contains(ship.system?.specAPI?.AIType)
    }
}