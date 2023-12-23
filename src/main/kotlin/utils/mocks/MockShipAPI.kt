package com.genir.aitweaks.utils.mocks

import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.CombatListenerManagerAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.loading.WeaponSlotAPI
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

open class MockShipAPI(vararg values: Pair<String, Any>) : ShipAPI, Mock(*values) {
    override fun getLocation(): Vector2f = getMockValue(object {})

    override fun getVelocity(): Vector2f = getMockValue(object {})

    override fun getFacing(): Float = getMockValue(object {})

    override fun setFacing(p0: Float) = Unit

    override fun getAngularVelocity(): Float = getMockValue(object {})

    override fun setAngularVelocity(p0: Float) = Unit

    override fun getOwner(): Int = getMockValue(object {})

    override fun setOwner(p0: Int) = Unit

    override fun getCollisionRadius(): Float = getMockValue(object {})

    override fun getCollisionClass(): CollisionClass = getMockValue(object {})

    override fun setCollisionClass(p0: CollisionClass?) = Unit

    override fun getMass(): Float = getMockValue(object {})

    override fun setMass(p0: Float) = Unit

    override fun getExactBounds(): BoundsAPI = getMockValue(object {})

    override fun getShield(): ShieldAPI = getMockValue(object {})

    override fun getHullLevel(): Float = getMockValue(object {})

    override fun getHitpoints(): Float = getMockValue(object {})

    override fun getMaxHitpoints(): Float = getMockValue(object {})

    override fun setCollisionRadius(p0: Float) = Unit

    override fun getAI(): Any = getMockValue(object {})

    override fun isExpired(): Boolean = getMockValue(object {})

    override fun setCustomData(p0: String?, p1: Any?) = Unit

    override fun removeCustomData(p0: String?) = Unit

    override fun getCustomData(): MutableMap<String, Any> = getMockValue(object {})

    override fun setHitpoints(p0: Float) = Unit

    override fun getFleetMemberId(): String = getMockValue(object {})

    override fun getMouseTarget(): Vector2f = getMockValue(object {})

    override fun isShuttlePod(): Boolean = getMockValue(object {})

    override fun isDrone(): Boolean = getMockValue(object {})

    override fun isFighter(): Boolean = getMockValue(object {})

    override fun isFrigate(): Boolean = getMockValue(object {})

    override fun isDestroyer(): Boolean = getMockValue(object {})

    override fun isCruiser(): Boolean = getMockValue(object {})

    override fun isCapital(): Boolean = getMockValue(object {})

    override fun getHullSize(): ShipAPI.HullSize = getMockValue(object {})

    override fun getShipTarget(): ShipAPI = getMockValue(object {})

    override fun setShipTarget(p0: ShipAPI?) = Unit

    override fun getOriginalOwner(): Int = getMockValue(object {})

    override fun setOriginalOwner(p0: Int) = Unit

    override fun resetOriginalOwner() = Unit

    override fun getMutableStats(): MutableShipStatsAPI = getMockValue(object {})

    override fun isHulk(): Boolean = getMockValue(object {})

    override fun getAllWeapons(): MutableList<WeaponAPI> = getMockValue(object {})

    override fun getPhaseCloak(): ShipSystemAPI = getMockValue(object {})

    override fun getSystem(): ShipSystemAPI = getMockValue(object {})

    override fun getTravelDrive(): ShipSystemAPI = getMockValue(object {})

    override fun toggleTravelDrive() = Unit

    override fun setShield(p0: ShieldAPI.ShieldType?, p1: Float, p2: Float, p3: Float) = Unit

    override fun getHullSpec(): ShipHullSpecAPI = getMockValue(object {})

    override fun getVariant(): ShipVariantAPI = getMockValue(object {})

    override fun useSystem() = Unit

    override fun getFluxTracker(): FluxTrackerAPI = getMockValue(object {})

    @Deprecated("Deprecated in Java")
    override fun getWingMembers(): MutableList<ShipAPI> = getMockValue(object {})

    override fun getWingLeader(): ShipAPI = getMockValue(object {})

    override fun isWingLeader(): Boolean = getMockValue(object {})

