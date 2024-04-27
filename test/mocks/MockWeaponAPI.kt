package mocks

import com.fs.starfarer.api.AnimationAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.loading.MuzzleFlashSpec
import com.fs.starfarer.api.loading.WeaponSlotAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

open class MockWeaponAPI(vararg values: Pair<String, Any?>) : WeaponAPI, Mock(*values) {
    override fun getId(): String? = getMockValue(object {})

    override fun getType(): WeaponAPI.WeaponType? = getMockValue(object {})

    override fun getSize(): WeaponAPI.WeaponSize? = getMockValue(object {})

    override fun setPD(p0: Boolean) = Unit

    override fun distanceFromArc(p0: Vector2f?): Float = getMockValue(object {})!!

    override fun isAlwaysFire(): Boolean = getMockValue(object {})!!

    override fun getCurrSpread(): Float = getMockValue(object {})!!

    override fun getCurrAngle(): Float = getMockValue(object {})!!

    override fun getArcFacing(): Float = getMockValue(object {})!!

    override fun getArc(): Float = getMockValue(object {})!!

    override fun setCurrAngle(p0: Float) = Unit

    override fun getRange(): Float = getMockValue(object {})!!

    override fun getDisplayArcRadius(): Float = getMockValue(object {})!!

    override fun getChargeLevel(): Float = getMockValue(object {})!!

    override fun getTurnRate(): Float = getMockValue(object {})!!

    override fun getProjectileSpeed(): Float = getMockValue(object {})!!

    override fun getDisplayName(): String? = getMockValue(object {})

    override fun getAmmo(): Int = getMockValue(object {})!!

    override fun getMaxAmmo(): Int = getMockValue(object {})!!

    override fun setMaxAmmo(p0: Int) = Unit

    override fun resetAmmo() = Unit

    override fun getCooldownRemaining(): Float = getMockValue(object {})!!

    override fun getCooldown(): Float = getMockValue(object {})!!

    override fun setRemainingCooldownTo(p0: Float) = Unit

    override fun isBeam(): Boolean = getMockValue(object {})!!

    override fun isBurstBeam(): Boolean = getMockValue(object {})!!

    override fun isPulse(): Boolean = getMockValue(object {})!!

    override fun requiresFullCharge(): Boolean = getMockValue(object {})!!

    override fun getLocation(): Vector2f? = getMockValue(object {})

    override fun isFiring(): Boolean = getMockValue(object {})!!

    override fun usesAmmo(): Boolean = getMockValue(object {})!!

    override fun usesEnergy(): Boolean = getMockValue(object {})!!

    override fun hasAIHint(p0: WeaponAPI.AIHints?): Boolean = getMockValue(object {})!!

    override fun getProjectileCollisionClass(): CollisionClass? = getMockValue(object {})

    override fun beginSelectionFlash() = Unit

    override fun getFluxCostToFire(): Float = getMockValue(object {})!!

    override fun getMaxHealth(): Float = getMockValue(object {})!!

    override fun getCurrHealth(): Float = getMockValue(object {})!!

    override fun isDisabled(): Boolean = getMockValue(object {})!!

    override fun getDisabledDuration(): Float = getMockValue(object {})!!

    override fun isPermanentlyDisabled(): Boolean = getMockValue(object {})!!

    override fun getDamageType(): DamageType? = getMockValue(object {})

    override fun getShip(): ShipAPI? = getMockValue(object {})

    override fun getDerivedStats(): WeaponAPI.DerivedWeaponStatsAPI? = getMockValue(object {})

    override fun setAmmo(p0: Int) = Unit

    override fun getAnimation(): AnimationAPI? = getMockValue(object {})

    override fun getSprite(): SpriteAPI? = getMockValue(object {})

    override fun getUnderSpriteAPI(): SpriteAPI? = getMockValue(object {})

    override fun getBarrelSpriteAPI(): SpriteAPI? = getMockValue(object {})

    override fun renderBarrel(p0: SpriteAPI?, p1: Vector2f?, p2: Float) = Unit

    override fun isRenderBarrelBelow(): Boolean = getMockValue(object {})!!

    override fun disable() = Unit

    override fun disable(p0: Boolean) = Unit

    override fun repair() = Unit

    override fun getSpec(): WeaponSpecAPI? = getMockValue(object {})

    override fun getSlot(): WeaponSlotAPI? = getMockValue(object {})

    override fun getEffectPlugin(): EveryFrameWeaponEffectPlugin? = getMockValue(object {})

    override fun getMissileRenderData(): MutableList<MissileRenderDataAPI>? = getMockValue(object {})

    override fun getDamage(): DamageAPI? = getMockValue(object {})

    override fun getProjectileFadeRange(): Float = getMockValue(object {})!!

    override fun isDecorative(): Boolean = getMockValue(object {})!!

    override fun ensureClonedSpec() = Unit

    override fun getAmmoPerSecond(): Float = getMockValue(object {})!!

    override fun setPDAlso(p0: Boolean) = Unit

    override fun setCurrHealth(p0: Float) = Unit

    override fun getMuzzleFlashSpec(): MuzzleFlashSpec? = getMockValue(object {})

    override fun getBeams(): MutableList<BeamAPI>? = getMockValue(object {})

    override fun getFirePoint(p0: Int): Vector2f? = getMockValue(object {})

    override fun setTurnRateOverride(p0: Float?) = Unit

    override fun getGlowSpriteAPI(): SpriteAPI? = getMockValue(object {})

    override fun getAmmoTracker(): AmmoTrackerAPI? = getMockValue(object {})

    override fun setRefireDelay(p0: Float) = Unit

    override fun setFacing(p0: Float) = Unit

    override fun updateBeamFromPoints() = Unit

    override fun isKeepBeamTargetWhileChargingDown(): Boolean = getMockValue(object {})!!

    override fun setKeepBeamTargetWhileChargingDown(p0: Boolean) = Unit

    override fun setScaleBeamGlowBasedOnDamageEffectiveness(p0: Boolean) = Unit

    override fun setForceFireOneFrame(p0: Boolean) = Unit

    override fun setGlowAmount(p0: Float, p1: Color?) = Unit

    override fun setForceNoFireOneFrame(p0: Boolean) = Unit

    override fun setSuspendAutomaticTurning(p0: Boolean) = Unit

    override fun getBurstFireTimeRemaining(): Float = getMockValue(object {})!!

    override fun getRenderOffsetForDecorativeBeamWeaponsOnly(): Vector2f? = getMockValue(object {})

    override fun setRenderOffsetForDecorativeBeamWeaponsOnly(p0: Vector2f?) = Unit

    override fun getRefireDelay(): Float = getMockValue(object {})!!

    override fun forceShowBeamGlow() = Unit

    override fun isInBurst(): Boolean = getMockValue(object {})!!

    override fun getOriginalSpec(): WeaponSpecAPI? = getMockValue(object {})

    override fun setWeaponGlowWidthMult(p0: Float) = Unit

    override fun setWeaponGlowHeightMult(p0: Float) = Unit

    override fun stopFiring() = Unit
}