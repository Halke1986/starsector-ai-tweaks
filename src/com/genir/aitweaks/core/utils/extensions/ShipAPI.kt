package com.genir.aitweaks.core.utils.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.CombatAssignmentType.*
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.features.shipai.AIPlugin
import com.genir.aitweaks.core.features.shipai.customAI
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

val ShipAPI.maneuverTarget: ShipAPI?
    get() = aiFlags.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI

val ShipAPI.attackTarget: ShipAPI?
    get() {
        if (isModule) return rootModule.attackTarget

        val engine = Global.getCombatEngine()
        val manualControl = this == engine.playerShip && engine.isUIAutopilotOn
        if (manualControl) return shipTarget

        customAI?.attackTarget?.let { return it }

        return maneuverTarget
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