    override fun getWing(): FighterWingAPI = getMockValue(object {})

    override fun getDeployedDrones(): MutableList<ShipAPI> = getMockValue(object {})

    override fun getDroneSource(): ShipAPI = getMockValue(object {})

    override fun getWingToken(): Any = getMockValue(object {})

    override fun getArmorGrid(): ArmorGridAPI = getMockValue(object {})

    override fun setRenderBounds(p0: Boolean) = Unit

    override fun setCRAtDeployment(p0: Float) = Unit

    override fun getCRAtDeployment(): Float = getMockValue(object {})

    override fun getCurrentCR(): Float = getMockValue(object {})

    override fun setCurrentCR(p0: Float) = Unit

    override fun getWingCRAtDeployment(): Float = getMockValue(object {})

    override fun getTimeDeployedForCRReduction(): Float = getMockValue(object {})

    override fun getFullTimeDeployed(): Float = getMockValue(object {})

    override fun losesCRDuringCombat(): Boolean = getMockValue(object {})

    override fun controlsLocked(): Boolean = getMockValue(object {})

    override fun setControlsLocked(p0: Boolean) = Unit

    override fun setShipSystemDisabled(p0: Boolean) = Unit

    override fun getDisabledWeapons(): MutableSet<WeaponAPI> = getMockValue(object {})

    override fun getNumFlameouts(): Int = getMockValue(object {})

    override fun getHullLevelAtDeployment(): Float = getMockValue(object {})

    override fun setSprite(p0: String?, p1: String?) = Unit

    override fun setSprite(p0: SpriteAPI?) = Unit

    override fun getSpriteAPI(): SpriteAPI = getMockValue(object {})

    override fun getEngineController(): ShipEngineControllerAPI = getMockValue(object {})

    override fun giveCommand(p0: ShipCommand?, p1: Any?, p2: Int) = Unit

    override fun setShipAI(p0: ShipAIPlugin?) = Unit

    override fun getShipAI(): ShipAIPlugin = getMockValue(object {})

    override fun resetDefaultAI() = Unit

    override fun turnOnTravelDrive() = Unit

    override fun turnOnTravelDrive(p0: Float) = Unit

    override fun turnOffTravelDrive() = Unit

    override fun isRetreating(): Boolean = getMockValue(object {})

    override fun abortLanding() = Unit

    override fun beginLandingAnimation(p0: ShipAPI?) = Unit

    override fun isLanding(): Boolean = getMockValue(object {})

    override fun isFinishedLanding(): Boolean = getMockValue(object {})

    override fun isAlive(): Boolean = getMockValue(object {})

    override fun isInsideNebula(): Boolean = getMockValue(object {})

    override fun setInsideNebula(p0: Boolean) = Unit

    override fun isAffectedByNebula(): Boolean = getMockValue(object {})

    override fun setAffectedByNebula(p0: Boolean) = Unit

    override fun getDeployCost(): Float = getMockValue(object {})

    override fun removeWeaponFromGroups(p0: WeaponAPI?) = Unit

    override fun applyCriticalMalfunction(p0: Any?) = Unit

    override fun applyCriticalMalfunction(p0: Any?, p1: Boolean) = Unit

    override fun getBaseCriticalMalfunctionDamage(): Float = getMockValue(object {})

    override fun getEngineFractionPermanentlyDisabled(): Float = getMockValue(object {})

    override fun getCombinedAlphaMult(): Float = getMockValue(object {})

    override fun getLowestHullLevelReached(): Float = getMockValue(object {})

    override fun getAIFlags(): ShipwideAIFlags = getMockValue(object {})

    override fun getWeaponGroupsCopy(): MutableList<WeaponGroupAPI> = getMockValue(object {})

    override fun isHoldFire(): Boolean = getMockValue(object {})

    override fun isHoldFireOneFrame(): Boolean = getMockValue(object {})

    override fun setHoldFireOneFrame(p0: Boolean) = Unit

    override fun isPhased(): Boolean = getMockValue(object {})

    override fun isAlly(): Boolean = getMockValue(object {})

    override fun setWeaponGlow(p0: Float, p1: Color?, p2: EnumSet<WeaponAPI.WeaponType>?) = Unit

