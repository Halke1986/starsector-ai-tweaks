package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.CombatAssignmentType.*
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship

/** Return personality preset used by vanilla ship AI. */
val ShipAPI.AIPersonality: String
    get() = (this.ai as? BasicShipAI)?.config?.personalityOverride ?: (this as Ship).personality

val ShipAPI.isModule: Boolean
    get() = this.stationSlot != null

val ShipAPI.rootModule: ShipAPI
    get() = if (this.isModule) this.parentStation else this

val ShipAPI.isHullDamageable: Boolean
    get() = this.mutableStats.hullDamageTakenMult.getModifiedValue() > 0f

val ShipAPI.taskManager: CombatTaskManagerAPI
    get() = Global.getCombatEngine().getFleetManager(this.owner).getTaskManager(this.isAlly)

val ShipAPI.assignment: CombatFleetManagerAPI.AssignmentInfo?
    get() = this.taskManager.getAssignmentFor(this)

val ShipAPI.hasEscortAssignment: Boolean
    get() = this.assignment?.type.let { it == LIGHT_ESCORT || it == MEDIUM_ESCORT || it == HEAVY_ESCORT }

val ShipAPI.deployedFleetMember: DeployedFleetMemberAPI?
    get() = Global.getCombatEngine().getFleetManager(this.owner).getDeployedFleetMember(this)

val ShipAPI.maneuverTarget: ShipAPI?
    get() = this.aiFlags.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI

val ShipAPI.trueShipTarget: ShipAPI?
    get() {
        val root = this.rootModule
        val engine = Global.getCombatEngine()
        val aiControl = root != engine.playerShip || !engine.isUIAutopilotOn
        return if (aiControl) root.maneuverTarget
        else root.shipTarget
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

val ShipAPI.isAutomated: Boolean
    get() = this.variant.hasHullMod(HullMods.AUTOMATED)

inline fun <reified T> ShipAPI.hasAIType(): Boolean = (this as Ship).shipAI?.let { it is T || (it as? Ship.ShipAIWrapper)?.ai is T }
    ?: false

val ShipAPI.deploymentPoints: Float
    get() = this.fleetMember?.unmodifiedDeploymentPointsCost ?: 0f

val ShipAPI.maxFiringRange: Float
    get() = this.allWeapons.maxOfOrNull { it.range } ?: 0f