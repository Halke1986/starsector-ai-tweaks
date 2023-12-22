package com.genir.aitweaks.mock

import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.CombatListenerManagerAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.loading.WeaponSlotAPI
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

open class MockShipAPI :ShipAPI {
    override fun getLocation(): Vector2f {
        TODO("Unexpected mock method call")
    }

    lateinit var getVelocityMock: Vector2f
    override fun getVelocity(): Vector2f = getVelocityMock

    override fun getFacing(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setFacing(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getAngularVelocity(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setAngularVelocity(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getOwner(): Int {
        TODO("Unexpected mock method call")
    }

    override fun setOwner(p0: Int) {
        TODO("Unexpected mock method call")
    }

    override fun getCollisionRadius(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getCollisionClass(): CollisionClass {
        TODO("Unexpected mock method call")
    }

    override fun setCollisionClass(p0: CollisionClass?) {
        TODO("Unexpected mock method call")
    }

    override fun getMass(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setMass(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getExactBounds(): BoundsAPI {
        TODO("Unexpected mock method call")
    }

    override fun getShield(): ShieldAPI {
        TODO("Unexpected mock method call")
    }

    override fun getHullLevel(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getHitpoints(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getMaxHitpoints(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setCollisionRadius(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getAI(): Any {
        TODO("Unexpected mock method call")
    }

    override fun isExpired(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setCustomData(p0: String?, p1: Any?) {
        TODO("Unexpected mock method call")
    }

    override fun removeCustomData(p0: String?) {
        TODO("Unexpected mock method call")
    }

    override fun getCustomData(): MutableMap<String, Any> {
        TODO("Unexpected mock method call")
    }

    override fun setHitpoints(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getFleetMemberId(): String {
        TODO("Unexpected mock method call")
    }

    override fun getMouseTarget(): Vector2f {
        TODO("Unexpected mock method call")
    }

    override fun isShuttlePod(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isDrone(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isFighter(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isFrigate(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isDestroyer(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isCruiser(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isCapital(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getHullSize(): ShipAPI.HullSize {
        TODO("Unexpected mock method call")
    }

    override fun getShipTarget(): ShipAPI {
        TODO("Unexpected mock method call")
    }

    override fun setShipTarget(p0: ShipAPI?) {
        TODO("Unexpected mock method call")
    }

    override fun getOriginalOwner(): Int {
        TODO("Unexpected mock method call")
    }

    override fun setOriginalOwner(p0: Int) {
        TODO("Unexpected mock method call")
    }

    override fun resetOriginalOwner() {
        TODO("Unexpected mock method call")
    }

    override fun getMutableStats(): MutableShipStatsAPI {
        TODO("Unexpected mock method call")
    }

    override fun isHulk(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getAllWeapons(): MutableList<WeaponAPI> {
        TODO("Unexpected mock method call")
    }

    override fun getPhaseCloak(): ShipSystemAPI {
        TODO("Unexpected mock method call")
    }

    override fun getSystem(): ShipSystemAPI {
        TODO("Unexpected mock method call")
    }

    override fun getTravelDrive(): ShipSystemAPI {
        TODO("Unexpected mock method call")
    }

    override fun toggleTravelDrive() {
        TODO("Unexpected mock method call")
    }

    override fun setShield(p0: ShieldAPI.ShieldType?, p1: Float, p2: Float, p3: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getHullSpec(): ShipHullSpecAPI {
        TODO("Unexpected mock method call")
    }

    override fun getVariant(): ShipVariantAPI {
        TODO("Unexpected mock method call")
    }

    override fun useSystem() {
        TODO("Unexpected mock method call")
    }

    override fun getFluxTracker(): FluxTrackerAPI {
        TODO("Unexpected mock method call")
    }

    override fun getWingMembers(): MutableList<ShipAPI> {
        TODO("Unexpected mock method call")
    }

    override fun getWingLeader(): ShipAPI {
        TODO("Unexpected mock method call")
    }

    override fun isWingLeader(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getWing(): FighterWingAPI {
        TODO("Unexpected mock method call")
    }

    override fun getDeployedDrones(): MutableList<ShipAPI> {
        TODO("Unexpected mock method call")
    }

    override fun getDroneSource(): ShipAPI {
        TODO("Unexpected mock method call")
    }

    override fun getWingToken(): Any {
        TODO("Unexpected mock method call")
    }

    override fun getArmorGrid(): ArmorGridAPI {
        TODO("Unexpected mock method call")
    }

    override fun setRenderBounds(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun setCRAtDeployment(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getCRAtDeployment(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getCurrentCR(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setCurrentCR(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getWingCRAtDeployment(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getTimeDeployedForCRReduction(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getFullTimeDeployed(): Float {
        TODO("Unexpected mock method call")
    }

    override fun losesCRDuringCombat(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun controlsLocked(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setControlsLocked(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun setShipSystemDisabled(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun getDisabledWeapons(): MutableSet<WeaponAPI> {
        TODO("Unexpected mock method call")
    }

    override fun getNumFlameouts(): Int {
        TODO("Unexpected mock method call")
    }

    override fun getHullLevelAtDeployment(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setSprite(p0: String?, p1: String?) {
        TODO("Unexpected mock method call")
    }

    override fun setSprite(p0: SpriteAPI?) {
        TODO("Unexpected mock method call")
    }

    override fun getSpriteAPI(): SpriteAPI {
        TODO("Unexpected mock method call")
    }

    override fun getEngineController(): ShipEngineControllerAPI {
        TODO("Unexpected mock method call")
    }

    override fun giveCommand(p0: ShipCommand?, p1: Any?, p2: Int) {
        TODO("Unexpected mock method call")
    }

    override fun setShipAI(p0: ShipAIPlugin?) {
        TODO("Unexpected mock method call")
    }

    override fun getShipAI(): ShipAIPlugin {
        TODO("Unexpected mock method call")
    }

    override fun resetDefaultAI() {
        TODO("Unexpected mock method call")
    }

    override fun turnOnTravelDrive() {
        TODO("Unexpected mock method call")
    }

    override fun turnOnTravelDrive(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun turnOffTravelDrive() {
        TODO("Unexpected mock method call")
    }

    override fun isRetreating(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun abortLanding() {
        TODO("Unexpected mock method call")
    }

    override fun beginLandingAnimation(p0: ShipAPI?) {
        TODO("Unexpected mock method call")
    }

    override fun isLanding(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isFinishedLanding(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isAlive(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isInsideNebula(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setInsideNebula(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun isAffectedByNebula(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setAffectedByNebula(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun getDeployCost(): Float {
        TODO("Unexpected mock method call")
    }

    override fun removeWeaponFromGroups(p0: WeaponAPI?) {
        TODO("Unexpected mock method call")
    }

    override fun applyCriticalMalfunction(p0: Any?) {
        TODO("Unexpected mock method call")
    }

    override fun applyCriticalMalfunction(p0: Any?, p1: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun getBaseCriticalMalfunctionDamage(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getEngineFractionPermanentlyDisabled(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getCombinedAlphaMult(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getLowestHullLevelReached(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getAIFlags(): ShipwideAIFlags {
        TODO("Unexpected mock method call")
    }

    override fun getWeaponGroupsCopy(): MutableList<WeaponGroupAPI> {
        TODO("Unexpected mock method call")
    }

    override fun isHoldFire(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isHoldFireOneFrame(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setHoldFireOneFrame(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun isPhased(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isAlly(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setWeaponGlow(p0: Float, p1: Color?, p2: EnumSet<WeaponAPI.WeaponType>?) {
        TODO("Unexpected mock method call")
    }

    override fun setVentCoreColor(p0: Color?) {
        TODO("Unexpected mock method call")
    }

    override fun setVentFringeColor(p0: Color?) {
        TODO("Unexpected mock method call")
    }

    override fun getVentCoreColor(): Color {
        TODO("Unexpected mock method call")
    }

    override fun getVentFringeColor(): Color {
        TODO("Unexpected mock method call")
    }

    override fun getHullStyleId(): String {
        TODO("Unexpected mock method call")
    }

    override fun getWeaponGroupFor(p0: WeaponAPI?): WeaponGroupAPI {
        TODO("Unexpected mock method call")
    }

    override fun setCopyLocation(p0: Vector2f?, p1: Float, p2: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getCopyLocation(): Vector2f {
        TODO("Unexpected mock method call")
    }

    override fun setAlly(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun getId(): String {
        TODO("Unexpected mock method call")
    }

    override fun getName(): String {
        TODO("Unexpected mock method call")
    }

    override fun setJitter(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float) {
        TODO("Unexpected mock method call")
    }

    override fun setJitter(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float, p5: Float) {
        TODO("Unexpected mock method call")
    }

    override fun setJitterUnder(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float) {
        TODO("Unexpected mock method call")
    }

    override fun setJitterUnder(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float, p5: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getTimeDeployedUnderPlayerControl(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getSmallTurretCover(): SpriteAPI {
        TODO("Unexpected mock method call")
    }

    override fun getSmallHardpointCover(): SpriteAPI {
        TODO("Unexpected mock method call")
    }

    override fun getMediumTurretCover(): SpriteAPI {
        TODO("Unexpected mock method call")
    }

    override fun getMediumHardpointCover(): SpriteAPI {
        TODO("Unexpected mock method call")
    }

    override fun getLargeTurretCover(): SpriteAPI {
        TODO("Unexpected mock method call")
    }

    override fun getLargeHardpointCover(): SpriteAPI {
        TODO("Unexpected mock method call")
    }

    override fun isDefenseDisabled(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setDefenseDisabled(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun setPhased(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun setExtraAlphaMult(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun setApplyExtraAlphaToEngines(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun setOverloadColor(p0: Color?) {
        TODO("Unexpected mock method call")
    }

    override fun resetOverloadColor() {
        TODO("Unexpected mock method call")
    }

    override fun getOverloadColor(): Color {
        TODO("Unexpected mock method call")
    }

    override fun isRecentlyShotByPlayer(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getMaxSpeedWithoutBoost(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getHardFluxLevel(): Float {
        TODO("Unexpected mock method call")
    }

    override fun fadeToColor(p0: Any?, p1: Color?, p2: Float, p3: Float, p4: Float) {
        TODO("Unexpected mock method call")
    }

    override fun isShowModuleJitterUnder(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setShowModuleJitterUnder(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

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
    ) {
        TODO("Unexpected mock method call")
    }

    override fun getCaptain(): PersonAPI {
        TODO("Unexpected mock method call")
    }

    override fun getStationSlot(): WeaponSlotAPI {
        TODO("Unexpected mock method call")
    }

    override fun setStationSlot(p0: WeaponSlotAPI?) {
        TODO("Unexpected mock method call")
    }

    override fun getParentStation(): ShipAPI {
        TODO("Unexpected mock method call")
    }

    override fun setParentStation(p0: ShipAPI?) {
        TODO("Unexpected mock method call")
    }

    override fun getFixedLocation(): Vector2f {
        TODO("Unexpected mock method call")
    }

    override fun setFixedLocation(p0: Vector2f?) {
        TODO("Unexpected mock method call")
    }

    override fun hasRadarRibbonIcon(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isTargetable(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setStation(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun isSelectableInWarroom(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isShipWithModules(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setShipWithModules(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun getChildModulesCopy(): MutableList<ShipAPI> {
        TODO("Unexpected mock method call")
    }

    override fun isPiece(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getVisualBounds(): BoundsAPI {
        TODO("Unexpected mock method call")
    }

    override fun getRenderOffset(): Vector2f {
        TODO("Unexpected mock method call")
    }

    override fun splitShip(): ShipAPI {
        TODO("Unexpected mock method call")
    }

    override fun getNumFighterBays(): Int {
        TODO("Unexpected mock method call")
    }

    override fun isPullBackFighters(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setPullBackFighters(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun hasLaunchBays(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getLaunchBaysCopy(): MutableList<FighterLaunchBayAPI> {
        TODO("Unexpected mock method call")
    }

    override fun getFighterTimeBeforeRefit(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setFighterTimeBeforeRefit(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getAllWings(): MutableList<FighterWingAPI> {
        TODO("Unexpected mock method call")
    }

    override fun getSharedFighterReplacementRate(): Float {
        TODO("Unexpected mock method call")
    }

    override fun areSignificantEnemiesInRange(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getUsableWeapons(): MutableList<WeaponAPI> {
        TODO("Unexpected mock method call")
    }

    override fun getModuleOffset(): Vector2f {
        TODO("Unexpected mock method call")
    }

    override fun getMassWithModules(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getOriginalCaptain(): PersonAPI {
        TODO("Unexpected mock method call")
    }

    override fun isRenderEngines(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setRenderEngines(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun getSelectedGroupAPI(): WeaponGroupAPI {
        TODO("Unexpected mock method call")
    }

    override fun setHullSize(p0: ShipAPI.HullSize?) {
        TODO("Unexpected mock method call")
    }

    override fun ensureClonedStationSlotSpec() {
        TODO("Unexpected mock method call")
    }

    override fun setMaxHitpoints(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun setDHullOverlay(p0: String?) {
        TODO("Unexpected mock method call")
    }

    override fun isStation(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isStationModule(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun areAnyEnemiesInRange(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun blockCommandForOneFrame(p0: ShipCommand?) {
        TODO("Unexpected mock method call")
    }

    override fun getMaxTurnRate(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getTurnAcceleration(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getTurnDeceleration(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getDeceleration(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getAcceleration(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getMaxSpeed(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getFluxLevel(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getCurrFlux(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getMaxFlux(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getMinFluxLevel(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getMinFlux(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setLightDHullOverlay() {
        TODO("Unexpected mock method call")
    }

    override fun setMediumDHullOverlay() {
        TODO("Unexpected mock method call")
    }

    override fun setHeavyDHullOverlay() {
        TODO("Unexpected mock method call")
    }

    override fun isJitterShields(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setJitterShields(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun isInvalidTransferCommandTarget(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setInvalidTransferCommandTarget(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun clearDamageDecals() {
        TODO("Unexpected mock method call")
    }

    override fun syncWithArmorGridState() {
        TODO("Unexpected mock method call")
    }

    override fun syncWeaponDecalsWithArmorDamage() {
        TODO("Unexpected mock method call")
    }

    override fun isDirectRetreat(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setRetreating(p0: Boolean, p1: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun isLiftingOff(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setVariantForHullmodCheckOnly(p0: ShipVariantAPI?) {
        TODO("Unexpected mock method call")
    }

    override fun getShieldCenterEvenIfNoShield(): Vector2f {
        TODO("Unexpected mock method call")
    }

    override fun getShieldRadiusEvenIfNoShield(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getFleetMember(): FleetMemberAPI {
        TODO("Unexpected mock method call")
    }

    override fun getShieldTarget(): Vector2f {
        TODO("Unexpected mock method call")
    }

    override fun setShieldTargetOverride(p0: Float, p1: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getListenerManager(): CombatListenerManagerAPI {
        TODO("Unexpected mock method call")
    }

    override fun addListener(p0: Any?) {
        TODO("Unexpected mock method call")
    }

    override fun removeListener(p0: Any?) {
        TODO("Unexpected mock method call")
    }

    override fun removeListenerOfClass(p0: Class<*>?) {
        TODO("Unexpected mock method call")
    }

    override fun hasListener(p0: Any?): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun hasListenerOfClass(p0: Class<*>?): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun <T : Any?> getListeners(p0: Class<T>?): MutableList<T> {
        TODO("Unexpected mock method call")
    }

    override fun getParamAboutToApplyDamage(): Any {
        TODO("Unexpected mock method call")
    }

    override fun setParamAboutToApplyDamage(p0: Any?) {
        TODO("Unexpected mock method call")
    }

    override fun getFluxBasedEnergyWeaponDamageMultiplier(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setName(p0: String?) {
        TODO("Unexpected mock method call")
    }

    override fun setHulk(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun setCaptain(p0: PersonAPI?) {
        TODO("Unexpected mock method call")
    }

    override fun getShipExplosionRadius(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setCircularJitter(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun getExtraAlphaMult(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setAlphaMult(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getAlphaMult(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setAnimatedLaunch() {
        TODO("Unexpected mock method call")
    }

    override fun setLaunchingShip(p0: ShipAPI?) {
        TODO("Unexpected mock method call")
    }

    override fun isNonCombat(p0: Boolean): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun findBestArmorInArc(p0: Float, p1: Float): Float {
        TODO("Unexpected mock method call")
    }

    override fun getAverageArmorInSlice(p0: Float, p1: Float): Float {
        TODO("Unexpected mock method call")
    }

    override fun setHoldFire(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun cloneVariant() {
        TODO("Unexpected mock method call")
    }

    override fun setTimeDeployed(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun setFluxVentTextureSheet(p0: String?) {
        TODO("Unexpected mock method call")
    }

    override fun getFluxVentTextureSheet(): String {
        TODO("Unexpected mock method call")
    }

    override fun getAimAccuracy(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getForceCarrierTargetTime(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setForceCarrierTargetTime(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getForceCarrierPullBackTime(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setForceCarrierPullBackTime(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getForceCarrierTarget(): ShipAPI {
        TODO("Unexpected mock method call")
    }

    override fun setForceCarrierTarget(p0: ShipAPI?) {
        TODO("Unexpected mock method call")
    }

    override fun setWing(p0: FighterWingAPI?) {
        TODO("Unexpected mock method call")
    }

    override fun getExplosionScale(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setExplosionScale(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getExplosionFlashColorOverride(): Color {
        TODO("Unexpected mock method call")
    }

    override fun setExplosionFlashColorOverride(p0: Color?) {
        TODO("Unexpected mock method call")
    }

    override fun getExplosionVelocityOverride(): Vector2f {
        TODO("Unexpected mock method call")
    }

    override fun setExplosionVelocityOverride(p0: Vector2f?) {
        TODO("Unexpected mock method call")
    }

    override fun setNextHitHullDamageThresholdMult(p0: Float, p1: Float) {
        TODO("Unexpected mock method call")
    }

    override fun isEngineBoostActive(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun makeLookDisabled() {
        TODO("Unexpected mock method call")
    }

    override fun setExtraAlphaMult2(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getExtraAlphaMult2(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setDrone(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun getLayer(): CombatEngineLayers {
        TODO("Unexpected mock method call")
    }

    override fun setLayer(p0: CombatEngineLayers?) {
        TODO("Unexpected mock method call")
    }

    override fun isForceHideFFOverlay(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setForceHideFFOverlay(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun getTags(): MutableSet<String> {
        TODO("Unexpected mock method call")
    }

    override fun addTag(p0: String?) {
        TODO("Unexpected mock method call")
    }

    override fun hasTag(p0: String?): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getPeakTimeRemaining(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        TODO("Unexpected mock method call")
    }
}