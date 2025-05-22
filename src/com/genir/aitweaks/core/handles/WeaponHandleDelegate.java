package com.genir.aitweaks.core.handles;

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

public class WeaponHandleDelegate {
    private final Weapon $$delegate_0;

    public WeaponHandleDelegate(Weapon weapon) {
        this.$$delegate_0 = weapon;
    }

    public String getId() {
        return this.$$delegate_0.getId();
    }

    public WeaponAPI.WeaponType getType() {
        return this.$$delegate_0.getType();
    }

    public WeaponAPI.WeaponSize getSize() {
        return this.$$delegate_0.getSize();
    }

    public void setPD(boolean p0) {
        this.$$delegate_0.setPD(p0);
    }

    public float distanceFromArc(Vector2f p0) {
        return this.$$delegate_0.distanceFromArc(p0);
    }

    public boolean isAlwaysFire() {
        return this.$$delegate_0.isAlwaysFire();
    }

    public float getCurrSpread() {
        return this.$$delegate_0.getCurrSpread();
    }

    public float getCurrAngle() {
        return this.$$delegate_0.getCurrAngle();
    }

    public float getArcFacing() {
        return this.$$delegate_0.getArcFacing();
    }

    public float getArc() {
        return this.$$delegate_0.getArc();
    }

    public void setCurrAngle(float p0) {
        this.$$delegate_0.setCurrAngle(p0);
    }

    public float getRange() {
        return this.$$delegate_0.getRange();
    }

    public float getDisplayArcRadius() {
        return this.$$delegate_0.getDisplayArcRadius();
    }

    public float getChargeLevel() {
        return this.$$delegate_0.getChargeLevel();
    }

    public float getTurnRate() {
        return this.$$delegate_0.getTurnRate();
    }

    public float getProjectileSpeed() {
        return this.$$delegate_0.getProjectileSpeed();
    }

    public String getDisplayName() {
        return this.$$delegate_0.getDisplayName();
    }

    public int getAmmo() {
        return this.$$delegate_0.getAmmo();
    }

    public int getMaxAmmo() {
        return this.$$delegate_0.getMaxAmmo();
    }

    public void setMaxAmmo(int p0) {
        this.$$delegate_0.setMaxAmmo(p0);
    }

    public void resetAmmo() {
        this.$$delegate_0.resetAmmo();
    }

    public float getCooldownRemaining() {
        return this.$$delegate_0.getCooldownRemaining();
    }

    public float getCooldown() {
        return this.$$delegate_0.getCooldown();
    }

    public void setRemainingCooldownTo(float p0) {
        this.$$delegate_0.setRemainingCooldownTo(p0);
    }

    public boolean isBeam() {
        return this.$$delegate_0.isBeam();
    }

    public boolean isBurstBeam() {
        return this.$$delegate_0.isBurstBeam();
    }

    public boolean isPulse() {
        return this.$$delegate_0.isPulse();
    }

    public boolean requiresFullCharge() {
        return this.$$delegate_0.requiresFullCharge();
    }

    public Vector2f getLocation() {
        return this.$$delegate_0.getLocation();
    }

    public boolean isFiring() {
        return this.$$delegate_0.isFiring();
    }

    public boolean usesAmmo() {
        return this.$$delegate_0.usesAmmo();
    }

    public boolean usesEnergy() {
        return this.$$delegate_0.usesEnergy();
    }

    public boolean hasAIHint(WeaponAPI.AIHints p0) {
        return this.$$delegate_0.hasAIHint(p0);
    }

    public CollisionClass getProjectileCollisionClass() {
        return this.$$delegate_0.getProjectileCollisionClass();
    }

    public void beginSelectionFlash() {
        this.$$delegate_0.beginSelectionFlash();
    }

    public float getFluxCostToFire() {
        return this.$$delegate_0.getFluxCostToFire();
    }

    public float getMaxHealth() {
        return this.$$delegate_0.getMaxHealth();
    }

    public float getCurrHealth() {
        return this.$$delegate_0.getCurrHealth();
    }

    public boolean isDisabled() {
        return this.$$delegate_0.isDisabled();
    }

    public float getDisabledDuration() {
        return this.$$delegate_0.getDisabledDuration();
    }

    public boolean isPermanentlyDisabled() {
        return this.$$delegate_0.isPermanentlyDisabled();
    }

    public DamageType getDamageType() {
        return this.$$delegate_0.getDamageType();
    }

    public ShipAPI getShip() {
        return this.$$delegate_0.getShip();
    }

    public WeaponAPI.DerivedWeaponStatsAPI getDerivedStats() {
        return this.$$delegate_0.getDerivedStats();
    }

    public void setAmmo(int p0) {
        this.$$delegate_0.setAmmo(p0);
    }

    public AnimationAPI getAnimation() {
        return this.$$delegate_0.getAnimation();
    }

    public SpriteAPI getSprite() {
        return this.$$delegate_0.getSprite();
    }

    public SpriteAPI getUnderSpriteAPI() {
        return this.$$delegate_0.getUnderSpriteAPI();
    }

    public SpriteAPI getBarrelSpriteAPI() {
        return this.$$delegate_0.getBarrelSpriteAPI();
    }

    public void renderBarrel(SpriteAPI p0, Vector2f p1, float p2) {
        this.$$delegate_0.renderBarrel(p0, p1, p2);
    }