    override fun setVentCoreColor(p0: Color?) = Unit

    override fun setVentFringeColor(p0: Color?) = Unit

    override fun getVentCoreColor(): Color = getMockValue(object {})

    override fun getVentFringeColor(): Color = getMockValue(object {})

    override fun getHullStyleId(): String = getMockValue(object {})

    override fun getWeaponGroupFor(p0: WeaponAPI?): WeaponGroupAPI = getMockValue(object {})

    override fun setCopyLocation(p0: Vector2f?, p1: Float, p2: Float) = Unit

    override fun getCopyLocation(): Vector2f = getMockValue(object {})

    override fun setAlly(p0: Boolean) = Unit

    override fun getId(): String = getMockValue(object {})

    override fun getName(): String = getMockValue(object {})

    override fun setJitter(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float) = Unit

    override fun setJitter(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float, p5: Float) = Unit

    override fun setJitterUnder(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float) = Unit

    override fun setJitterUnder(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float, p5: Float) = Unit

    override fun getTimeDeployedUnderPlayerControl(): Float = getMockValue(object {})

    override fun getSmallTurretCover(): SpriteAPI = getMockValue(object {})

    override fun getSmallHardpointCover(): SpriteAPI = getMockValue(object {})

    override fun getMediumTurretCover(): SpriteAPI = getMockValue(object {})

    override fun getMediumHardpointCover(): SpriteAPI = getMockValue(object {})

    override fun getLargeTurretCover(): SpriteAPI = getMockValue(object {})

    override fun getLargeHardpointCover(): SpriteAPI = getMockValue(object {})

    override fun isDefenseDisabled(): Boolean = getMockValue(object {})

    override fun setDefenseDisabled(p0: Boolean) = Unit

    override fun setPhased(p0: Boolean) = Unit

    override fun setExtraAlphaMult(p0: Float) = Unit

    override fun setApplyExtraAlphaToEngines(p0: Boolean) = Unit

    override fun setOverloadColor(p0: Color?) = Unit

    override fun resetOverloadColor() = Unit

    override fun getOverloadColor(): Color = getMockValue(object {})

    override fun isRecentlyShotByPlayer(): Boolean = getMockValue(object {})

    override fun getMaxSpeedWithoutBoost(): Float = getMockValue(object {})

    override fun getHardFluxLevel(): Float = getMockValue(object {})

    override fun fadeToColor(p0: Any?, p1: Color?, p2: Float, p3: Float, p4: Float) = Unit

    override fun isShowModuleJitterUnder(): Boolean = getMockValue(object {})

    override fun setShowModuleJitterUnder(p0: Boolean) = Unit

    override fun addAfterimage(
        p0: Color?,
        p1: Float,
        p2: Float,
        p3: Float,
        p4: Float,
        p5: Float,
        p6: Float,
        p7: Float,
        p8: Float,
        p9: Boolean,
        p10: Boolean,
        p11: Boolean
    ) = Unit

    override fun getCaptain(): PersonAPI = getMockValue(object {})

    override fun getStationSlot(): WeaponSlotAPI = getMockValue(object {})

    override fun setStationSlot(p0: WeaponSlotAPI?) = Unit

    override fun getParentStation(): ShipAPI = getMockValue(object {})

    override fun setParentStation(p0: ShipAPI?) = Unit

    override fun getFixedLocation(): Vector2f = getMockValue(object {})

    override fun setFixedLocation(p0: Vector2f?) = Unit

    override fun hasRadarRibbonIcon(): Boolean = getMockValue(object {})

    override fun isTargetable(): Boolean = getMockValue(object {})

    override fun setStation(p0: Boolean) = Unit

    override fun isSelectableInWarroom(): Boolean = getMockValue(object {})

    override fun isShipWithModules(): Boolean = getMockValue(object {})

    override fun setShipWithModules(p0: Boolean) = Unit

    override fun getChildModulesCopy(): MutableList<ShipAPI> = getMockValue(object {})

    override fun isPiece(): Boolean = getMockValue(object {})

    override fun getVisualBounds(): BoundsAPI = getMockValue(object {})

