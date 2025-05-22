package com.genir.aitweaks.core.handles.wrappers;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.MuzzleFlashSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.genir.starfarer.combat.entities.ship.trackers.AimTracker;
import com.genir.starfarer.combat.systems.Weapon;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

/**
 * WeaponWrapper exposes all methods of the Weapon class without implementing the WeaponAPI interface.
 * This prevents accidentally passing a WeaponHandle (which inherits from WeaponWrapper) to the game engine.
 */
public class WeaponWrapper {
    protected final Weapon weapon;

    public WeaponWrapper(Weapon weapon) {
        this.weapon = weapon;
    }

    public String getId() {
        return this.weapon.getId();
    }

    public WeaponAPI.WeaponType getType() {
        return this.weapon.getType();
    }

    public WeaponAPI.WeaponSize getSize() {
        return this.weapon.getSize();
    }

    public void setPD(boolean p0) {
        this.weapon.setPD(p0);
    }

    public float distanceFromArc(Vector2f p0) {
        return this.weapon.distanceFromArc(p0);
    }

    public boolean isAlwaysFire() {
        return this.weapon.isAlwaysFire();
    }

    public float getCurrSpread() {
        return this.weapon.getCurrSpread();
    }

    public float getCurrAngle() {
        return this.weapon.getCurrAngle();
    }

    public void setCurrAngle(float p0) {
        this.weapon.setCurrAngle(p0);
    }

    public float getArcFacing() {
        return this.weapon.getArcFacing();
    }

    public float getArc() {
        return this.weapon.getArc();
    }

    public float getRange() {
        return this.weapon.getRange();
    }

    public float getDisplayArcRadius() {
        return this.weapon.getDisplayArcRadius();
    }

    public float getChargeLevel() {
        return this.weapon.getChargeLevel();
    }

    public float getTurnRate() {
        return this.weapon.getTurnRate();
    }

    public float getProjectileSpeed() {
        return this.weapon.getProjectileSpeed();
    }

    public String getDisplayName() {
        return this.weapon.getDisplayName();
    }

    public int getAmmo() {
        return this.weapon.getAmmo();
    }

    public void setAmmo(int p0) {
        this.weapon.setAmmo(p0);
    }

    public int getMaxAmmo() {
        return this.weapon.getMaxAmmo();
    }

    public void setMaxAmmo(int p0) {
        this.weapon.setMaxAmmo(p0);
    }

    public void resetAmmo() {
        this.weapon.resetAmmo();
    }

    public float getCooldownRemaining() {
        return this.weapon.getCooldownRemaining();
    }

    public float getCooldown() {
        return this.weapon.getCooldown();
    }

    public void setRemainingCooldownTo(float p0) {
        this.weapon.setRemainingCooldownTo(p0);
    }

    public boolean isBeam() {
        return this.weapon.isBeam();
    }

    public boolean isBurstBeam() {
        return this.weapon.isBurstBeam();
    }

    public boolean isPulse() {
        return this.weapon.isPulse();
    }

    public boolean requiresFullCharge() {
        return this.weapon.requiresFullCharge();
    }

    public Vector2f getLocation() {
        return this.weapon.getLocation();
    }

    public boolean isFiring() {
        return this.weapon.isFiring();
    }

    public boolean usesAmmo() {
        return this.weapon.usesAmmo();
    }

    public boolean usesEnergy() {
        return this.weapon.usesEnergy();
    }

    public boolean hasAIHint(WeaponAPI.AIHints p0) {
        return this.weapon.hasAIHint(p0);
    }

    public CollisionClass getProjectileCollisionClass() {
        return this.weapon.getProjectileCollisionClass();
    }

    public void beginSelectionFlash() {
        this.weapon.beginSelectionFlash();
    }

    public float getFluxCostToFire() {
        return this.weapon.getFluxCostToFire();
    }

    public float getMaxHealth() {
        return this.weapon.getMaxHealth();
    }

    public float getCurrHealth() {
        return this.weapon.getCurrHealth();
    }

    public void setCurrHealth(float p0) {
        this.weapon.setCurrHealth(p0);
    }

    public boolean isDisabled() {
        return this.weapon.isDisabled();
    }

    public float getDisabledDuration() {
        return this.weapon.getDisabledDuration();
    }

    public boolean isPermanentlyDisabled() {
        return this.weapon.isPermanentlyDisabled();
    }

    public DamageType getDamageType() {
        return this.weapon.getDamageType();
    }

    public ShipAPI getShip() {
        return this.weapon.getShip();
    }

    public WeaponAPI.DerivedWeaponStatsAPI getDerivedStats() {
        return this.weapon.getDerivedStats();
    }

    public AnimationAPI getAnimation() {
        return this.weapon.getAnimation();
    }

    public SpriteAPI getSprite() {
        return this.weapon.getSprite();
    }

    public SpriteAPI getUnderSpriteAPI() {
        return this.weapon.getUnderSpriteAPI();
    }

    public SpriteAPI getBarrelSpriteAPI() {
        return this.weapon.getBarrelSpriteAPI();
    }

