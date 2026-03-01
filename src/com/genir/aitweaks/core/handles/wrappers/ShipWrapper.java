package com.genir.aitweaks.core.handles.wrappers;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.CombatListenerManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.genir.starfarer.combat.entities.Ship;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ShipWrapper exposes all methods of the Ship class without implementing the ShipAPI interface.
 * This prevents accidentally passing a ShipHandle (which inherits from ShipWrapper) to the game engine.
 */
public class ShipWrapper {
    protected final Ship ship;

    public ShipWrapper(Ship ship) {
        this.ship = ship;
    }

    public String getFleetMemberId() {
        return this.ship.getFleetMemberId();
    }

    public Vector2f getMouseTarget() {
        return this.ship.getMouseTarget();
    }

    public boolean isShuttlePod() {
        return this.ship.isShuttlePod();
    }

    public boolean isDrone() {
        return this.ship.isDrone();
    }

    public boolean isFighter() {
        return this.ship.isFighter();
    }

    public boolean isFrigate() {
        return this.ship.isFrigate();
    }

    public boolean isDestroyer() {
        return this.ship.isDestroyer();
    }

    public boolean isCruiser() {
        return this.ship.isCruiser();
    }

    public boolean isCapital() {
        return this.ship.isCapital();
    }

    public ShipAPI.HullSize getHullSize() {
        return this.ship.getHullSize();
    }

    public ShipAPI getShipTarget() {
        return (ShipAPI) this.ship.getShipTarget();
    }

    public void setShipTarget(ShipAPI p0) {
        this.ship.setShipTarget(p0);
    }

    public int getOriginalOwner() {
        return this.ship.getOriginalOwner();
    }

    public void setOriginalOwner(int p0) {
        this.ship.setOriginalOwner(p0);
    }

    public void resetOriginalOwner() {
        this.ship.resetOriginalOwner();
    }

    public MutableShipStatsAPI getMutableStats() {
        return this.ship.getMutableStats();
    }

    public boolean isHulk() {
        return this.ship.isHulk();
    }

    public List getAllWeapons() {
        return this.ship.getAllWeapons();
    }

    public ShipSystemAPI getPhaseCloak() {
        return (ShipSystemAPI) this.ship.getPhaseCloak();
    }

    public ShipSystemAPI getSystem() {
        return (ShipSystemAPI) this.ship.getSystem();
    }

    public ShipSystemAPI getTravelDrive() {
        return (ShipSystemAPI) this.ship.getTravelDrive();
    }

    public void toggleTravelDrive() {
        this.ship.toggleTravelDrive();
    }

    public void setShield(ShieldAPI.ShieldType p0, float p1, float p2, float p3) {
        this.ship.setShield(p0, p1, p2, p3);
    }

    public ShipHullSpecAPI getHullSpec() {
        return (ShipHullSpecAPI) this.ship.getHullSpec();
    }

    public ShipVariantAPI getVariant() {
        return (ShipVariantAPI) this.ship.getVariant();
    }

    public void useSystem() {
        this.ship.useSystem();
    }

    public FluxTrackerAPI getFluxTracker() {
        return (FluxTrackerAPI) this.ship.getFluxTracker();
    }

    public List getWingMembers() {
        return this.ship.getWingMembers();
    }

    public ShipAPI getWingLeader() {
        return this.ship.getWingLeader();
    }

    public boolean isWingLeader() {
        return this.ship.isWingLeader();
    }

    public FighterWingAPI getWing() {
        return (FighterWingAPI) this.ship.getWing();
    }

    public List getDeployedDrones() {
        return this.ship.getDeployedDrones();
    }

    public ShipAPI getDroneSource() {
        return this.ship.getDroneSource();
    }

    public Object getWingToken() {
        return this.ship.getWingToken();
    }

    public ArmorGridAPI getArmorGrid() {
        return (ArmorGridAPI) this.ship.getArmorGrid();
    }

    public void setRenderBounds(boolean p0) {
        this.ship.setRenderBounds(p0);
    }

    public void setCRAtDeployment(float p0) {
        this.ship.setCRAtDeployment(p0);
    }

    public float getCRAtDeployment() {
        return this.ship.getCRAtDeployment();
    }

    public float getCurrentCR() {
        return this.ship.getCurrentCR();
    }

    public void setCurrentCR(float p0) {
        this.ship.setCurrentCR(p0);
    }

