package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints.PHASE
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
import com.genir.starfarer.combat.ai.BasicShipAI
import com.genir.starfarer.combat.entities.Ship
import com.genir.starfarer.combat.tasks.CombatTaskManager
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max

/** Returns false for detached modules. Will be false before
 * ship is completely initialized, e.g. in AI picker. */
val ShipAPI.isModule: Boolean
    get() = stationSlot != null && parentStation != null

/** Return ship root module. */
val ShipAPI.root: ShipAPI
    get() = if (isModule) parentStation else this

val ShipAPI.isBig: Boolean
    get() = (root.isDestroyer || root.isCruiser || root.isCapital) && allGroupedWeapons.isNotEmpty()

val ShipAPI.isHullDamageable: Boolean
    get() = mutableStats.hullDamageTakenMult.modifiedValue > 0f

val ShipAPI.taskManager: CombatTaskManagerAPI
    get() = Global.getCombatEngine().getFleetManager(owner).getTaskManager(isAlly)

val ShipAPI.assignment: CombatFleetManagerAPI.AssignmentInfo?
    get() = taskManager.getAssignmentFor(this)

val ShipAPI.deployedFleetMember: DeployedFleetMemberAPI?
    get() = Global.getCombatEngine().getFleetManager(owner).getDeployedFleetMember(this)

val ShipAPI.hasDirectOrders: Boolean
    get() {
        val taskManager = taskManager as? CombatTaskManager
        val fleetMember = deployedFleetMember as? CombatTaskManager.DeployedFleetMember

        return taskManager?.hasDirectOrders(fleetMember) == true
    }

val ShipAPI.canReceiveOrders: Boolean
    get() = when {
        !isAlive -> false
        isExpired -> false
        isStation -> false
        isModule -> false
        isFighter -> false
        isUnderManualControl -> false
        deployedFleetMember == null -> false
        else -> true
    }

/** Get target which the ship is currently attacking. */
val ShipAPI.attackTarget: ShipAPI?
    get() {
        val entity = when {
            // Modules follow their parent target.
            isModule -> root.attackTarget

            // Custom AI reliably sets shipTarget value.
            customShipAI != null -> shipTarget

            // For manually controlled ship, return the R-selected target.
            isUnderManualControl -> shipTarget

            // For vanilla AI, check the maneuver target directly.
            basicShipAI != null -> {
                basicShipAI!!.currentManeuver?.maneuver_getTarget()
            }

            // Fall back to using vanilla maneuver target flag.
            else -> aiFlags?.getCustom(AIFlags.MANEUVER_TARGET)
        }

        if (entity !is ShipAPI || !entity.isValidTarget) {
            return null
        }

        return entity
    }

val ShipAPI.isAutomated: Boolean
    get() = variant.hasHullMod(HullMods.AUTOMATED)

val ShipAPI.isPhase: Boolean
    get() = hullSpec.isPhase || hullSpec.hints.contains(PHASE) || shield?.type == ShieldAPI.ShieldType.PHASE

val ShipAPI.deploymentPoints: Float
    get() = max(0f, fleetMember?.unmodifiedDeploymentPointsCost ?: 0f)

/** Calculates the effective ship velocity in the global frame
 * of reference, taking into account the ship's time flow. */
val ShipAPI.timeAdjustedVelocity: Vector2f
    get() = velocity * timeMult

val ShipAPI.timeMult: Float
    get() = mutableStats.timeMult.modifiedValue

val ShipAPI.fluxLeft: Float
    get() = fluxTracker.maxFlux - fluxTracker.currFlux

val ShipAPI.customShipAI: CustomShipAI?
    get() = ai as? CustomShipAI

val ShipAPI.basicShipAI: BasicShipAI?
    get() = ai as? BasicShipAI

val ShipAPI.isUnderManualControl: Boolean
    get() = this == Global.getCombatEngine().playerShip && Global.getCombatEngine().isUIAutopilotOn

val ShipAPI.allGroupedWeapons: List<WeaponHandle>
    get() = weaponGroupsCopy.flatMap { it.weaponsCopy.map { weaponAPI -> weaponAPI.handle } }

fun ShipAPI.shortestRotationToTarget(target: Vector2f, weaponGroupFacing: Direction): Direction {
    val targetFacing = (target - location).facing
    val weaponFacing = facing.direction + weaponGroupFacing
    return targetFacing - weaponFacing
}

/** Collision radius encompassing an entire modular ship, including drones. */
val ShipAPI.totalCollisionRadius: Float
    get() {
        val modules = childModulesCopy.filter { it.isModule } // Make sure the module is still attached.
        val drones = deployedDrones?.filter { it.collisionClass == CollisionClass.SHIP }

        val withModules = modules.maxOfOrNull { (location - it.location).length + it.collisionRadius } ?: 0f
        val withDrones = drones?.maxOfOrNull { (location - it.location).length + it.collisionRadius } ?: 0f

        return max(collisionRadius, max(withDrones, withModules))
    }

fun ShipAPI.command(cmd: ShipCommand) = this.giveCommand(cmd, null, 0)

// TODO refine the speed threshold, maybe add maneuverability threshold.
val ShipAPI.isFast: Boolean
    get() = isAlive && (root.isFrigate || root.isDestroyer || baseMaxSpeed * timeMult >= 150f)

/** Ship max speed not modified by zero flux boost or an active system. */
val ShipAPI.baseMaxSpeed: Float
    get() = statWithoutMobilityBonuses(mutableStats.maxSpeed).modifiedValue

val ShipAPI.baseTurnRate: Float
    get() = statWithoutMobilityBonuses(mutableStats.maxTurnRate).modifiedValue

private fun ShipAPI.statWithoutMobilityBonuses(modifiedStat: MutableStat): MutableStat {
    val stat = modifiedStat.createCopy()

    if (system != null) {
        stat.unmodify(system.id + " effect")
    }

    // Remove zero flux boost for ships with no Safety Overrides.
    if (mutableStats.zeroFluxMinimumFluxLevel.modifiedValue < 1f) {
        stat.unmodify("zero_flux_boost")
    }

    return stat
}

val ShipAPI.maxRange: Float
    get() = allGroupedWeapons.maxOfOrNull { it.slot.rangeFromShipCenter(0f.direction, it.totalRange) } ?: 0f

val ShipAPI.AIPersonality: String
    get() = (ai as? BasicShipAI)?.config?.personalityOverride ?: (this as Ship).personality

val ShipAPI.Id: String
    get() = hullSpec?.hullId ?: "-"

val ShipAPI.isFlamedOut: Boolean
    get() = engineController.isFlamedOut

val ShipAPI.isSkirmisher: Boolean
    get() = root.isFrigate || variant.hasHullMod("aitweaks_skirmisher")
