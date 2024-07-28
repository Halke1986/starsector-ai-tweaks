package com.genir.aitweaks.core.utils.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAssignmentType.*
import com.fs.starfarer.api.combat.CombatFleetManagerAPI
import com.fs.starfarer.api.combat.CombatTaskManagerAPI
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.combat.combatState
import com.genir.aitweaks.core.features.shipai.AI
import com.genir.aitweaks.core.features.shipai.AIPlugin
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

// Returns false for detached modules.
val ShipAPI.isModule: Boolean
    get() = stationSlot != null

val ShipAPI.rootModule: ShipAPI
    get() = if (isModule) parentStation else this

val ShipAPI.isHullDamageable: Boolean
    get() = mutableStats.hullDamageTakenMult.getModifiedValue() > 0f

private val ShipAPI.taskManager: CombatTaskManagerAPI
    get() = Global.getCombatEngine().getFleetManager(owner).getTaskManager(isAlly)

val ShipAPI.assignment: CombatFleetManagerAPI.AssignmentInfo?
    get() = taskManager.getAssignmentFor(this)

val ShipAPI.hasEscortAssignment: Boolean
    get() = assignment?.type.let { it == LIGHT_ESCORT || it == MEDIUM_ESCORT || it == HEAVY_ESCORT }

val ShipAPI.deployedFleetMember: DeployedFleetMemberAPI?
    get() = Global.getCombatEngine().getFleetManager(owner).getDeployedFleetMember(this)

/** Get target which the ship is currently attacking. */
val ShipAPI.attackTarget: ShipAPI?
    get() = when {
        // Modules follow their parent target.
        isModule -> rootModule.attackTarget

        // Custom AI reliably sets shipTarget value.
        customAI != null -> shipTarget

        // For manually controlled ship, return R-selected target.
        this == Global.getCombatEngine().playerShip && Global.getCombatEngine().isUIAutopilotOn -> shipTarget

        // Fall back to using vanilla AI maneuver target.
        else -> combatState().maneuverTargetTracker[this]
    }

val ShipAPI.isAutomated: Boolean
    get() = variant.hasHullMod(HullMods.AUTOMATED)

val ShipAPI.deploymentPoints: Float
    get() = fleetMember?.unmodifiedDeploymentPointsCost ?: 0f

/** Angle between ship facing and direction from ship to point p. */
fun ShipAPI.angleFromFacing(p: Vector2f): Float {
    return MathUtils.getShortestRotation((p - location).getFacing(), facing)
}

fun ShipAPI.hasAIType(c: Class<*>?): Boolean {
    return when {
        c == null -> false
        c.isInstance(ai) -> true
        c.isInstance((ai as? Ship.ShipAIWrapper)?.ai) -> true
        else -> false
    }
}

val ShipAPI.hasVanillaAI: Boolean
    get() = hasAIType(BasicShipAI::class.java)

val ShipAPI.hasCustomAI: Boolean
    get() = hasAIType(AIPlugin::class.java)

val ShipAPI.customAI: AI?
    get() = ((ai as? Ship.ShipAIWrapper)?.ai as? AIPlugin)?.ai

val ShipAPI.fluxLeft: Float
    get() = fluxTracker.maxFlux - fluxTracker.currFlux