    public float getWingCRAtDeployment() {
        return this.ship.getWingCRAtDeployment();
    }

    public void setHitpoints(float p0) {
        this.ship.setHitpoints(p0);
    }

    public float getTimeDeployedForCRReduction() {
        return this.ship.getTimeDeployedForCRReduction();
    }

    public float getFullTimeDeployed() {
        return this.ship.getFullTimeDeployed();
    }

    public boolean losesCRDuringCombat() {
        return this.ship.losesCRDuringCombat();
    }

    public boolean controlsLocked() {
        return this.ship.controlsLocked();
    }

    public void setControlsLocked(boolean p0) {
        this.ship.setControlsLocked(p0);
    }

    public void setShipSystemDisabled(boolean p0) {
        this.ship.setShipSystemDisabled(p0);
    }

    public Set getDisabledWeapons() {
        return this.ship.getDisabledWeapons();
    }

    public int getNumFlameouts() {
        return this.ship.getNumFlameouts();
    }

    public float getHullLevelAtDeployment() {
        return this.ship.getHullLevelAtDeployment();
    }

    public void setSprite(String p0, String p1) {
        this.ship.setSprite(p0, p1);
    }

    public void setSprite(SpriteAPI p0) {
        this.ship.setSprite(p0);
    }

    public SpriteAPI getSpriteAPI() {
        return this.ship.getSpriteAPI();
    }

    public ShipEngineControllerAPI getEngineController() {
        return (ShipEngineControllerAPI) this.ship.getEngineController();
    }

    public void giveCommand(ShipCommand p0, Object p1, int p2) {
        this.ship.giveCommand(p0, p1, p2);
    }

    public void setShipAI(ShipAIPlugin p0) {
        this.ship.setShipAI(p0);
    }

    public ShipAIPlugin getShipAI() {
        return this.ship.getShipAI();
    }

    public void resetDefaultAI() {
        this.ship.resetDefaultAI();
    }

    public void turnOnTravelDrive() {
        this.ship.turnOnTravelDrive();
    }

    public void turnOnTravelDrive(float p0) {
        this.ship.turnOnTravelDrive(p0);
    }

    public void turnOffTravelDrive() {
        this.ship.turnOffTravelDrive();
    }

    public boolean isRetreating() {
        return this.ship.isRetreating();
    }

    public void abortLanding() {
        this.ship.abortLanding();
    }

    public void beginLandingAnimation(ShipAPI p0) {
        this.ship.beginLandingAnimation(p0);
    }

    public boolean isLanding() {
        return this.ship.isLanding();
    }

    public boolean isFinishedLanding() {
        return this.ship.isFinishedLanding();
    }

    public boolean isAlive() {
        return this.ship.isAlive();
    }

    public boolean isInsideNebula() {
        return this.ship.isInsideNebula();
    }

    public void setInsideNebula(boolean p0) {
        this.ship.setInsideNebula(p0);
    }

    public boolean isAffectedByNebula() {
        return this.ship.isAffectedByNebula();
    }

    public void setAffectedByNebula(boolean p0) {
        this.ship.setAffectedByNebula(p0);
    }

    public float getDeployCost() {
        return this.ship.getDeployCost();
    }

    public void removeWeaponFromGroups(WeaponAPI p0) {
        this.ship.removeWeaponFromGroups(p0);
    }

    public void applyCriticalMalfunction(Object p0) {
        this.ship.applyCriticalMalfunction(p0);
    }

    public void applyCriticalMalfunction(Object p0, boolean p1) {
        this.ship.applyCriticalMalfunction(p0, p1);
    }

    public float getBaseCriticalMalfunctionDamage() {
        return this.ship.getBaseCriticalMalfunctionDamage();
    }

    public float getEngineFractionPermanentlyDisabled() {
        return this.ship.getEngineFractionPermanentlyDisabled();
    }

    public float getCombinedAlphaMult() {
        return this.ship.getCombinedAlphaMult();
    }

    public float getLowestHullLevelReached() {
        return this.ship.getLowestHullLevelReached();
    }

    public ShipwideAIFlags getAIFlags() {
        return this.ship.getAIFlags();
    }

    public List getWeaponGroupsCopy() {
        return this.ship.getWeaponGroupsCopy();
    }

    public boolean isHoldFire() {
        return this.ship.isHoldFire();
    }

