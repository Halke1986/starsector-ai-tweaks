package com.genir.aitweaks.core.handles

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.CombatListenerManagerAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.loading.WeaponSlotAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.state.Config
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection
import com.genir.starfarer.combat.ai.BasicShipAI
import com.genir.starfarer.combat.entities.Ship
import com.genir.starfarer.combat.tasks.CombatTaskManager
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import kotlin.math.max

@JvmInline
value class ShipHandle(val shipAPI: ShipAPI) {
    companion object {
        val ShipAPI.handle: ShipHandle
            get() = ShipHandle(this)
    }

    /** Returns false for detached modules. Will be false before
     * ship is completely initialized, e.g. in AI picker. */
    val isModule: Boolean
        get() = stationSlot != null && parentStation != null

    /** Return ship root module. */
    val root: ShipHandle
        get() = if (isModule) parentStation ?: this else this

    val isBig: Boolean
        get() = (root.isDestroyer || root.isCruiser || root.isCapital) && allGroupedWeapons.isNotEmpty()

    val isHullDamageable: Boolean
        get() = mutableStats.hullDamageTakenMult.modifiedValue > 0f

    val taskManager: CombatTaskManagerAPI
        get() = Global.getCombatEngine().getFleetManager(owner).getTaskManager(isAlly)

    val assignment: CombatFleetManagerAPI.AssignmentInfo?
        get() = taskManager.getAssignmentFor(this.shipAPI)

    val deployedFleetMember: DeployedFleetMemberAPI?
        get() = Global.getCombatEngine().getFleetManager(owner).getDeployedFleetMember(this.shipAPI)

    val hasDirectOrders: Boolean
        get() {
            val taskManager = taskManager as? CombatTaskManager
            val fleetMember = deployedFleetMember as? CombatTaskManager.DeployedFleetMember

            return taskManager?.hasDirectOrders(fleetMember) == true
        }

    val canReceiveOrders: Boolean
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
    val attackTarget: ShipHandle?
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

    val isAutomated: Boolean
        get() = variant.hasHullMod(HullMods.AUTOMATED)

    val isPhase: Boolean
        get() = hullSpec.isPhase || hullSpec.hints.contains(ShipHullSpecAPI.ShipTypeHints.PHASE) || shield?.type == ShieldAPI.ShieldType.PHASE

    val deploymentPoints: Float
        get() = max(0f, fleetMember?.unmodifiedDeploymentPointsCost ?: 0f)

    val timeMult: Float
        get() = mutableStats.timeMult.modifiedValue

    val fluxLeft: Float
        get() = fluxTracker.maxFlux - fluxTracker.currFlux

    val customShipAI: CustomShipAI?
        get() = ai as? CustomShipAI

    val basicShipAI: BasicShipAI?
        get() = ai as? BasicShipAI

    val isUnderManualControl: Boolean
        get() = this == Global.getCombatEngine().playerShip && Global.getCombatEngine().isUIAutopilotOn

    val allGroupedWeapons: List<WeaponHandle>
        get() = weaponGroupsCopy.flatMap { it.weaponsCopy.map { weaponAPI -> weaponAPI.handle } }

    fun shortestRotationToTarget(target: Vector2f, weaponGroupFacing: Direction): Direction {
        val targetFacing = (target - location).facing
        val weaponFacing = facing.toDirection + weaponGroupFacing
        return targetFacing - weaponFacing
    }

    /** Collision radius encompassing an entire modular ship, including drones. */
    val totalCollisionRadius: Float
        get() {
            val modules = childModulesCopy.filter { it.isModule } // Make sure the module is still attached.
            val drones = deployedDrones?.filter { it.collisionClass == CollisionClass.SHIP && it.isValidTarget }

            val withModules = modules.maxOfOrNull { (location - it.location).length + it.collisionRadius } ?: 0f
            val withDrones = drones?.maxOfOrNull { (location - it.location).length + it.collisionRadius } ?: 0f

            return max(collisionRadius, max(withDrones, withModules))
        }

    fun command(cmd: ShipCommand) = this.giveCommand(cmd, null, 0)

    // TODO refine the speed threshold, maybe add maneuverability threshold.
    val isFast: Boolean
        get() = isAlive && (root.isFrigate || root.isDestroyer || baseMaxSpeed * timeMult >= 150f)

    /** Ship max speed not modified by zero flux boost or an active system. */
    val baseMaxSpeed: Float
        get() = statWithoutMobilityBonuses(mutableStats.maxSpeed).modifiedValue

    val baseTurnRate: Float
        get() = statWithoutMobilityBonuses(mutableStats.maxTurnRate).modifiedValue

    private fun statWithoutMobilityBonuses(modifiedStat: MutableStat): MutableStat {
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

    val maxRange: Float
        get() = allGroupedWeapons.maxOfOrNull { it.slot.rangeFromShipCenter(0f.toDirection, it.engagementRange) } ?: 0f

    val AIPersonality: String
        get() = (ai as? BasicShipAI)?.config?.personalityOverride ?: (this as Ship).personality

    val Id: String
        get() = hullSpec?.hullId ?: "-"

    val isFlamedOut: Boolean
        get() = engineController.isFlamedOut

    val isSkirmisher: Boolean
        get() = root.isFrigate || variant.hasHullMod("aitweaks_skirmisher")

    val offlineTimeRemaining: Float
        get() = when {
            fluxTracker.isOverloaded -> {
                fluxTracker.overloadTimeRemaining
            }

            fluxTracker.isVenting -> {
                fluxTracker.timeToVent
            }

            else -> {
                0f
            }
        }

    /** Some ships, e.g. ones with Safety Overrides can not vent. */
    val canVentFlux: Boolean
        get() = mutableStats.ventRateMult.getModifiedValue() > 0f

    val isAlwaysSearchDestroy: Boolean
        get() = Config.config.fleetwideSearchAndDestroy || variant.hasHullMod("aitweaks_search_and_destroy")

    /** Vanilla ShipAPI.fluxLevel May return NaN for ships
     * without flux bar, like armor modules. */
    val FluxLevel: Float
        get() {
            val level = shipAPI.fluxLevel
            return if (level.isNaN()) 0f else level
        }

// ****************************************************************************
// ShipAPI Implementation

    val fleetMemberId: String
        get() = shipAPI.fleetMemberId

    val mouseTarget: Vector2f
        get() = shipAPI.mouseTarget

    val isShuttlePod: Boolean
        get() = shipAPI.isShuttlePod

    var isDrone: Boolean
        get() = shipAPI.isDrone
        set(p0) {
            shipAPI.isDrone = p0
        }

    val isFighter: Boolean
        get() = shipAPI.isFighter

    val isFrigate: Boolean
        get() = shipAPI.isFrigate

    val isDestroyer: Boolean
        get() = shipAPI.isDestroyer

    val isCruiser: Boolean
        get() = shipAPI.isCruiser

    val isCapital: Boolean
        get() = shipAPI.isCapital

    var hullSize: ShipAPI.HullSize?
        get() = shipAPI.hullSize
        set(p0) {
            shipAPI.hullSize = p0
        }

    var shipTarget: ShipAPI?
        get() = shipAPI.shipTarget
        set(p0) {
            shipAPI.shipTarget = p0
        }

    var originalOwner: Int
        get() = shipAPI.originalOwner
        set(p0) {
            shipAPI.originalOwner = p0
        }

    fun resetOriginalOwner() {
        shipAPI.resetOriginalOwner()
    }

    val mutableStats: MutableShipStatsAPI
        get() = shipAPI.mutableStats

    var isHulk: Boolean
        get() = shipAPI.isHulk
        set(p0) {
            shipAPI.isHulk = p0
        }

    val allWeapons: List<WeaponAPI>
        get() = shipAPI.allWeapons

    val phaseCloak: ShipSystemAPI
        get() = shipAPI.phaseCloak as ShipSystemAPI

    val system: ShipSystemAPI?
        get() = shipAPI.system

    val travelDrive: ShipSystemAPI
        get() = shipAPI.travelDrive as ShipSystemAPI

    fun toggleTravelDrive() {
        shipAPI.toggleTravelDrive()
    }

    fun setShield(p0: ShieldAPI.ShieldType?, p1: Float, p2: Float, p3: Float) {
        shipAPI.setShield(p0, p1, p2, p3)
    }

    val hullSpec: ShipHullSpecAPI
        get() = shipAPI.hullSpec as ShipHullSpecAPI

    val variant: ShipVariantAPI
        get() = shipAPI.variant as ShipVariantAPI

    fun useSystem() {
        shipAPI.useSystem()
    }

    val fluxTracker: FluxTrackerAPI
        get() = shipAPI.fluxTracker as FluxTrackerAPI

    val wingMembers: List<ShipAPI>
        get() = shipAPI.wingMembers

    val wingLeader: ShipAPI
        get() = shipAPI.wingLeader

    fun isWingLeader(): Boolean {
        return shipAPI.isWingLeader
    }

    var wing: FighterWingAPI?
        get() = shipAPI.wing as FighterWingAPI
        set(p0) {
            shipAPI.wing = p0
        }

    val deployedDrones: List<ShipAPI>
        get() = shipAPI.deployedDrones

    val droneSource: ShipAPI
        get() = shipAPI.droneSource

    val wingToken: Any
        get() = shipAPI.wingToken

    val armorGrid: ArmorGridAPI
        get() = shipAPI.armorGrid as ArmorGridAPI

    fun setRenderBounds(p0: Boolean) {
        shipAPI.setRenderBounds(p0)
    }

    var cRAtDeployment: Float
        get() = shipAPI.crAtDeployment
        set(p0) {
            shipAPI.crAtDeployment = p0
        }

    var currentCR: Float
        get() = shipAPI.currentCR
        set(p0) {
            shipAPI.currentCR = p0
        }

    val wingCRAtDeployment: Float
        get() = shipAPI.wingCRAtDeployment

    val timeDeployedForCRReduction: Float
        get() = shipAPI.timeDeployedForCRReduction

    val fullTimeDeployed: Float
        get() = shipAPI.fullTimeDeployed

    fun losesCRDuringCombat(): Boolean {
        return shipAPI.losesCRDuringCombat()
    }

    fun controlsLocked(): Boolean {
        return shipAPI.controlsLocked()
    }

    fun setControlsLocked(p0: Boolean) {
        shipAPI.setControlsLocked(p0)
    }

    val disabledWeapons: Set<WeaponAPI>
        get() = shipAPI.disabledWeapons

    val numFlameouts: Int
        get() = shipAPI.numFlameouts

    val hullLevelAtDeployment: Float
        get() = shipAPI.hullLevelAtDeployment

    fun setSprite(p0: String?, p1: String?) {
        shipAPI.setSprite(p0, p1)
    }

    fun setSprite(p0: SpriteAPI?) {
        shipAPI.setSprite(p0)
    }

    val spriteAPI: SpriteAPI
        get() = shipAPI.spriteAPI

    val engineController: ShipEngineControllerAPI
        get() = shipAPI.engineController as ShipEngineControllerAPI

    fun giveCommand(p0: ShipCommand?, p1: Any?, p2: Int) {
        shipAPI.giveCommand(p0, p1, p2)
    }

    var shipAI: ShipAIPlugin?
        get() = shipAPI.shipAI
        set(p0) {
            shipAPI.shipAI = p0
        }

    fun resetDefaultAI() {
        shipAPI.resetDefaultAI()
    }

    fun turnOnTravelDrive() {
        shipAPI.turnOnTravelDrive()
    }

    fun turnOnTravelDrive(p0: Float) {
        shipAPI.turnOnTravelDrive(p0)
    }

    fun turnOffTravelDrive() {
        shipAPI.turnOffTravelDrive()
    }

    val isRetreating: Boolean
        get() = shipAPI.isRetreating

    fun abortLanding() {
        shipAPI.abortLanding()
    }

    fun beginLandingAnimation(p0: ShipAPI?) {
        shipAPI.beginLandingAnimation(p0)
    }

    val isLanding: Boolean
        get() = shipAPI.isLanding

    val isFinishedLanding: Boolean
        get() = shipAPI.isFinishedLanding

    val isAlive: Boolean
        get() = shipAPI.isAlive

    var isInsideNebula: Boolean
        get() = shipAPI.isInsideNebula
        set(p0) {
            shipAPI.isInsideNebula = p0
        }

    var isAffectedByNebula: Boolean
        get() = shipAPI.isAffectedByNebula
        set(p0) {
            shipAPI.isAffectedByNebula = p0
        }

    val deployCost: Float
        get() = shipAPI.deployCost

    fun removeWeaponFromGroups(p0: WeaponAPI?) {
        shipAPI.removeWeaponFromGroups(p0)
    }

    fun applyCriticalMalfunction(p0: Any?) {
        shipAPI.applyCriticalMalfunction(p0)
    }

    fun applyCriticalMalfunction(p0: Any?, p1: Boolean) {
        shipAPI.applyCriticalMalfunction(p0, p1)
    }

    val baseCriticalMalfunctionDamage: Float
        get() = shipAPI.baseCriticalMalfunctionDamage

    val engineFractionPermanentlyDisabled: Float
        get() = shipAPI.engineFractionPermanentlyDisabled

    val combinedAlphaMult: Float
        get() = shipAPI.combinedAlphaMult

    var lowestHullLevelReached: Float
        get() = shipAPI.lowestHullLevelReached
        set(p0) {
            shipAPI.lowestHullLevelReached = p0
        }

    val aIFlags: ShipwideAIFlags
        get() = shipAPI.aiFlags

    val weaponGroupsCopy: List<WeaponGroupAPI>
        get() = shipAPI.weaponGroupsCopy

    var isHoldFire: Boolean
        get() = shipAPI.isHoldFire
        set(p0) {
            shipAPI.isHoldFire = p0
        }

    var isHoldFireOneFrame: Boolean
        get() = shipAPI.isHoldFireOneFrame
        set(p0) {
            shipAPI.isHoldFireOneFrame = p0
        }

    var isPhased: Boolean
        get() = shipAPI.isPhased
        set(p0) {
            shipAPI.isPhased = p0
        }

    var isAlly: Boolean
        get() = shipAPI.isAlly
        set(p0) {
            shipAPI.isAlly = p0
        }

    fun setWeaponGlow(p0: Float, p1: Color?, p2: EnumSet<WeaponAPI.WeaponType>?) {
        shipAPI.setWeaponGlow(p0, p1, p2)
    }

    var ventCoreColor: Color?
        get() = shipAPI.ventCoreColor
        set(p0) {
            shipAPI.ventCoreColor = p0
        }

    var ventFringeColor: Color?
        get() = shipAPI.ventFringeColor
        set(p0) {
            shipAPI.ventFringeColor = p0
        }

    val hullStyleId: String
        get() = shipAPI.hullStyleId

    fun getWeaponGroupFor(p0: WeaponAPI?): WeaponGroupAPI {
        return shipAPI.getWeaponGroupFor(p0)
    }

    fun setCopyLocation(p0: Vector2f?, p1: Float, p2: Float) {
        shipAPI.setCopyLocation(p0, p1, p2)
    }

    val copyLocation: Vector2f
        get() = shipAPI.copyLocation

    val id: String
        get() = shipAPI.id

    var name: String?
        get() = shipAPI.name
        set(p0) {
            shipAPI.name = p0
        }

    fun setJitter(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float) {
        shipAPI.setJitter(p0, p1, p2, p3, p4)
    }

    fun setJitter(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float, p5: Float) {
        shipAPI.setJitter(p0, p1, p2, p3, p4, p5)
    }

    fun setJitterUnder(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float) {
        shipAPI.setJitterUnder(p0, p1, p2, p3, p4)
    }

    fun setJitterUnder(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float, p5: Float) {
        shipAPI.setJitterUnder(p0, p1, p2, p3, p4, p5)
    }

    val timeDeployedUnderPlayerControl: Float
        get() = shipAPI.timeDeployedUnderPlayerControl

    val smallTurretCover: SpriteAPI
        get() = shipAPI.smallTurretCover

    val smallHardpointCover: SpriteAPI
        get() = shipAPI.smallHardpointCover

    val mediumTurretCover: SpriteAPI
        get() = shipAPI.mediumTurretCover

    val mediumHardpointCover: SpriteAPI
        get() = shipAPI.mediumHardpointCover

    val largeTurretCover: SpriteAPI
        get() = shipAPI.largeTurretCover

    val largeHardpointCover: SpriteAPI
        get() = shipAPI.largeHardpointCover

    var isDefenseDisabled: Boolean
        get() = shipAPI.isDefenseDisabled
        set(p0) {
            shipAPI.isDefenseDisabled = p0
        }

    fun setApplyExtraAlphaToEngines(p0: Boolean) {
        shipAPI.setApplyExtraAlphaToEngines(p0)
    }

    fun resetOverloadColor() {
        shipAPI.resetOverloadColor()
    }

    var overloadColor: Color?
        get() = shipAPI.overloadColor
        set(p0) {
            shipAPI.overloadColor = p0
        }

    val isRecentlyShotByPlayer: Boolean
        get() = shipAPI.isRecentlyShotByPlayer

    val maxSpeedWithoutBoost: Float
        get() = shipAPI.maxSpeedWithoutBoost

    val hardFluxLevel: Float
        get() = shipAPI.hardFluxLevel

    fun fadeToColor(p0: Any?, p1: Color?, p2: Float, p3: Float, p4: Float) {
        shipAPI.fadeToColor(p0, p1, p2, p3, p4)
    }

    var isShowModuleJitterUnder: Boolean
        get() = shipAPI.isShowModuleJitterUnder
        set(p0) {
            shipAPI.isShowModuleJitterUnder = p0
        }

    fun addAfterimage(p0: Color?, p1: Float, p2: Float, p3: Float, p4: Float, p5: Float, p6: Float, p7: Float, p8: Float, p9: Boolean, p10: Boolean, p11: Boolean) {
        shipAPI.addAfterimage(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11)
    }

    var captain: PersonAPI?
        get() = shipAPI.captain as PersonAPI
        set(p0) {
            shipAPI.captain = p0
        }

    var stationSlot: WeaponSlotAPI?
        get() = shipAPI.stationSlot as WeaponSlotAPI
        set(p0) {
            shipAPI.stationSlot = p0
        }

    var parentStation: ShipHandle?
        get() = shipAPI.parentStation?.handle
        set(p0) {
            shipAPI.parentStation = p0?.shipAPI
        }

    var fixedLocation: Vector2f?
        get() = shipAPI.fixedLocation
        set(p0) {
            shipAPI.fixedLocation = p0
        }

    fun hasRadarRibbonIcon(): Boolean {
        return shipAPI.hasRadarRibbonIcon()
    }

    val isTargetable: Boolean
        get() = shipAPI.isTargetable

    val isSelectableInWarroom: Boolean
        get() = shipAPI.isSelectableInWarroom

    var isShipWithModules: Boolean
        get() = shipAPI.isShipWithModules
        set(p0) {
            shipAPI.isShipWithModules = p0
        }

    val childModulesCopy: List<ShipAPI>
        get() = shipAPI.childModulesCopy

    val isPiece: Boolean
        get() = shipAPI.isPiece

    val visualBounds: BoundsAPI
        get() = shipAPI.visualBounds as BoundsAPI

    val renderOffset: Vector2f
        get() = shipAPI.renderOffset

    fun splitShip(): ShipAPI {
        return shipAPI.splitShip()
    }

    val numFighterBays: Int
        get() = shipAPI.numFighterBays

    var isPullBackFighters: Boolean
        get() = shipAPI.isPullBackFighters
        set(p0) {
            shipAPI.isPullBackFighters = p0
        }

    fun hasLaunchBays(): Boolean {
        return shipAPI.hasLaunchBays()
    }

    val launchBaysCopy: List<FighterLaunchBayAPI>
        get() = shipAPI.launchBaysCopy

    var fighterTimeBeforeRefit: Float
        get() = shipAPI.fighterTimeBeforeRefit
        set(p0) {
            shipAPI.fighterTimeBeforeRefit = p0
        }

    val allWings: List<FighterWingAPI>
        get() = shipAPI.allWings

    val sharedFighterReplacementRate: Float
        get() = shipAPI.sharedFighterReplacementRate

    fun areSignificantEnemiesInRange(): Boolean {
        return shipAPI.areSignificantEnemiesInRange()
    }

    val usableWeapons: List<WeaponAPI>
        get() = shipAPI.usableWeapons

    val moduleOffset: Vector2f
        get() = shipAPI.moduleOffset

    val massWithModules: Float
        get() = shipAPI.massWithModules

    val originalCaptain: PersonAPI
        get() = shipAPI.originalCaptain as PersonAPI

    var isRenderEngines: Boolean
        get() = shipAPI.isRenderEngines
        set(p0) {
            shipAPI.isRenderEngines = p0
        }

    val selectedGroupAPI: WeaponGroupAPI
        get() = shipAPI.selectedGroupAPI

    fun ensureClonedStationSlotSpec() {
        shipAPI.ensureClonedStationSlotSpec()
    }

    fun setDHullOverlay(p0: String?) {
        shipAPI.setDHullOverlay(p0)
    }

    var isStation: Boolean
        get() = shipAPI.isStation
        set(p0) {
            shipAPI.isStation = p0
        }

    val isStationModule: Boolean
        get() = shipAPI.isStationModule

    fun areAnyEnemiesInRange(): Boolean {
        return shipAPI.areAnyEnemiesInRange()
    }

    fun blockCommandForOneFrame(p0: ShipCommand?) {
        shipAPI.blockCommandForOneFrame(p0)
    }

    val maxTurnRate: Float
        get() = shipAPI.maxTurnRate

    val turnAcceleration: Float
        get() = shipAPI.turnAcceleration

    val turnDeceleration: Float
        get() = shipAPI.turnDeceleration

    val deceleration: Float
        get() = shipAPI.deceleration

    val acceleration: Float
        get() = shipAPI.acceleration

    val maxSpeed: Float
        get() = shipAPI.maxSpeed

//    OVERRIDEN
//    val fluxLevel: Float
//        get() = shipAPI.fluxLevel

    val currFlux: Float
        get() = shipAPI.currFlux

    val maxFlux: Float
        get() = shipAPI.maxFlux

    val minFluxLevel: Float
        get() = shipAPI.minFluxLevel

    val minFlux: Float
        get() = shipAPI.minFlux

    fun setLightDHullOverlay() {
        shipAPI.setLightDHullOverlay()
    }

    fun setMediumDHullOverlay() {
        shipAPI.setMediumDHullOverlay()
    }

    fun setHeavyDHullOverlay() {
        shipAPI.setHeavyDHullOverlay()
    }

    var isJitterShields: Boolean
        get() = shipAPI.isJitterShields
        set(p0) {
            shipAPI.isJitterShields = p0
        }

    var isInvalidTransferCommandTarget: Boolean
        get() = shipAPI.isInvalidTransferCommandTarget
        set(p0) {
            shipAPI.isInvalidTransferCommandTarget = p0
        }

    fun clearDamageDecals() {
        shipAPI.clearDamageDecals()
    }

    fun syncWithArmorGridState() {
        shipAPI.syncWithArmorGridState()
    }

    fun syncWeaponDecalsWithArmorDamage() {
        shipAPI.syncWeaponDecalsWithArmorDamage()
    }

    val isDirectRetreat: Boolean
        get() = shipAPI.isDirectRetreat

    fun setRetreating(p0: Boolean, p1: Boolean) {
        shipAPI.setRetreating(p0, p1)
    }

    val isLiftingOff: Boolean
        get() = shipAPI.isLiftingOff

    fun setVariantForHullmodCheckOnly(p0: ShipVariantAPI?) {
        shipAPI.setVariantForHullmodCheckOnly(p0)
    }

    val shieldCenterEvenIfNoShield: Vector2f
        get() = shipAPI.shieldCenterEvenIfNoShield

    val shieldRadiusEvenIfNoShield: Float
        get() = shipAPI.shieldRadiusEvenIfNoShield

    var fleetMember: FleetMemberAPI?
        get() = shipAPI.fleetMember
        set(p0) {
            shipAPI.fleetMember = p0
        }

    val shieldTarget: Vector2f
        get() = shipAPI.shieldTarget

    fun setShieldTargetOverride(p0: Float, p1: Float) {
        shipAPI.setShieldTargetOverride(p0, p1)
    }

    val listenerManager: CombatListenerManagerAPI
        get() = shipAPI.listenerManager

    fun addListener(p0: Any?) {
        shipAPI.addListener(p0)
    }

    fun removeListener(p0: Any?) {
        shipAPI.removeListener(p0)
    }

    fun removeListenerOfClass(p0: Class<*>?) {
        shipAPI.removeListenerOfClass(p0)
    }

    fun hasListener(p0: Any?): Boolean {
        return shipAPI.hasListener(p0)
    }

    fun hasListenerOfClass(p0: Class<*>?): Boolean {
        return shipAPI.hasListenerOfClass(p0)
    }

    var paramAboutToApplyDamage: Any?
        get() = shipAPI.paramAboutToApplyDamage
        set(p0) {
            shipAPI.paramAboutToApplyDamage = p0
        }

    val fluxBasedEnergyWeaponDamageMultiplier: Float
        get() = shipAPI.fluxBasedEnergyWeaponDamageMultiplier

    val shipExplosionRadius: Float
        get() = shipAPI.shipExplosionRadius

    fun setCircularJitter(p0: Boolean) {
        shipAPI.setCircularJitter(p0)
    }

    var extraAlphaMult: Float
        get() = shipAPI.extraAlphaMult
        set(p0) {
            shipAPI.extraAlphaMult = p0
        }

    var alphaMult: Float
        get() = shipAPI.alphaMult
        set(p0) {
            shipAPI.alphaMult = p0
        }

    fun setAnimatedLaunch() {
        shipAPI.setAnimatedLaunch()
    }

    fun setLaunchingShip(p0: ShipAPI?) {
        shipAPI.setLaunchingShip(p0)
    }

    fun isNonCombat(p0: Boolean): Boolean {
        return shipAPI.isNonCombat(p0)
    }

    fun findBestArmorInArc(p0: Float, p1: Float): Float {
        return shipAPI.findBestArmorInArc(p0, p1)
    }

    fun getAverageArmorInSlice(p0: Float, p1: Float): Float {
        return shipAPI.getAverageArmorInSlice(p0, p1)
    }

    fun cloneVariant() {
        shipAPI.cloneVariant()
    }

    fun setTimeDeployed(p0: Float) {
        shipAPI.setTimeDeployed(p0)
    }

    var fluxVentTextureSheet: String?
        get() = shipAPI.fluxVentTextureSheet
        set(p0) {
            shipAPI.fluxVentTextureSheet = p0
        }

    val aimAccuracy: Float
        get() = shipAPI.aimAccuracy

    var forceCarrierTargetTime: Float
        get() = shipAPI.forceCarrierTargetTime
        set(p0) {
            shipAPI.forceCarrierTargetTime = p0
        }

    var forceCarrierPullBackTime: Float
        get() = shipAPI.forceCarrierPullBackTime
        set(p0) {
            shipAPI.forceCarrierPullBackTime = p0
        }

    var forceCarrierTarget: ShipAPI?
        get() = shipAPI.forceCarrierTarget
        set(p0) {
            shipAPI.forceCarrierTarget = p0
        }

    var explosionScale: Float
        get() = shipAPI.explosionScale
        set(p0) {
            shipAPI.explosionScale = p0
        }

    var explosionFlashColorOverride: Color?
        get() = shipAPI.explosionFlashColorOverride
        set(p0) {
            shipAPI.explosionFlashColorOverride = p0
        }

    var explosionVelocityOverride: Vector2f?
        get() = shipAPI.explosionVelocityOverride
        set(p0) {
            shipAPI.explosionVelocityOverride = p0
        }

    fun setNextHitHullDamageThresholdMult(p0: Float, p1: Float) {
        shipAPI.setNextHitHullDamageThresholdMult(p0, p1)
    }

    val isEngineBoostActive: Boolean
        get() = shipAPI.isEngineBoostActive

    fun makeLookDisabled() {
        shipAPI.makeLookDisabled()
    }

    var extraAlphaMult2: Float
        get() = shipAPI.extraAlphaMult2
        set(p0) {
            shipAPI.extraAlphaMult2 = p0
        }

    var layer: CombatEngineLayers?
        get() = shipAPI.layer
        set(p0) {
            shipAPI.layer = p0
        }

    var isForceHideFFOverlay: Boolean
        get() = shipAPI.isForceHideFFOverlay
        set(p0) {
            shipAPI.isForceHideFFOverlay = p0
        }

    val tags: Set<String>
        get() = shipAPI.tags

    fun addTag(p0: String?) {
        shipAPI.addTag(p0)
    }

    fun hasTag(p0: String?): Boolean {
        return shipAPI.hasTag(p0)
    }

    val peakTimeRemaining: Float
        get() = shipAPI.peakTimeRemaining

    val activeLayers: EnumSet<CombatEngineLayers>
        get() = shipAPI.activeLayers

    var isShipSystemDisabled: Boolean
        get() = shipAPI.isShipSystemDisabled
        set(p0) {
            shipAPI.isShipSystemDisabled = p0
        }

    var isDoNotFlareEnginesWhenStrafingOrDecelerating: Boolean
        get() = shipAPI.isDoNotFlareEnginesWhenStrafingOrDecelerating
        set(p0) {
            shipAPI.isDoNotFlareEnginesWhenStrafingOrDecelerating = p0
        }

    val fleetCommander: PersonAPI
        get() = shipAPI.fleetCommander as PersonAPI

    var isDoNotRender: Boolean
        get() = shipAPI.isDoNotRender
        set(p0) {
            shipAPI.isDoNotRender = p0
        }

    var hulkChanceOverride: Float
        get() = shipAPI.hulkChanceOverride
        set(p0) {
            shipAPI.hulkChanceOverride = p0
        }

    var impactVolumeMult: Float
        get() = shipAPI.impactVolumeMult
        set(p0) {
            shipAPI.impactVolumeMult = p0
        }

    fun checkCollisionVsRay(p0: Vector2f?, p1: Vector2f?): Vector2f {
        return shipAPI.checkCollisionVsRay(p0, p1)
    }

    fun isPointInBounds(p0: Vector2f?): Boolean {
        return shipAPI.isPointInBounds(p0)
    }

    var isSpawnDebris: Boolean
        get() = shipAPI.isSpawnDebris
        set(p0) {
            shipAPI.isSpawnDebris = p0
        }

    var dHullOverlayAngleOffset: Float
        get() = shipAPI.dHullOverlayAngleOffset
        set(p0) {
            shipAPI.dHullOverlayAngleOffset = p0
        }

    var extraOverlayAngleOffset: Float
        get() = shipAPI.extraOverlayAngleOffset
        set(p0) {
            shipAPI.extraOverlayAngleOffset = p0
        }

    fun setExtraOverlay(p0: String?) {
        shipAPI.setExtraOverlay(p0)
    }

    var extraOverlayShadowOpacity: Float
        get() = shipAPI.extraOverlayShadowOpacity
        set(p0) {
            shipAPI.extraOverlayShadowOpacity = p0
        }

    var isExtraOverlayMatchHullColor: Boolean
        get() = shipAPI.isExtraOverlayMatchHullColor
        set(p0) {
            shipAPI.isExtraOverlayMatchHullColor = p0
        }

    fun resetSelectedGroup() {
        shipAPI.resetSelectedGroup()
    }

    fun removeTag(p0: String?) {
        shipAPI.removeTag(p0)
    }

    var isSkipNextDamagedExplosion: Boolean
        get() = shipAPI.isSkipNextDamagedExplosion
        set(p0) {
            shipAPI.isSkipNextDamagedExplosion = p0
        }

    fun setDefaultAI(p0: FleetMemberAPI?) {
        shipAPI.setDefaultAI(p0)
    }

    var isNoDamagedExplosions: Boolean
        get() = shipAPI.isNoDamagedExplosions
        set(p0) {
            shipAPI.isNoDamagedExplosions = p0
        }

    var isDoNotRenderSprite: Boolean
        get() = shipAPI.isDoNotRenderSprite
        set(p0) {
            shipAPI.isDoNotRenderSprite = p0
        }

    var isDoNotRenderShield: Boolean
        get() = shipAPI.isDoNotRenderShield
        set(p0) {
            shipAPI.isDoNotRenderShield = p0
        }

    var isDoNotRenderWeapons: Boolean
        get() = shipAPI.isDoNotRenderWeapons
        set(p0) {
            shipAPI.isDoNotRenderWeapons = p0
        }

    var isDoNotRenderVentingAnimation: Boolean
        get() = shipAPI.isDoNotRenderVentingAnimation
        set(p0) {
            shipAPI.isDoNotRenderVentingAnimation = p0
        }

    var shipCollisionSoundOverride: String?
        get() = shipAPI.shipCollisionSoundOverride
        set(p0) {
            shipAPI.shipCollisionSoundOverride = p0
        }

    var asteroidCollisionSoundOverride: String?
        get() = shipAPI.asteroidCollisionSoundOverride
        set(p0) {
            shipAPI.asteroidCollisionSoundOverride = p0
        }

    var parentPieceId: String?
        get() = shipAPI.parentPieceId
        set(p0) {
            shipAPI.parentPieceId = p0
        }

    fun applyEffectsAfterShipAddedToCombatEngine() {
        shipAPI.applyEffectsAfterShipAddedToCombatEngine()
    }

    val sinceLastDamageTaken: Float
        get() = shipAPI.sinceLastDamageTaken

    var isNoMuzzleFlash: Boolean
        get() = shipAPI.isNoMuzzleFlash
        set(p0) {
            shipAPI.isNoMuzzleFlash = p0
        }

    var isBeingIgnored: Boolean
        get() = shipAPI.isBeingIgnored
        set(p0) {
            shipAPI.isBeingIgnored = p0
        }

    val location: Vector2f
        get() = shipAPI.location

    val velocity: Vector2f
        get() = shipAPI.velocity

    var facing: Float
        get() = shipAPI.facing
        set(p0) {
            shipAPI.facing = p0
        }

    var angularVelocity: Float
        get() = shipAPI.angularVelocity
        set(p0) {
            shipAPI.angularVelocity = p0
        }

    var owner: Int
        get() = shipAPI.owner
        set(p0) {
            shipAPI.owner = p0
        }

    var collisionRadius: Float
        get() = shipAPI.collisionRadius
        set(p0) {
            shipAPI.collisionRadius = p0
        }

    var collisionClass: CollisionClass?
        get() = shipAPI.collisionClass
        set(p0) {
            shipAPI.collisionClass = p0
        }

    var mass: Float
        get() = shipAPI.mass
        set(p0) {
            shipAPI.mass = p0
        }

    val exactBounds: BoundsAPI
        get() = shipAPI.exactBounds as BoundsAPI

    val shield: ShieldAPI
        get() = shipAPI.shield as ShieldAPI

    val hullLevel: Float
        get() = shipAPI.hullLevel

    var hitpoints: Float
        get() = shipAPI.hitpoints
        set(p0) {
            shipAPI.hitpoints = p0
        }

    var maxHitpoints: Float
        get() = shipAPI.maxHitpoints
        set(p0) {
            shipAPI.maxHitpoints = p0
        }

    val ai: Any
        get() = shipAPI.ai

    val isExpired: Boolean
        get() = shipAPI.isExpired

    fun setCustomData(p0: String?, p1: Any?) {
        shipAPI.setCustomData(p0, p1)
    }

    fun removeCustomData(p0: String?) {
        shipAPI.removeCustomData(p0)
    }

    val customData: Map<*, *>
        get() = shipAPI.customData

    fun wasRemoved(): Boolean {
        return shipAPI.wasRemoved()
    }
}
