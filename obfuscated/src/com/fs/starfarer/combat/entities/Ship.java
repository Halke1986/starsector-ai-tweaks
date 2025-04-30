package com.fs.starfarer.combat.entities;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.CombatListenerManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.combat.ai.AI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// UNOBFUSCATED
public class Ship implements ShipAPI {
    // UNOBFUSCATED
    public String getPersonality() {
        return null;
    }

    // UNOBFUSCATED
    public EnumSet<Command> getBlockedCommands() {
        return null;
    }

    // UNOBFUSCATED
    public void setNoWeaponSelected() {
    }

    // UNOBFUSCATED
    public void setFallbackPersonalityId(String var1) {
    }

    // UNOBFUSCATED
    public List<CommandWrapper> getCommands() {
        return null;
    }

    @Override
    public String getFleetMemberId() {
        return null;
    }

    @Override
    public Vector2f getMouseTarget() {
        return null;
    }

    @Override
    public boolean isShuttlePod() {
        return false;
    }

    @Override
    public boolean isDrone() {
        return false;
    }

    @Override
    public void setDrone(boolean isDrone) {

    }

    @Override
    public boolean isFighter() {
        return false;
    }

    @Override
    public boolean isFrigate() {
        return false;
    }

    @Override
    public boolean isDestroyer() {
        return false;
    }

    @Override
    public boolean isCruiser() {
        return false;
    }

    @Override
    public boolean isCapital() {
        return false;
    }

    @Override
    public HullSize getHullSize() {
        return null;
    }

    @Override
    public void setHullSize(HullSize hullSize) {

    }

    @Override
    public ShipAPI getShipTarget() {
        return null;
    }

    @Override
    public void setShipTarget(ShipAPI ship) {

    }

    @Override
    public int getOriginalOwner() {
        return 0;
    }

    @Override
    public void setOriginalOwner(int originalOwner) {

    }

    @Override
    public void resetOriginalOwner() {

    }

    @Override
    public MutableShipStatsAPI getMutableStats() {
        return null;
    }

    @Override
    public boolean isHulk() {
        return false;
    }

    @Override
    public void setHulk(boolean isHulk) {

    }

    @Override
    public List<WeaponAPI> getAllWeapons() {
        return null;
    }

    @Override
    public ShipSystemAPI getPhaseCloak() {
        return null;
    }

    @Override
    public ShipSystemAPI getSystem() {
        return null;
    }

    @Override
    public ShipSystemAPI getTravelDrive() {
        return null;
    }

    @Override
    public void toggleTravelDrive() {

    }

    @Override
    public void setShield(ShieldAPI.ShieldType type, float shieldUpkeep, float shieldEfficiency, float arc) {

    }

    @Override
    public ShipHullSpecAPI getHullSpec() {
        return null;
    }

    @Override
    public ShipVariantAPI getVariant() {
        return null;
    }

    @Override
    public void useSystem() {

    }

    @Override
    public FluxTrackerAPI getFluxTracker() {
        return null;
    }

    @Override
    @Deprecated
    public List<ShipAPI> getWingMembers() {
        return null;
    }

    @Override
    public ShipAPI getWingLeader() {
        return null;
    }

    @Override
    public boolean isWingLeader() {
        return false;
    }

    @Override
    public FighterWingAPI getWing() {
        return null;
    }

    @Override
    public void setWing(FighterWingAPI wing) {

    }

    @Override
    public List<ShipAPI> getDeployedDrones() {
        return null;
    }

    @Override
    public ShipAPI getDroneSource() {
        return null;
    }

    @Override
    public java.lang.Object getWingToken() {
        return null;
    }

    @Override
    public ArmorGridAPI getArmorGrid() {
        return null;
    }

    @Override
    public void setRenderBounds(boolean renderBounds) {

    }

    @Override
    public float getCRAtDeployment() {
        return 0;
    }

    @Override
    public void setCRAtDeployment(float cr) {

    }

    @Override
    public float getCurrentCR() {
        return 0;
    }