    public boolean isHoldFireOneFrame() {
        return this.ship.isHoldFireOneFrame();
    }

    public void setHoldFireOneFrame(boolean p0) {
        this.ship.setHoldFireOneFrame(p0);
    }

    public boolean isPhased() {
        return this.ship.isPhased();
    }

    public boolean isAlly() {
        return this.ship.isAlly();
    }

    public void setWeaponGlow(float p0, Color p1, EnumSet p2) {
        this.ship.setWeaponGlow(p0, p1, p2);
    }

    public void setVentCoreColor(Color p0) {
        this.ship.setVentCoreColor(p0);
    }

    public void setVentFringeColor(Color p0) {
        this.ship.setVentFringeColor(p0);
    }

    public Color getVentCoreColor() {
        return this.ship.getVentCoreColor();
    }

    public Color getVentFringeColor() {
        return this.ship.getVentFringeColor();
    }

    public String getHullStyleId() {
        return this.ship.getHullStyleId();
    }

    public WeaponGroupAPI getWeaponGroupFor(WeaponAPI p0) {
        return this.ship.getWeaponGroupFor(p0);
    }

    public void setCopyLocation(Vector2f p0, float p1, float p2) {
        this.ship.setCopyLocation(p0, p1, p2);
    }

    public Vector2f getCopyLocation() {
        return this.ship.getCopyLocation();
    }

    public void setAlly(boolean p0) {
        this.ship.setAlly(p0);
    }

    public String getId() {
        return this.ship.getId();
    }

    public String getName() {
        return this.ship.getName();
    }

    public void setJitter(Object p0, Color p1, float p2, int p3, float p4) {
        this.ship.setJitter(p0, p1, p2, p3, p4);
    }

    public void setJitter(Object p0, Color p1, float p2, int p3, float p4, float p5) {
        this.ship.setJitter(p0, p1, p2, p3, p4, p5);
    }

    public void setJitterUnder(Object p0, Color p1, float p2, int p3, float p4) {
        this.ship.setJitterUnder(p0, p1, p2, p3, p4);
    }

    public void setJitterUnder(Object p0, Color p1, float p2, int p3, float p4, float p5) {
        this.ship.setJitterUnder(p0, p1, p2, p3, p4, p5);
    }

    public float getTimeDeployedUnderPlayerControl() {
        return this.ship.getTimeDeployedUnderPlayerControl();
    }

    public SpriteAPI getSmallTurretCover() {
        return this.ship.getSmallTurretCover();
    }

    public SpriteAPI getSmallHardpointCover() {
        return this.ship.getSmallHardpointCover();
    }

    public SpriteAPI getMediumTurretCover() {
        return this.ship.getMediumTurretCover();
    }

    public SpriteAPI getMediumHardpointCover() {
        return this.ship.getMediumHardpointCover();
    }

    public SpriteAPI getLargeTurretCover() {
        return this.ship.getLargeTurretCover();
    }

    public SpriteAPI getLargeHardpointCover() {
        return this.ship.getLargeHardpointCover();
    }

    public boolean isDefenseDisabled() {
        return this.ship.isDefenseDisabled();
    }

    public void setDefenseDisabled(boolean p0) {
        this.ship.setDefenseDisabled(p0);
    }

    public void setPhased(boolean p0) {
        this.ship.setPhased(p0);
    }

    public void setExtraAlphaMult(float p0) {
        this.ship.setExtraAlphaMult(p0);
    }

    public void setApplyExtraAlphaToEngines(boolean p0) {
        this.ship.setApplyExtraAlphaToEngines(p0);
    }

    public void setOverloadColor(Color p0) {
        this.ship.setOverloadColor(p0);
    }

    public void resetOverloadColor() {
        this.ship.resetOverloadColor();
    }

    public Color getOverloadColor() {
        return this.ship.getOverloadColor();
    }

    public boolean isRecentlyShotByPlayer() {
        return this.ship.isRecentlyShotByPlayer();
    }

    public float getMaxSpeedWithoutBoost() {
        return this.ship.getMaxSpeedWithoutBoost();
    }

    public float getHardFluxLevel() {
        return this.ship.getHardFluxLevel();
    }

    public void fadeToColor(Object p0, Color p1, float p2, float p3, float p4) {
        this.ship.fadeToColor(p0, p1, p2, p3, p4);
    }