    override fun getRenderOffset(): Vector2f = getMockValue(object {})

    override fun splitShip(): ShipAPI = getMockValue(object {})

    override fun getNumFighterBays(): Int = getMockValue(object {})

    override fun isPullBackFighters(): Boolean = getMockValue(object {})

    override fun setPullBackFighters(p0: Boolean) = Unit

    override fun hasLaunchBays(): Boolean = getMockValue(object {})

    override fun getLaunchBaysCopy(): MutableList<FighterLaunchBayAPI> = getMockValue(object {})

    override fun getFighterTimeBeforeRefit(): Float = getMockValue(object {})

    override fun setFighterTimeBeforeRefit(p0: Float) = Unit

    override fun getAllWings(): MutableList<FighterWingAPI> = getMockValue(object {})

    override fun getSharedFighterReplacementRate(): Float = getMockValue(object {})

    override fun areSignificantEnemiesInRange(): Boolean = getMockValue(object {})

    override fun getUsableWeapons(): MutableList<WeaponAPI> = getMockValue(object {})

    override fun getModuleOffset(): Vector2f = getMockValue(object {})

    override fun getMassWithModules(): Float = getMockValue(object {})

    override fun getOriginalCaptain(): PersonAPI = getMockValue(object {})

    override fun isRenderEngines(): Boolean = getMockValue(object {})

    override fun setRenderEngines(p0: Boolean) = Unit

    override fun getSelectedGroupAPI(): WeaponGroupAPI = getMockValue(object {})

    override fun setHullSize(p0: ShipAPI.HullSize?) = Unit

    override fun ensureClonedStationSlotSpec() = Unit

    override fun setMaxHitpoints(p0: Float) = Unit

    override fun setDHullOverlay(p0: String?) = Unit

    override fun isStation(): Boolean = getMockValue(object {})

    override fun isStationModule(): Boolean = getMockValue(object {})

    override fun areAnyEnemiesInRange(): Boolean = getMockValue(object {})

    override fun blockCommandForOneFrame(p0: ShipCommand?) = Unit

    override fun getMaxTurnRate(): Float = getMockValue(object {})

    override fun getTurnAcceleration(): Float = getMockValue(object {})

    override fun getTurnDeceleration(): Float = getMockValue(object {})

    override fun getDeceleration(): Float = getMockValue(object {})

    override fun getAcceleration(): Float = getMockValue(object {})

    override fun getMaxSpeed(): Float = getMockValue(object {})

    override fun getFluxLevel(): Float = getMockValue(object {})

    override fun getCurrFlux(): Float = getMockValue(object {})

    override fun getMaxFlux(): Float = getMockValue(object {})

    override fun getMinFluxLevel(): Float = getMockValue(object {})

    override fun getMinFlux(): Float = getMockValue(object {})

    override fun setLightDHullOverlay() = Unit

    override fun setMediumDHullOverlay() = Unit

    override fun setHeavyDHullOverlay() = Unit

    override fun isJitterShields(): Boolean = getMockValue(object {})

    override fun setJitterShields(p0: Boolean) = Unit

    override fun isInvalidTransferCommandTarget(): Boolean = getMockValue(object {})

    override fun setInvalidTransferCommandTarget(p0: Boolean) = Unit

    override fun clearDamageDecals() = Unit

    override fun syncWithArmorGridState() = Unit

    override fun syncWeaponDecalsWithArmorDamage() = Unit

    override fun isDirectRetreat(): Boolean = getMockValue(object {})

    override fun setRetreating(p0: Boolean, p1: Boolean) = Unit

    override fun isLiftingOff(): Boolean = getMockValue(object {})

    override fun setVariantForHullmodCheckOnly(p0: ShipVariantAPI?) = Unit

    override fun getShieldCenterEvenIfNoShield(): Vector2f = getMockValue(object {})

    override fun getShieldRadiusEvenIfNoShield(): Float = getMockValue(object {})

    override fun getFleetMember(): FleetMemberAPI = getMockValue(object {})

    override fun getShieldTarget(): Vector2f = getMockValue(object {})

    override fun setShieldTargetOverride(p0: Float, p1: Float) = Unit