    @Override
    public void setCurrentCR(float cr) {

    }

    @Override
    public float getWingCRAtDeployment() {
        return 0;
    }

    @Override
    public Vector2f getLocation() {
        return null;
    }

    @Override
    public Vector2f getVelocity() {
        return null;
    }

    @Override
    public float getFacing() {
        return 0;
    }

    @Override
    public void setFacing(float facing) {

    }

    @Override
    public float getAngularVelocity() {
        return 0;
    }

    @Override
    public void setAngularVelocity(float angVel) {

    }

    @Override
    public int getOwner() {
        return 0;
    }

    @Override
    public void setOwner(int owner) {

    }

    @Override
    public float getCollisionRadius() {
        return 0;
    }

    @Override
    public void setCollisionRadius(float radius) {

    }

    @Override
    public CollisionClass getCollisionClass() {
        return null;
    }

    @Override
    public void setCollisionClass(CollisionClass collisionClass) {

    }

    @Override
    public float getMass() {
        return 0;
    }

    @Override
    public void setMass(float mass) {

    }

    @Override
    public BoundsAPI getExactBounds() {
        return null;
    }

    @Override
    public ShieldAPI getShield() {
        return null;
    }

    @Override
    public float getHullLevel() {
        return 0;
    }

    @Override
    public float getHitpoints() {
        return 0;
    }

    @Override
    public void setHitpoints(float value) {

    }

    @Override
    public float getMaxHitpoints() {
        return 0;
    }

    @Override
    public void setMaxHitpoints(float maxArmor) {

    }

    @Override
    public AI getAI() {
        return null;
    }