    public boolean isShowModuleJitterUnder() {
        return this.ship.isShowModuleJitterUnder();
    }

    public void setShowModuleJitterUnder(boolean p0) {
        this.ship.setShowModuleJitterUnder(p0);
    }

    public void addAfterimage(Color p0, float p1, float p2, float p3, float p4, float p5, float p6, float p7, float p8, boolean p9, boolean p10, boolean p11) {
        this.ship.addAfterimage(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11);
    }

    public PersonAPI getCaptain() {
        return (PersonAPI) this.ship.getCaptain();
    }

    public WeaponSlotAPI getStationSlot() {
        return (WeaponSlotAPI) this.ship.getStationSlot();
    }

    public void setStationSlot(WeaponSlotAPI p0) {
        this.ship.setStationSlot(p0);
    }

    public ShipAPI getParentStation() {
        return (ShipAPI) this.ship.getParentStation();
    }

    public void setParentStation(ShipAPI p0) {
        this.ship.setParentStation(p0);
    }

    public Vector2f getFixedLocation() {
        return this.ship.getFixedLocation();
    }

    public void setFixedLocation(Vector2f p0) {
        this.ship.setFixedLocation(p0);
    }

    public boolean hasRadarRibbonIcon() {
        return this.ship.hasRadarRibbonIcon();
    }

    public boolean isTargetable() {
        return this.ship.isTargetable();
    }

    public void setStation(boolean p0) {
        this.ship.setStation(p0);
    }

    public boolean isSelectableInWarroom() {
        return this.ship.isSelectableInWarroom();
    }

    public boolean isShipWithModules() {
        return this.ship.isShipWithModules();
    }

    public void setShipWithModules(boolean p0) {
        this.ship.setShipWithModules(p0);
    }

    public List getChildModulesCopy() {
        return this.ship.getChildModulesCopy();
    }

    public boolean isPiece() {
        return this.ship.isPiece();
    }

    public BoundsAPI getVisualBounds() {
        return (BoundsAPI) this.ship.getVisualBounds();
    }

    public Vector2f getRenderOffset() {
        return this.ship.getRenderOffset();
    }

    public ShipAPI splitShip() {
        return this.ship.splitShip();
    }

    public int getNumFighterBays() {
        return this.ship.getNumFighterBays();
    }

    public boolean isPullBackFighters() {
        return this.ship.isPullBackFighters();
    }

    public void setPullBackFighters(boolean p0) {
        this.ship.setPullBackFighters(p0);
    }

    public boolean hasLaunchBays() {
        return this.ship.hasLaunchBays();
    }

    public List getLaunchBaysCopy() {
        return this.ship.getLaunchBaysCopy();
    }

    public float getFighterTimeBeforeRefit() {
        return this.ship.getFighterTimeBeforeRefit();
    }

    public void setFighterTimeBeforeRefit(float p0) {
        this.ship.setFighterTimeBeforeRefit(p0);
    }

    public List getAllWings() {
        return this.ship.getAllWings();
    }

    public float getSharedFighterReplacementRate() {
        return this.ship.getSharedFighterReplacementRate();
    }

    public boolean areSignificantEnemiesInRange() {
        return this.ship.areSignificantEnemiesInRange();
    }

    public List getUsableWeapons() {
        return this.ship.getUsableWeapons();
    }

    public Vector2f getModuleOffset() {
        return this.ship.getModuleOffset();
    }

    public float getMassWithModules() {
        return this.ship.getMassWithModules();
    }

    public PersonAPI getOriginalCaptain() {
        return (PersonAPI) this.ship.getOriginalCaptain();
    }

    public boolean isRenderEngines() {
        return this.ship.isRenderEngines();
    }

    public void setRenderEngines(boolean p0) {
        this.ship.setRenderEngines(p0);
    }

    public WeaponGroupAPI getSelectedGroupAPI() {
        return this.ship.getSelectedGroupAPI();
    }

    public void setHullSize(ShipAPI.HullSize p0) {
        this.ship.setHullSize(p0);
    }

    public void ensureClonedStationSlotSpec() {
        this.ship.ensureClonedStationSlotSpec();
    }

    public void setMaxHitpoints(float p0) {
        this.ship.setMaxHitpoints(p0);
    }

    public void setDHullOverlay(String p0) {
        this.ship.setDHullOverlay(p0);
    }

