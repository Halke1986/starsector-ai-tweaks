package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.CombatAssignmentType.*
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.utils.aitStash
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

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

val ShipAPI.trueShipTarget: ShipAPI?
    get() {
        val root = rootModule
        val engine = Global.getCombatEngine()
        val aiControl = root != engine.playerShip || !engine.isUIAutopilotOn
        return if (aiControl) aitStash.attackTarget ?: root.maneuverTarget
        else root.shipTarget
    }

val ShipAPI.isAutomated: Boolean
    get() = variant.hasHullMod(HullMods.AUTOMATED)

val ShipAPI.deploymentPoints: Float
    get() = fleetMember?.unmodifiedDeploymentPointsCost ?: 0f

val ShipAPI.maxFiringRange: Float
    get() = allWeapons.maxOfOrNull { it.range } ?: 0f

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