    public void renderBarrel(SpriteAPI p0, Vector2f p1, float p2) {
        this.weapon.renderBarrel(p0, p1, p2);
    }

    public boolean isRenderBarrelBelow() {
        return this.weapon.isRenderBarrelBelow();
    }

    public void disable() {
        this.weapon.disable();
    }

    public void disable(boolean p0) {
        this.weapon.disable(p0);
    }

    public void repair() {
        this.weapon.repair();
    }

    public WeaponSpecAPI getSpec() {
        return this.weapon.getSpec();
    }

    public WeaponSlotAPI getSlot() {
        return this.weapon.getSlot();
    }

    public EveryFrameWeaponEffectPlugin getEffectPlugin() {
        return this.weapon.getEffectPlugin();
    }

    public List getMissileRenderData() {
        return this.weapon.getMissileRenderData();
    }

    public DamageAPI getDamage() {
        return this.weapon.getDamage();
    }

    public float getProjectileFadeRange() {
        return this.weapon.getProjectileFadeRange();
    }

    public boolean isDecorative() {
        return this.weapon.isDecorative();
    }

    public void ensureClonedSpec() {
        this.weapon.ensureClonedSpec();
    }

    public float getAmmoPerSecond() {
        return this.weapon.getAmmoPerSecond();
    }

    public void setPDAlso(boolean p0) {
        this.weapon.setPDAlso(p0);
    }

    public MuzzleFlashSpec getMuzzleFlashSpec() {
        return this.weapon.getMuzzleFlashSpec();
    }

    public List getBeams() {
        return this.weapon.getBeams();
    }

    public Vector2f getFirePoint(int p0) {
        return this.weapon.getFirePoint(p0);
    }

    public void setTurnRateOverride(Float p0) {
        this.weapon.setTurnRateOverride(p0);
    }

    public SpriteAPI getGlowSpriteAPI() {
        return this.weapon.getGlowSpriteAPI();
    }

    public AmmoTrackerAPI getAmmoTracker() {
        return this.weapon.getAmmoTracker();
    }

    public void setFacing(float p0) {
        this.weapon.setFacing(p0);
    }

    public void updateBeamFromPoints() {
        this.weapon.updateBeamFromPoints();
    }

    public boolean isKeepBeamTargetWhileChargingDown() {
        return this.weapon.isKeepBeamTargetWhileChargingDown();
    }

    public void setKeepBeamTargetWhileChargingDown(boolean p0) {
        this.weapon.setKeepBeamTargetWhileChargingDown(p0);
    }

    public void setScaleBeamGlowBasedOnDamageEffectiveness(boolean p0) {
        this.weapon.setScaleBeamGlowBasedOnDamageEffectiveness(p0);
    }

    public void setForceFireOneFrame(boolean p0) {
        this.weapon.setForceFireOneFrame(p0);
    }

    public void setGlowAmount(float p0, Color p1) {
        this.weapon.setGlowAmount(p0, p1);
    }

    public void setSuspendAutomaticTurning(boolean p0) {
        this.weapon.setSuspendAutomaticTurning(p0);
    }

    public float getBurstFireTimeRemaining() {
        return this.weapon.getBurstFireTimeRemaining();
    }

    public Vector2f getRenderOffsetForDecorativeBeamWeaponsOnly() {
        return this.weapon.getRenderOffsetForDecorativeBeamWeaponsOnly();
    }

    public void setRenderOffsetForDecorativeBeamWeaponsOnly(Vector2f p0) {
        this.weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(p0);
    }

    public float getRefireDelay() {
        return this.weapon.getRefireDelay();
    }

    public void setRefireDelay(float p0) {
        this.weapon.setRefireDelay(p0);
    }

    public void forceShowBeamGlow() {
        this.weapon.forceShowBeamGlow();
    }

    public boolean isInBurst() {
        return this.weapon.isInBurst();
    }

    public WeaponSpecAPI getOriginalSpec() {
        return this.weapon.getOriginalSpec();
    }

    public void setWeaponGlowWidthMult(float p0) {
        this.weapon.setWeaponGlowWidthMult(p0);
    }

    public void setWeaponGlowHeightMult(float p0) {
        this.weapon.setWeaponGlowHeightMult(p0);
    }

    public void stopFiring() {
        this.weapon.stopFiring();
    }

    public boolean isForceDisabled() {
        return this.weapon.isForceDisabled();
    }

    public void setForceDisabled(boolean p0) {
        this.weapon.setForceDisabled(p0);
    }

    public Object getCustom() {
        return this.weapon.getCustom();
    }

    public void setCustom(Object p0) {
        this.weapon.setCustom(p0);
    }

    public boolean isForceNoFireOneFrame() {
        return this.weapon.isForceNoFireOneFrame();
    }

    public void setForceNoFireOneFrame(boolean p0) {
        this.weapon.setForceNoFireOneFrame(p0);
    }

    public AimTracker getAimTracker() {
        return this.weapon.getAimTracker();
    }
}