    public boolean isStation() {
        return this.ship.isStation();
    }

    public boolean isStationModule() {
        return this.ship.isStationModule();
    }

    public boolean areAnyEnemiesInRange() {
        return this.ship.areAnyEnemiesInRange();
    }

    public void blockCommandForOneFrame(ShipCommand p0) {
        this.ship.blockCommandForOneFrame(p0);
    }

    public float getMaxTurnRate() {
        return this.ship.getMaxTurnRate();
    }

    public float getTurnAcceleration() {
        return this.ship.getTurnAcceleration();
    }

    public float getTurnDeceleration() {
        return this.ship.getTurnDeceleration();
    }

    public float getDeceleration() {
        return this.ship.getDeceleration();
    }

    public float getAcceleration() {
        return this.ship.getAcceleration();
    }

    public float getMaxSpeed() {
        return this.ship.getMaxSpeed();
    }

    public float getFluxLevel() {
        return this.ship.getFluxLevel();
    }

    public float getCurrFlux() {
        return this.ship.getCurrFlux();
    }

    public float getMaxFlux() {
        return this.ship.getMaxFlux();
    }

    public float getMinFluxLevel() {
        return this.ship.getMinFluxLevel();
    }

    public float getMinFlux() {
        return this.ship.getMinFlux();
    }

    public void setLightDHullOverlay() {
        this.ship.setLightDHullOverlay();
    }

    public void setMediumDHullOverlay() {
        this.ship.setMediumDHullOverlay();
    }

    public void setHeavyDHullOverlay() {
        this.ship.setHeavyDHullOverlay();
    }

    public boolean isJitterShields() {
        return this.ship.isJitterShields();
    }

    public void setJitterShields(boolean p0) {
        this.ship.setJitterShields(p0);
    }

    public boolean isInvalidTransferCommandTarget() {
        return this.ship.isInvalidTransferCommandTarget();
    }

    public void setInvalidTransferCommandTarget(boolean p0) {
        this.ship.setInvalidTransferCommandTarget(p0);
    }

    public void clearDamageDecals() {
        this.ship.clearDamageDecals();
    }

    public void syncWithArmorGridState() {
        this.ship.syncWithArmorGridState();
    }

    public void syncWeaponDecalsWithArmorDamage() {
        this.ship.syncWeaponDecalsWithArmorDamage();
    }

    public boolean isDirectRetreat() {
        return this.ship.isDirectRetreat();
    }

    public void setRetreating(boolean p0, boolean p1) {
        this.ship.setRetreating(p0, p1);
    }

    public boolean isLiftingOff() {
        return this.ship.isLiftingOff();
    }

    public void setVariantForHullmodCheckOnly(ShipVariantAPI p0) {
        this.ship.setVariantForHullmodCheckOnly(p0);
    }

    public Vector2f getShieldCenterEvenIfNoShield() {
        return this.ship.getShieldCenterEvenIfNoShield();
    }

    public float getShieldRadiusEvenIfNoShield() {
        return this.ship.getShieldRadiusEvenIfNoShield();
    }

    public FleetMemberAPI getFleetMember() {
        return this.ship.getFleetMember();
    }

    public Vector2f getShieldTarget() {
        return this.ship.getShieldTarget();
    }

    public void setShieldTargetOverride(float p0, float p1) {
        this.ship.setShieldTargetOverride(p0, p1);
    }

    public CombatListenerManagerAPI getListenerManager() {
        return this.ship.getListenerManager();
    }

    public void addListener(Object p0) {
        this.ship.addListener(p0);
    }

    public void removeListener(Object p0) {
        this.ship.removeListener(p0);
    }

    public void removeListenerOfClass(Class p0) {
        this.ship.removeListenerOfClass(p0);
    }

    public boolean hasListener(Object p0) {
        return this.ship.hasListener(p0);
    }

    public boolean hasListenerOfClass(Class p0) {
        return this.ship.hasListenerOfClass(p0);
    }

    public List getListeners(Class p0) {
        return this.ship.getListeners(p0);
    }

    public Object getParamAboutToApplyDamage() {
        return this.ship.getParamAboutToApplyDamage();
    }

    public void setParamAboutToApplyDamage(Object p0) {
        this.ship.setParamAboutToApplyDamage(p0);
    }

    public float getFluxBasedEnergyWeaponDamageMultiplier() {
        return this.ship.getFluxBasedEnergyWeaponDamageMultiplier();
    }

