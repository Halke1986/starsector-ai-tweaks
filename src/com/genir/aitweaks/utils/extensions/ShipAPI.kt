package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAssignmentType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship

/** Return personality preset used by vanilla ship AI. */
val ShipAPI.AIPersonality: String
    get() = (this.ai as? BasicShipAI)?.config?.personalityOverride ?: (this as Ship).personality

val ShipAPI.rootModule: ShipAPI
    get() = this.parentStation ?: this

val ShipAPI.isVastBulk: Boolean
    get() = this.hullSpec.isBuiltInMod("vastbulk")

val ShipAPI.hasEscortAssignment: Boolean
    get() = Global.getCombatEngine().getFleetManager(this.owner).getTaskManager(this.isAlly).getAssignmentFor(this)?.type.let { it == CombatAssignmentType.LIGHT_ESCORT || it == CombatAssignmentType.MEDIUM_ESCORT || it == CombatAssignmentType.HEAVY_ESCORT }

val ShipAPI.trueShipTarget: ShipAPI?
    get() {
        val ship = this.rootModule
        val engine = Global.getCombatEngine()
        val aiControl = ship != engine.playerShip || !engine.isUIAutopilotOn
        return if (aiControl) ship.aiFlags.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI
        else ship.shipTarget
    }

val ShipAPI.strafeAcceleration: Float
    get() = this.acceleration * when (this.hullSize) {
        ShipAPI.HullSize.FIGHTER -> 0.75f
        ShipAPI.HullSize.FRIGATE -> 1.0f
        ShipAPI.HullSize.DESTROYER -> 0.75f
        ShipAPI.HullSize.CRUISER -> 0.5f
        ShipAPI.HullSize.CAPITAL_SHIP -> 0.25f
        else -> 1.0f
    }