    override fun getListenerManager(): CombatListenerManagerAPI = getMockValue(object {})

    override fun addListener(p0: Any?) = Unit

    override fun removeListener(p0: Any?) = Unit

    override fun removeListenerOfClass(p0: Class<*>?) = Unit

    override fun hasListener(p0: Any?): Boolean = getMockValue(object {})

    override fun hasListenerOfClass(p0: Class<*>?): Boolean = getMockValue(object {})

    override fun <T : Any?> getListeners(p0: Class<T>?): MutableList<T> = getMockValue(object {})

    override fun getParamAboutToApplyDamage(): Any = getMockValue(object {})

    override fun setParamAboutToApplyDamage(p0: Any?) = Unit

    override fun getFluxBasedEnergyWeaponDamageMultiplier(): Float = getMockValue(object {})

    override fun setName(p0: String?) = Unit

    override fun setHulk(p0: Boolean) = Unit

    override fun setCaptain(p0: PersonAPI?) = Unit

    override fun getShipExplosionRadius(): Float = getMockValue(object {})

    override fun setCircularJitter(p0: Boolean) = Unit

    override fun getExtraAlphaMult(): Float = getMockValue(object {})

    override fun setAlphaMult(p0: Float) = Unit

    override fun getAlphaMult(): Float = getMockValue(object {})

    override fun setAnimatedLaunch() = Unit

    override fun setLaunchingShip(p0: ShipAPI?) = Unit

    override fun isNonCombat(p0: Boolean): Boolean = getMockValue(object {})

    override fun findBestArmorInArc(p0: Float, p1: Float): Float = getMockValue(object {})

    override fun getAverageArmorInSlice(p0: Float, p1: Float): Float = getMockValue(object {})

    override fun setHoldFire(p0: Boolean) = Unit

    override fun cloneVariant() = Unit

    override fun setTimeDeployed(p0: Float) = Unit

    override fun setFluxVentTextureSheet(p0: String?) = Unit

    override fun getFluxVentTextureSheet(): String = getMockValue(object {})

    override fun getAimAccuracy(): Float = getMockValue(object {})

    override fun getForceCarrierTargetTime(): Float = getMockValue(object {})

    override fun setForceCarrierTargetTime(p0: Float) = Unit

    override fun getForceCarrierPullBackTime(): Float = getMockValue(object {})

    override fun setForceCarrierPullBackTime(p0: Float) = Unit

    override fun getForceCarrierTarget(): ShipAPI = getMockValue(object {})

    override fun setForceCarrierTarget(p0: ShipAPI?) = Unit

    override fun setWing(p0: FighterWingAPI?) = Unit

    override fun getExplosionScale(): Float = getMockValue(object {})

    override fun setExplosionScale(p0: Float) = Unit

    override fun getExplosionFlashColorOverride(): Color = getMockValue(object {})

    override fun setExplosionFlashColorOverride(p0: Color?) = Unit

    override fun getExplosionVelocityOverride(): Vector2f = getMockValue(object {})

    override fun setExplosionVelocityOverride(p0: Vector2f?) = Unit

    override fun setNextHitHullDamageThresholdMult(p0: Float, p1: Float) = Unit

    override fun isEngineBoostActive(): Boolean = getMockValue(object {})

    override fun makeLookDisabled() = Unit

    override fun setExtraAlphaMult2(p0: Float) = Unit

    override fun getExtraAlphaMult2(): Float = getMockValue(object {})

    override fun setDrone(p0: Boolean) = Unit

    override fun getLayer(): CombatEngineLayers = getMockValue(object {})

    override fun setLayer(p0: CombatEngineLayers?) = Unit

    override fun isForceHideFFOverlay(): Boolean = getMockValue(object {})

    override fun setForceHideFFOverlay(p0: Boolean) = Unit

    override fun getTags(): MutableSet<String> = getMockValue(object {})

    override fun addTag(p0: String?) = Unit

    override fun hasTag(p0: String?): Boolean = getMockValue(object {})

    override fun getPeakTimeRemaining(): Float = getMockValue(object {})

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> = getMockValue(object {})
}