    public void setName(String p0) {
        this.ship.setName(p0);
    }

    public void setHulk(boolean p0) {
        this.ship.setHulk(p0);
    }

    public void setCaptain(PersonAPI p0) {
        this.ship.setCaptain(p0);
    }

    public float getShipExplosionRadius() {
        return this.ship.getShipExplosionRadius();
    }

    public void setCircularJitter(boolean p0) {
        this.ship.setCircularJitter(p0);
    }

    public float getExtraAlphaMult() {
        return this.ship.getExtraAlphaMult();
    }

    public void setAlphaMult(float p0) {
        this.ship.setAlphaMult(p0);
    }

    public float getAlphaMult() {
        return this.ship.getAlphaMult();
    }

    public void setAnimatedLaunch() {
        this.ship.setAnimatedLaunch();
    }

    public void setLaunchingShip(ShipAPI p0) {
        this.ship.setLaunchingShip(p0);
    }

    public boolean isNonCombat(boolean p0) {
        return this.ship.isNonCombat(p0);
    }

    public float findBestArmorInArc(float p0, float p1) {
        return this.ship.findBestArmorInArc(p0, p1);
    }

    public float getAverageArmorInSlice(float p0, float p1) {
        return this.ship.getAverageArmorInSlice(p0, p1);
    }

    public void setHoldFire(boolean p0) {
        this.ship.setHoldFire(p0);
    }

    public void cloneVariant() {
        this.ship.cloneVariant();
    }

    public void setTimeDeployed(float p0) {
        this.ship.setTimeDeployed(p0);
    }

    public void setFluxVentTextureSheet(String p0) {
        this.ship.setFluxVentTextureSheet(p0);
    }

    public String getFluxVentTextureSheet() {
        return this.ship.getFluxVentTextureSheet();
    }

    public float getAimAccuracy() {
        return this.ship.getAimAccuracy();
    }

    public float getForceCarrierTargetTime() {
        return this.ship.getForceCarrierTargetTime();
    }

    public void setForceCarrierTargetTime(float p0) {
        this.ship.setForceCarrierTargetTime(p0);
    }

    public float getForceCarrierPullBackTime() {
        return this.ship.getForceCarrierPullBackTime();
    }

    public void setForceCarrierPullBackTime(float p0) {
        this.ship.setForceCarrierPullBackTime(p0);
    }

    public ShipAPI getForceCarrierTarget() {
        return this.ship.getForceCarrierTarget();
    }

    public void setForceCarrierTarget(ShipAPI p0) {
        this.ship.setForceCarrierTarget(p0);
    }

    public void setWing(FighterWingAPI p0) {
        this.ship.setWing(p0);
    }

    public float getExplosionScale() {
        return this.ship.getExplosionScale();
    }

    public void setExplosionScale(float p0) {
        this.ship.setExplosionScale(p0);
    }

    public Color getExplosionFlashColorOverride() {
        return this.ship.getExplosionFlashColorOverride();
    }

    public void setExplosionFlashColorOverride(Color p0) {
        this.ship.setExplosionFlashColorOverride(p0);
    }

    public Vector2f getExplosionVelocityOverride() {
        return this.ship.getExplosionVelocityOverride();
    }

    public void setExplosionVelocityOverride(Vector2f p0) {
        this.ship.setExplosionVelocityOverride(p0);
    }

    public void setNextHitHullDamageThresholdMult(float p0, float p1) {
        this.ship.setNextHitHullDamageThresholdMult(p0, p1);
    }

    public boolean isEngineBoostActive() {
        return this.ship.isEngineBoostActive();
    }

    public void makeLookDisabled() {
        this.ship.makeLookDisabled();
    }

    public void setExtraAlphaMult2(float p0) {
        this.ship.setExtraAlphaMult2(p0);
    }

    public float getExtraAlphaMult2() {
        return this.ship.getExtraAlphaMult2();
    }

    public void setDrone(boolean p0) {
        this.ship.setDrone(p0);
    }

    public CombatEngineLayers getLayer() {
        return this.ship.getLayer();
    }

    public void setLayer(CombatEngineLayers p0) {
        this.ship.setLayer(p0);
    }

    public boolean isForceHideFFOverlay() {
        return this.ship.isForceHideFFOverlay();
    }