    public boolean isRenderBarrelBelow() {
        return this.$$delegate_0.isRenderBarrelBelow();
    }

    public void disable() {
        this.$$delegate_0.disable();
    }

    public void disable(boolean p0) {
        this.$$delegate_0.disable(p0);
    }

    public void repair() {
        this.$$delegate_0.repair();
    }

    public WeaponSpecAPI getSpec() {
        return this.$$delegate_0.getSpec();
    }

    public WeaponSlotAPI getSlot() {
        return this.$$delegate_0.getSlot();
    }

    public EveryFrameWeaponEffectPlugin getEffectPlugin() {
        return this.$$delegate_0.getEffectPlugin();
    }

    public List getMissileRenderData() {
        return this.$$delegate_0.getMissileRenderData();
    }

    public DamageAPI getDamage() {
        return this.$$delegate_0.getDamage();
    }

    public float getProjectileFadeRange() {
        return this.$$delegate_0.getProjectileFadeRange();
    }

    public boolean isDecorative() {
        return this.$$delegate_0.isDecorative();
    }

    public void ensureClonedSpec() {
        this.$$delegate_0.ensureClonedSpec();
    }

    public float getAmmoPerSecond() {
        return this.$$delegate_0.getAmmoPerSecond();
    }

    public void setPDAlso(boolean p0) {
        this.$$delegate_0.setPDAlso(p0);
    }

    public void setCurrHealth(float p0) {
        this.$$delegate_0.setCurrHealth(p0);
    }

    public MuzzleFlashSpec getMuzzleFlashSpec() {
        return this.$$delegate_0.getMuzzleFlashSpec();
    }

    public List getBeams() {
        return this.$$delegate_0.getBeams();
    }

    public Vector2f getFirePoint(int p0) {
        return this.$$delegate_0.getFirePoint(p0);
    }

    public void setTurnRateOverride(Float p0) {
        this.$$delegate_0.setTurnRateOverride(p0);
    }

    public SpriteAPI getGlowSpriteAPI() {
        return this.$$delegate_0.getGlowSpriteAPI();
    }

    public AmmoTrackerAPI getAmmoTracker() {
        return this.$$delegate_0.getAmmoTracker();
    }

    public void setRefireDelay(float p0) {
        this.$$delegate_0.setRefireDelay(p0);
    }

    public void setFacing(float p0) {
        this.$$delegate_0.setFacing(p0);
    }

    public void updateBeamFromPoints() {
        this.$$delegate_0.updateBeamFromPoints();
    }

    public boolean isKeepBeamTargetWhileChargingDown() {
        return this.$$delegate_0.isKeepBeamTargetWhileChargingDown();
    }

    public void setKeepBeamTargetWhileChargingDown(boolean p0) {
        this.$$delegate_0.setKeepBeamTargetWhileChargingDown(p0);
    }

    public void setScaleBeamGlowBasedOnDamageEffectiveness(boolean p0) {
        this.$$delegate_0.setScaleBeamGlowBasedOnDamageEffectiveness(p0);
    }

    public void setForceFireOneFrame(boolean p0) {
        this.$$delegate_0.setForceFireOneFrame(p0);
    }

    public void setGlowAmount(float p0, Color p1) {
        this.$$delegate_0.setGlowAmount(p0, p1);
    }

    public void setForceNoFireOneFrame(boolean p0) {
        this.$$delegate_0.setForceNoFireOneFrame(p0);
    }

    public void setSuspendAutomaticTurning(boolean p0) {
        this.$$delegate_0.setSuspendAutomaticTurning(p0);
    }

    public float getBurstFireTimeRemaining() {
        return this.$$delegate_0.getBurstFireTimeRemaining();
    }

    public Vector2f getRenderOffsetForDecorativeBeamWeaponsOnly() {
        return this.$$delegate_0.getRenderOffsetForDecorativeBeamWeaponsOnly();
    }

    public void setRenderOffsetForDecorativeBeamWeaponsOnly(Vector2f p0) {
        this.$$delegate_0.setRenderOffsetForDecorativeBeamWeaponsOnly(p0);
    }

    public float getRefireDelay() {
        return this.$$delegate_0.getRefireDelay();
    }

    public void forceShowBeamGlow() {
        this.$$delegate_0.forceShowBeamGlow();
    }

    public boolean isInBurst() {
        return this.$$delegate_0.isInBurst();
    }

    public WeaponSpecAPI getOriginalSpec() {
        return this.$$delegate_0.getOriginalSpec();
    }

    public void setWeaponGlowWidthMult(float p0) {
        this.$$delegate_0.setWeaponGlowWidthMult(p0);
    }

    public void setWeaponGlowHeightMult(float p0) {
        this.$$delegate_0.setWeaponGlowHeightMult(p0);
    }

    public void stopFiring() {
        this.$$delegate_0.stopFiring();
    }

    public boolean isForceDisabled() {
        return this.$$delegate_0.isForceDisabled();
    }

    public void setForceDisabled(boolean p0) {
        this.$$delegate_0.setForceDisabled(p0);
    }

    public Object getCustom() {
        return this.$$delegate_0.getCustom();
    }

    public void setCustom(Object p0) {
        this.$$delegate_0.setCustom(p0);
    }

    public boolean isForceNoFireOneFrame() {
        return this.$$delegate_0.isForceNoFireOneFrame();
    }

    public AimTracker getAimTracker() {
        return this.$$delegate_0.getAimTracker();
    }
}