    // UNOBFUSCATED
    public void setAI(AI var1) {
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public void setCustomData(String key, java.lang.Object data) {

    }

    @Override
    public void removeCustomData(String key) {

    }

    @Override
    public Map<String, java.lang.Object> getCustomData() {
        return null;
    }

    @Override
    public float getTimeDeployedForCRReduction() {
        return 0;
    }

    @Override
    public float getFullTimeDeployed() {
        return 0;
    }

    @Override
    public boolean losesCRDuringCombat() {
        return false;
    }

    @Override
    public boolean controlsLocked() {
        return false;
    }

    @Override
    public void setControlsLocked(boolean controlsLocked) {

    }

    @Override
    public Set<WeaponAPI> getDisabledWeapons() {
        return null;
    }

    @Override
    public int getNumFlameouts() {
        return 0;
    }

    @Override
    public float getHullLevelAtDeployment() {
        return 0;
    }

    @Override
    public void setSprite(String category, String key) {

    }

    @Override
    public SpriteAPI getSpriteAPI() {
        return null;
    }

    @Override
    public ShipEngineControllerAPI getEngineController() {
        return null;
    }

    @Override
    public void giveCommand(ShipCommand command, java.lang.Object param, int groupNumber) {

    }

    @Override
    public ShipAIPlugin getShipAI() {
        return null;
    }

    @Override
    public void setShipAI(ShipAIPlugin ai) {

    }

    @Override
    public void resetDefaultAI() {

    }

    @Override
    public void turnOnTravelDrive() {

    }

    @Override
    public void turnOnTravelDrive(float dur) {

    }

    @Override
    public void turnOffTravelDrive() {

    }

    @Override
    public boolean isRetreating() {
        return false;
    }

    @Override
    public void abortLanding() {

    }

    @Override
    public void beginLandingAnimation(ShipAPI target) {

    }

    @Override
    public boolean isLanding() {
        return false;
    }

    @Override
    public boolean isFinishedLanding() {
        return false;
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public boolean isInsideNebula() {
        return false;
    }

    @Override
    public void setInsideNebula(boolean isInsideNebula) {

    }

    @Override
    public boolean isAffectedByNebula() {
        return false;
    }

    @Override
    public void setAffectedByNebula(boolean affectedByNebula) {

    }

    @Override
    public float getDeployCost() {
        return 0;
    }

    @Override
    public void removeWeaponFromGroups(WeaponAPI weapon) {

    }

    @Override
    public void applyCriticalMalfunction(java.lang.Object module) {

    }

    @Override
    public float getBaseCriticalMalfunctionDamage() {
        return 0;
    }

    @Override
    public float getEngineFractionPermanentlyDisabled() {
        return 0;
    }

    @Override
    public float getCombinedAlphaMult() {
        return 0;
    }

    @Override
    public float getLowestHullLevelReached() {
        return 0;
    }

    @Override
    public void setLowestHullLevelReached(float lowestHullLevelReached) {

    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return null;
    }

    @Override
    public List<WeaponGroupAPI> getWeaponGroupsCopy() {
        return null;
    }

    @Override
    public boolean isHoldFire() {
        return false;
    }

    @Override
    public void setHoldFire(boolean holdFire) {

    }

    @Override
    public boolean isHoldFireOneFrame() {
        return false;
    }

    @Override
    public void setHoldFireOneFrame(boolean holdFireOneFrame) {

    }

    @Override
    public boolean isPhased() {
        return false;
    }

    @Override
    public void setPhased(boolean phased) {

    }

    @Override
    public boolean isAlly() {
        return false;
    }

    @Override
    public void setAlly(boolean ally) {

    }

    @Override
    public void setWeaponGlow(float glow, Color color, EnumSet<WeaponAPI.WeaponType> types) {

    }

    @Override
    public Color getVentCoreColor() {
        return null;
    }

    @Override
    public void setVentCoreColor(Color color) {

    }

    @Override
    public Color getVentFringeColor() {
        return null;
    }

    @Override
    public void setVentFringeColor(Color color) {

    }

    @Override
    public String getHullStyleId() {
        return null;
    }

    @Override
    public WeaponGroupAPI getWeaponGroupFor(WeaponAPI weapon) {
        return null;
    }

    @Override
    public void setCopyLocation(Vector2f loc, float copyAlpha, float copyFacing) {

    }

    @Override
    public Vector2f getCopyLocation() {
        return null;
    }

    @Override
    public void applyCriticalMalfunction(java.lang.Object module, boolean permanent) {

    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void setJitter(java.lang.Object source, Color color, float intensity, int copies, float range) {

    }

    @Override
    public void setJitterUnder(java.lang.Object source, Color color, float intensity, int copies, float range) {

    }

    @Override
    public void setJitter(java.lang.Object source, Color color, float intensity, int copies, float minRange, float range) {

    }

    @Override
    public void setJitterUnder(java.lang.Object source, Color color, float intensity, int copies, float minRange, float range) {

    }

    @Override
    public float getTimeDeployedUnderPlayerControl() {
        return 0;
    }

    @Override
    public SpriteAPI getSmallTurretCover() {
        return null;
    }

    @Override
    public SpriteAPI getSmallHardpointCover() {
        return null;
    }

    @Override
    public SpriteAPI getMediumTurretCover() {
        return null;
    }

    @Override
    public SpriteAPI getMediumHardpointCover() {
        return null;
    }

    @Override
    public SpriteAPI getLargeTurretCover() {
        return null;
    }

    @Override
    public SpriteAPI getLargeHardpointCover() {
        return null;
    }

    @Override
    public boolean isDefenseDisabled() {
        return false;
    }

    @Override
    public void setDefenseDisabled(boolean defenseDisabled) {

    }

    @Override
    public void setApplyExtraAlphaToEngines(boolean applyExtraAlphaToEngines) {

    }

    @Override
    public void resetOverloadColor() {

    }

    @Override
    public Color getOverloadColor() {
        return null;
    }

    @Override
    public void setOverloadColor(Color color) {

    }

    @Override
    public boolean isRecentlyShotByPlayer() {
        return false;
    }

    @Override
    public float getMaxSpeedWithoutBoost() {
        return 0;
    }

    @Override
    public float getHardFluxLevel() {
        return 0;
    }

    @Override
    public void fadeToColor(java.lang.Object source, Color color, float durIn, float durOut, float maxShift) {

    }

    @Override
    public boolean isShowModuleJitterUnder() {
        return false;
    }

    @Override
    public void setShowModuleJitterUnder(boolean showModuleJitterUnder) {

    }

    @Override
    public void addAfterimage(Color color, float locX, float locY, float velX, float velY, float maxJitter, float in, float dur, float out, boolean additive, boolean combineWithSpriteColor, boolean aboveShip) {

    }

    @Override
    public PersonAPI getCaptain() {
        return null;
    }

    @Override
    public void setCaptain(PersonAPI captain) {

    }

    @Override
    public WeaponSlotAPI getStationSlot() {
        return null;
    }

    @Override
    public void setStationSlot(WeaponSlotAPI stationSlot) {

    }

    @Override
    public ShipAPI getParentStation() {
        return null;
    }

    @Override
    public void setParentStation(ShipAPI station) {

    }

    @Override
    public Vector2f getFixedLocation() {
        return null;
    }

    @Override
    public void setFixedLocation(Vector2f fixedLocation) {

    }

    @Override
    public boolean hasRadarRibbonIcon() {
        return false;
    }

    @Override
    public boolean isTargetable() {
        return false;
    }

    @Override
    public boolean isSelectableInWarroom() {
        return false;
    }

    @Override
    public boolean isShipWithModules() {
        return false;
    }

    @Override
    public void setShipWithModules(boolean isShipWithModules) {

    }

    @Override
    public List<ShipAPI> getChildModulesCopy() {
        return null;
    }

    @Override
    public boolean isPiece() {
        return false;
    }

    @Override
    public BoundsAPI getVisualBounds() {
        return null;
    }

    @Override
    public Vector2f getRenderOffset() {
        return null;
    }

    @Override
    public ShipAPI splitShip() {
        return null;
    }

    @Override
    public int getNumFighterBays() {
        return 0;
    }

    @Override
    public boolean isPullBackFighters() {
        return false;
    }

    @Override
    public void setPullBackFighters(boolean pullBackFighters) {

    }

    @Override
    public boolean hasLaunchBays() {
        return false;
    }

    @Override
    public List<FighterLaunchBayAPI> getLaunchBaysCopy() {
        return null;
    }

    @Override
    public float getFighterTimeBeforeRefit() {
        return 0;
    }

    @Override
    public void setFighterTimeBeforeRefit(float fighterTimeBeforeRefit) {

    }

    @Override
    public List<FighterWingAPI> getAllWings() {
        return null;
    }

    @Override
    public float getSharedFighterReplacementRate() {
        return 0;
    }

    @Override
    public boolean areSignificantEnemiesInRange() {
        return false;
    }

    @Override
    public List<WeaponAPI> getUsableWeapons() {
        return null;
    }

    @Override
    public Vector2f getModuleOffset() {
        return null;
    }

    @Override
    public float getMassWithModules() {
        return 0;
    }

    @Override
    public PersonAPI getOriginalCaptain() {
        return null;
    }

    @Override
    public boolean isRenderEngines() {
        return false;
    }

    @Override
    public void setRenderEngines(boolean renderEngines) {

    }

    @Override
    public WeaponGroupAPI getSelectedGroupAPI() {
        return null;
    }

    @Override
    public void ensureClonedStationSlotSpec() {

    }

    @Override
    public void setDHullOverlay(String spriteName) {

    }

    @Override
    public boolean isStation() {
        return false;
    }

    @Override
    public void setStation(boolean isStation) {

    }

    @Override
    public boolean isStationModule() {
        return false;
    }

    @Override
    public boolean areAnyEnemiesInRange() {
        return false;
    }

    @Override
    public void blockCommandForOneFrame(ShipCommand command) {

    }

    @Override
    public float getMaxTurnRate() {
        return 0;
    }

    @Override
    public float getTurnAcceleration() {
        return 0;
    }

    @Override
    public float getTurnDeceleration() {
        return 0;
    }

    @Override
    public float getDeceleration() {
        return 0;
    }

    @Override
    public float getAcceleration() {
        return 0;
    }

    @Override
    public float getMaxSpeed() {
        return 0;
    }

    @Override
    public float getFluxLevel() {
        return 0;
    }

    @Override
    public float getCurrFlux() {
        return 0;
    }

    @Override
    public float getMaxFlux() {
        return 0;
    }

    @Override
    public float getMinFluxLevel() {
        return 0;
    }

    @Override
    public float getMinFlux() {
        return 0;
    }

    @Override
    public void setLightDHullOverlay() {

    }

    @Override
    public void setMediumDHullOverlay() {

    }

    @Override
    public void setHeavyDHullOverlay() {

    }

    @Override
    public boolean isJitterShields() {
        return false;
    }

    @Override
    public void setJitterShields(boolean jitterShields) {

    }

    @Override
    public boolean isInvalidTransferCommandTarget() {
        return false;
    }

    @Override
    public void setInvalidTransferCommandTarget(boolean invalidTransferCommandTarget) {

    }

    @Override
    public void clearDamageDecals() {

    }

    @Override
    public void syncWithArmorGridState() {

    }

    @Override
    public void syncWeaponDecalsWithArmorDamage() {

    }

    @Override
    public boolean isDirectRetreat() {
        return false;
    }

    @Override
    public void setRetreating(boolean retreating, boolean direct) {

    }

    @Override
    public boolean isLiftingOff() {
        return false;
    }

    @Override
    public void setVariantForHullmodCheckOnly(ShipVariantAPI variant) {

    }

    @Override
    public Vector2f getShieldCenterEvenIfNoShield() {
        return null;
    }

    @Override
    public float getShieldRadiusEvenIfNoShield() {
        return 0;
    }

    @Override
    public FleetMemberAPI getFleetMember() {
        return null;
    }

    @Override
    public void setFleetMember(FleetMemberAPI member) {

    }

    @Override
    public Vector2f getShieldTarget() {
        return null;
    }

    @Override
    public void setShieldTargetOverride(float x, float y) {

    }

    @Override
    public CombatListenerManagerAPI getListenerManager() {
        return null;
    }

    @Override
    public void addListener(java.lang.Object listener) {

    }

    @Override
    public void removeListener(java.lang.Object listener) {

    }

    @Override
    public void removeListenerOfClass(Class<?> c) {

    }

    @Override
    public boolean hasListener(java.lang.Object listener) {
        return false;
    }

    @Override
    public boolean hasListenerOfClass(Class<?> c) {
        return false;
    }

    @Override
    public <T> List<T> getListeners(Class<T> c) {
        return null;
    }

    @Override
    public java.lang.Object getParamAboutToApplyDamage() {
        return null;
    }

    @Override
    public void setParamAboutToApplyDamage(java.lang.Object param) {

    }

    @Override
    public float getFluxBasedEnergyWeaponDamageMultiplier() {
        return 0;
    }

    @Override
    public float getShipExplosionRadius() {
        return 0;
    }

    @Override
    public void setCircularJitter(boolean circular) {

    }

    @Override
    public float getExtraAlphaMult() {
        return 0;
    }

    @Override
    public void setExtraAlphaMult(float transparency) {

    }

    @Override
    public float getAlphaMult() {
        return 0;
    }

    @Override
    public void setAlphaMult(float alphaMult) {

    }

    @Override
    public void setAnimatedLaunch() {

    }

    @Override
    public void setLaunchingShip(ShipAPI launchingShip) {

    }

    @Override
    public boolean isNonCombat(boolean considerOrders) {
        return false;
    }

    @Override
    public float findBestArmorInArc(float facing, float arc) {
        return 0;
    }

    @Override
    public float getAverageArmorInSlice(float direction, float arc) {
        return 0;
    }

    @Override
    public void cloneVariant() {

    }

    @Override
    public void setTimeDeployed(float timeDeployed) {

    }

    @Override
    public String getFluxVentTextureSheet() {
        return null;
    }

    @Override
    public void setFluxVentTextureSheet(String textureId) {

    }

    @Override
    public float getAimAccuracy() {
        return 0;
    }

    @Override
    public float getForceCarrierTargetTime() {
        return 0;
    }

    @Override
    public void setForceCarrierTargetTime(float forceCarrierTargetTime) {

    }

    @Override
    public float getForceCarrierPullBackTime() {
        return 0;
    }

    @Override
    public void setForceCarrierPullBackTime(float forceCarrierPullBackTime) {

    }

    @Override
    public ShipAPI getForceCarrierTarget() {
        return null;
    }

    @Override
    public void setForceCarrierTarget(ShipAPI forceCarrierTarget) {

    }

    @Override
    public float getExplosionScale() {
        return 0;
    }

    @Override
    public void setExplosionScale(float explosionScale) {

    }

    @Override
    public Color getExplosionFlashColorOverride() {
        return null;
    }

    @Override
    public void setExplosionFlashColorOverride(Color explosionFlashColorOverride) {

    }

    @Override
    public Vector2f getExplosionVelocityOverride() {
        return null;
    }

    @Override
    public void setExplosionVelocityOverride(Vector2f explosionVelocityOverride) {

    }

    @Override
    public void setNextHitHullDamageThresholdMult(float threshold, float multBeyondThreshold) {

    }

    @Override
    public boolean isEngineBoostActive() {
        return false;
    }

    @Override
    public void makeLookDisabled() {

    }

    @Override
    public float getExtraAlphaMult2() {
        return 0;
    }

    @Override
    public void setExtraAlphaMult2(float transparency) {

    }

    @Override
    public CombatEngineLayers getLayer() {
        return null;
    }

    @Override
    public void setLayer(CombatEngineLayers layer) {

    }

    @Override
    public boolean isForceHideFFOverlay() {
        return false;
    }

    @Override
    public void setForceHideFFOverlay(boolean forceHideFFOverlay) {

    }

    @Override
    public Set<String> getTags() {
        return null;
    }

    @Override
    public void addTag(String tag) {

    }

    @Override
    public boolean hasTag(String tag) {
        return false;
    }

    @Override
    public void setSprite(SpriteAPI sprite) {

    }

    @Override
    public float getPeakTimeRemaining() {
        return 0;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return null;
    }

    @Override
    public boolean isShipSystemDisabled() {
        return false;
    }

    @Override
    public void setShipSystemDisabled(boolean systemDisabled) {

    }

    @Override
    public boolean isDoNotFlareEnginesWhenStrafingOrDecelerating() {
        return false;
    }

    @Override
    public void setDoNotFlareEnginesWhenStrafingOrDecelerating(boolean doNotFlare) {

    }

    @Override
    public PersonAPI getFleetCommander() {
        return null;
    }

    @Override
    public boolean isDoNotRender() {
        return false;
    }

    @Override
    public void setDoNotRender(boolean doNotRender) {

    }

    @Override
    public float getHulkChanceOverride() {
        return 0;
    }

    @Override
    public void setHulkChanceOverride(float hulkChanceOverride) {

    }

    @Override
    public float getImpactVolumeMult() {
        return 0;
    }

    @Override
    public void setImpactVolumeMult(float impactVolumeMult) {

    }

    @Override
    public Vector2f checkCollisionVsRay(Vector2f from, Vector2f to) {
        return null;
    }

    @Override
    public boolean isPointInBounds(Vector2f p) {
        return false;
    }

    @Override
    public boolean wasRemoved() {
        return false;
    }

    @Override
    public boolean isSpawnDebris() {
        return false;
    }

    @Override
    public void setSpawnDebris(boolean spawnDebris) {

    }

    @Override
    public float getDHullOverlayAngleOffset() {
        return 0;
    }

    @Override
    public void setDHullOverlayAngleOffset(float dHullOverlayAngleOffset) {

    }

    @Override
    public float getExtraOverlayAngleOffset() {
        return 0;
    }

    @Override
    public void setExtraOverlayAngleOffset(float extraOverlayAngleOffset) {

    }

    @Override
    public void setExtraOverlay(String spriteName) {

    }

    @Override
    public float getExtraOverlayShadowOpacity() {
        return 0;
    }

    @Override
    public void setExtraOverlayShadowOpacity(float extraOverlayOpacity) {

    }

    @Override
    public boolean isExtraOverlayMatchHullColor() {
        return false;
    }

    @Override
    public void setExtraOverlayMatchHullColor(boolean extraOverlayMatchHullColor) {

    }

    @Override
    public void resetSelectedGroup() {

    }

    @Override
    public void removeTag(String tag) {

    }

    @Override
    public boolean isSkipNextDamagedExplosion() {
        return false;
    }

    @Override
    public void setSkipNextDamagedExplosion(boolean skipNextDamagedExplosion) {

    }

    @Override
    public void setDefaultAI(FleetMemberAPI member) {

    }

    @Override
    public boolean isNoDamagedExplosions() {
        return false;
    }

    @Override
    public void setNoDamagedExplosions(boolean noDamagedExplosions) {

    }

    @Override
    public boolean isDoNotRenderSprite() {
        return false;
    }

    @Override
    public void setDoNotRenderSprite(boolean doNotRenderSprite) {

    }

    @Override
    public boolean isDoNotRenderShield() {
        return false;
    }

    @Override
    public void setDoNotRenderShield(boolean doNotRenderShield) {

    }

    @Override
    public boolean isDoNotRenderWeapons() {
        return false;
    }

    @Override
    public void setDoNotRenderWeapons(boolean doNotRenderWeapons) {

    }

    @Override
    public boolean isDoNotRenderVentingAnimation() {
        return false;
    }

    @Override
    public void setDoNotRenderVentingAnimation(boolean doNotRenderVentingAnimation) {

    }

    @Override
    public String getShipCollisionSoundOverride() {
        return null;
    }

    @Override
    public void setShipCollisionSoundOverride(String shipCollisionSoundOverride) {

    }

    @Override
    public String getAsteroidCollisionSoundOverride() {
        return null;
    }

    @Override
    public void setAsteroidCollisionSoundOverride(String asteroidCollisionSoundOverride) {

    }

    @Override
    public String getParentPieceId() {
        return null;
    }

    @Override
    public void setParentPieceId(String parentPieceId) {

    }

    @Override
    public void applyEffectsAfterShipAddedToCombatEngine() {

    }

    @Override
    public float getSinceLastDamageTaken() {
        return 0;
    }

    @Override
    public boolean isNoMuzzleFlash() {
        return false;
    }

    @Override
    public void setNoMuzzleFlash(boolean noMuzzleFlash) {

    }

    @Override
    public boolean isBeingIgnored() {
        return false;
    }

    @Override
    public void setBeingIgnored(boolean beingIgnored) {

    }

    // OBFUSCATED
    public enum Command {}

    public static class ShipAIWrapper implements ShipAIPlugin {
        // UNOBFUSCATED
        public ShipAIPlugin getAI() {
            return null;
        }

        @Override
        public void setDoNotFireDelay(float amount) {

        }

        @Override
        public void forceCircumstanceEvaluation() {

        }

        @Override
        public void advance(float amount) {

        }

        @Override
        public boolean needsRefit() {
            return false;
        }

        @Override
        public ShipwideAIFlags getAIFlags() {
            return null;
        }

        @Override
        public void cancelCurrentManeuver() {

        }

        @Override
        public ShipAIConfig getConfig() {
            return null;
        }

        @Override
        public void setTargetOverride(ShipAPI target) {
            ShipAIPlugin.super.setTargetOverride(target);
        }
    }

    // OBFUSCATED
    public static class CommandWrapper {
        // OBFUSCATED
        public Command shipCommandWrapper_command;
    }
}