    public void setForceHideFFOverlay(boolean p0) {
        this.ship.setForceHideFFOverlay(p0);
    }

    public Set getTags() {
        return this.ship.getTags();
    }

    public void addTag(String p0) {
        this.ship.addTag(p0);
    }

    public boolean hasTag(String p0) {
        return this.ship.hasTag(p0);
    }

    public float getPeakTimeRemaining() {
        return this.ship.getPeakTimeRemaining();
    }

    public EnumSet getActiveLayers() {
        return this.ship.getActiveLayers();
    }

    public boolean isShipSystemDisabled() {
        return this.ship.isShipSystemDisabled();
    }

    public boolean isDoNotFlareEnginesWhenStrafingOrDecelerating() {
        return this.ship.isDoNotFlareEnginesWhenStrafingOrDecelerating();
    }

    public void setDoNotFlareEnginesWhenStrafingOrDecelerating(boolean p0) {
        this.ship.setDoNotFlareEnginesWhenStrafingOrDecelerating(p0);
    }

    public PersonAPI getFleetCommander() {
        return (PersonAPI) this.ship.getFleetCommander();
    }

    public boolean isDoNotRender() {
        return this.ship.isDoNotRender();
    }

    public void setDoNotRender(boolean p0) {
        this.ship.setDoNotRender(p0);
    }

    public float getHulkChanceOverride() {
        return this.ship.getHulkChanceOverride();
    }

    public void setHulkChanceOverride(float p0) {
        this.ship.setHulkChanceOverride(p0);
    }

    public float getImpactVolumeMult() {
        return this.ship.getImpactVolumeMult();
    }

    public void setImpactVolumeMult(float p0) {
        this.ship.setImpactVolumeMult(p0);
    }

    public Vector2f checkCollisionVsRay(Vector2f p0, Vector2f p1) {
        return this.ship.checkCollisionVsRay(p0, p1);
    }

    public boolean isPointInBounds(Vector2f p0) {
        return this.ship.isPointInBounds(p0);
    }

    public boolean isSpawnDebris() {
        return this.ship.isSpawnDebris();
    }

    public void setSpawnDebris(boolean p0) {
        this.ship.setSpawnDebris(p0);
    }

    public float getDHullOverlayAngleOffset() {
        return this.ship.getDHullOverlayAngleOffset();
    }

    public void setDHullOverlayAngleOffset(float p0) {
        this.ship.setDHullOverlayAngleOffset(p0);
    }

    public float getExtraOverlayAngleOffset() {
        return this.ship.getExtraOverlayAngleOffset();
    }

    public void setExtraOverlayAngleOffset(float p0) {
        this.ship.setExtraOverlayAngleOffset(p0);
    }

    public void setExtraOverlay(String p0) {
        this.ship.setExtraOverlay(p0);
    }

    public float getExtraOverlayShadowOpacity() {
        return this.ship.getExtraOverlayShadowOpacity();
    }

    public void setExtraOverlayShadowOpacity(float p0) {
        this.ship.setExtraOverlayShadowOpacity(p0);
    }

    public boolean isExtraOverlayMatchHullColor() {
        return this.ship.isExtraOverlayMatchHullColor();
    }

    public void setExtraOverlayMatchHullColor(boolean p0) {
        this.ship.setExtraOverlayMatchHullColor(p0);
    }

    public void resetSelectedGroup() {
        this.ship.resetSelectedGroup();
    }

    public void removeTag(String p0) {
        this.ship.removeTag(p0);
    }

    public boolean isSkipNextDamagedExplosion() {
        return this.ship.isSkipNextDamagedExplosion();
    }

    public void setSkipNextDamagedExplosion(boolean p0) {
        this.ship.setSkipNextDamagedExplosion(p0);
    }

    public void setDefaultAI(FleetMemberAPI p0) {
        this.ship.setDefaultAI(p0);
    }

    public boolean isNoDamagedExplosions() {
        return this.ship.isNoDamagedExplosions();
    }

    public void setNoDamagedExplosions(boolean p0) {
        this.ship.setNoDamagedExplosions(p0);
    }

    public boolean isDoNotRenderSprite() {
        return this.ship.isDoNotRenderSprite();
    }

    public void setDoNotRenderSprite(boolean p0) {
        this.ship.setDoNotRenderSprite(p0);
    }

    public boolean isDoNotRenderShield() {
        return this.ship.isDoNotRenderShield();
    }

    public void setDoNotRenderShield(boolean p0) {
        this.ship.setDoNotRenderShield(p0);
    }

    public boolean isDoNotRenderWeapons() {
        return this.ship.isDoNotRenderWeapons();
    }

    public void setDoNotRenderWeapons(boolean p0) {
        this.ship.setDoNotRenderWeapons(p0);
    }

    public void setDoNotRenderVentingAnimation(boolean p0) {
        this.ship.setDoNotRenderVentingAnimation(p0);
    }

    public boolean isDoNotRenderVentingAnimation() {
        return this.ship.isDoNotRenderVentingAnimation();
    }

    public String getShipCollisionSoundOverride() {
        return this.ship.getShipCollisionSoundOverride();
    }

    public void setShipCollisionSoundOverride(String p0) {
        this.ship.setShipCollisionSoundOverride(p0);
    }

    public String getAsteroidCollisionSoundOverride() {
        return this.ship.getAsteroidCollisionSoundOverride();
    }

    public void setAsteroidCollisionSoundOverride(String p0) {
        this.ship.setAsteroidCollisionSoundOverride(p0);
    }

    public String getParentPieceId() {
        return this.ship.getParentPieceId();
    }

    public void setParentPieceId(String p0) {
        this.ship.setParentPieceId(p0);
    }

    public void applyEffectsAfterShipAddedToCombatEngine() {
        this.ship.applyEffectsAfterShipAddedToCombatEngine();
    }

    public float getSinceLastDamageTaken() {
        return this.ship.getSinceLastDamageTaken();
    }

    public boolean isNoMuzzleFlash() {
        return this.ship.isNoMuzzleFlash();
    }

    public void setNoMuzzleFlash(boolean p0) {
        this.ship.setNoMuzzleFlash(p0);
    }

    public boolean isBeingIgnored() {
        return this.ship.isBeingIgnored();
    }

    public void setBeingIgnored(boolean p0) {
        this.ship.setBeingIgnored(p0);
    }

    public void setLowestHullLevelReached(float p0) {
        this.ship.setLowestHullLevelReached(p0);
    }

    public void setFleetMember(FleetMemberAPI p0) {
        this.ship.setFleetMember(p0);
    }

    public Vector2f getLocation() {
        return this.ship.getLocation();
    }

    public Vector2f getVelocity() {
        return this.ship.getVelocity();
    }

    public float getFacing() {
        return this.ship.getFacing();
    }

    public void setFacing(float p0) {
        this.ship.setFacing(p0);
    }

    public float getAngularVelocity() {
        return this.ship.getAngularVelocity();
    }

    public void setAngularVelocity(float p0) {
        this.ship.setAngularVelocity(p0);
    }

    public int getOwner() {
        return this.ship.getOwner();
    }

    public void setOwner(int p0) {
        this.ship.setOwner(p0);
    }

    public float getCollisionRadius() {
        return this.ship.getCollisionRadius();
    }

    public CollisionClass getCollisionClass() {
        return this.ship.getCollisionClass();
    }

    public void setCollisionClass(CollisionClass p0) {
        this.ship.setCollisionClass(p0);
    }

    public float getMass() {
        return this.ship.getMass();
    }

    public void setMass(float p0) {
        this.ship.setMass(p0);
    }

    public BoundsAPI getExactBounds() {
        return (BoundsAPI) this.ship.getExactBounds();
    }

    public ShieldAPI getShield() {
        return (ShieldAPI) this.ship.getShield();
    }

    public float getHullLevel() {
        return this.ship.getHullLevel();
    }

    public float getHitpoints() {
        return this.ship.getHitpoints();
    }

    public float getMaxHitpoints() {
        return this.ship.getMaxHitpoints();
    }

    public void setCollisionRadius(float p0) {
        this.ship.setCollisionRadius(p0);
    }

    public Object getAI() {
        return this.ship.getAI();
    }

    public boolean isExpired() {
        return this.ship.isExpired();
    }

    public void setCustomData(String p0, Object p1) {
        this.ship.setCustomData(p0, p1);
    }

    public void removeCustomData(String p0) {
        this.ship.removeCustomData(p0);
    }

    public Map getCustomData() {
        return this.ship.getCustomData();
    }

    public boolean wasRemoved() {
        return this.ship.wasRemoved();
    }
}
