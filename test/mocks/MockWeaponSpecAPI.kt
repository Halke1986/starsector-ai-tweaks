package mocks

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class MockWeaponSpecAPI(vararg values: Pair<String, Any?>) : WeaponSpecAPI, Mock(*values) {
    override fun getOrdnancePointCost(stats: MutableCharacterStatsAPI?): Float = getMockValue(object {})!!

    override fun getOrdnancePointCost(stats: MutableCharacterStatsAPI?, shipStats: MutableShipStatsAPI?): Float = getMockValue(object {})!!

    override fun getAIHints(): EnumSet<WeaponAPI.AIHints> = getMockValue(object {})!!

    override fun getType(): WeaponAPI.WeaponType = getMockValue(object {})!!

    override fun getAmmoPerSecond(): Float = getMockValue(object {})!!

    override fun getTier(): Int = getMockValue(object {})!!

    override fun getBaseValue(): Float = getMockValue(object {})!!

    override fun usesAmmo(): Boolean = getMockValue(object {})!!

    override fun getMaxAmmo(): Int = getMockValue(object {})!!

    override fun getWeaponId(): String = getMockValue(object {})!!

    override fun getSize(): WeaponAPI.WeaponSize = getMockValue(object {})!!

    override fun getWeaponName(): String = getMockValue(object {})!!

    override fun getBurstSize(): Int = getMockValue(object {})!!

    override fun getTags(): MutableSet<String> = getMockValue(object {})!!

    override fun addTag(tag: String?) = Unit

    override fun hasTag(tag: String?): Boolean = getMockValue(object {})!!

    override fun getRarity(): Float = getMockValue(object {})!!

    override fun setRarity(rarity: Float) = Unit

    override fun getDerivedStats(): WeaponAPI.DerivedWeaponStatsAPI = getMockValue(object {})!!

    override fun getHardpointFireOffsets(): MutableList<Vector2f> = getMockValue(object {})!!

    override fun getHardpointAngleOffsets(): MutableList<Float> = getMockValue(object {})!!

    override fun getTurretFireOffsets(): MutableList<Vector2f> = getMockValue(object {})!!

    override fun getTurretAngleOffsets(): MutableList<Float> = getMockValue(object {})!!

    override fun getHiddenFireOffsets(): MutableList<Vector2f> = getMockValue(object {})!!

    override fun getHiddenAngleOffsets(): MutableList<Float> = getMockValue(object {})!!

    override fun getHardpointSpriteName(): String = getMockValue(object {})!!

    override fun getTurretSpriteName(): String = getMockValue(object {})!!

    override fun getHardpointUnderSpriteName(): String = getMockValue(object {})!!

    override fun getTurretUnderSpriteName(): String = getMockValue(object {})!!

    override fun getManufacturer(): String = getMockValue(object {})!!

    override fun setManufacturer(manufacturer: String?) = Unit

    override fun getAutofitCategory(): String = getMockValue(object {})!!

    override fun getAutofitCategoriesInPriorityOrder(): MutableList<String> = getMockValue(object {})!!

    override fun getWeaponGroupTag(): String = getMockValue(object {})!!

    override fun setWeaponGroupTag(weaponGroupTag: String?) = Unit

    override fun isBeam(): Boolean = getMockValue(object {})!!

    override fun getPrimaryRoleStr(): String = getMockValue(object {})!!

    override fun setPrimaryRoleStr(primaryRoleStr: String?) = Unit

    override fun getSpeedStr(): String = getMockValue(object {})!!

    override fun setSpeedStr(speedStr: String?) = Unit

    override fun getTrackingStr(): String = getMockValue(object {})!!

    override fun setTrackingStr(trackingStr: String?) = Unit

    override fun getTurnRateStr(): String = getMockValue(object {})!!

    override fun setTurnRateStr(turnRateStr: String?) = Unit

    override fun getAccuracyStr(): String = getMockValue(object {})!!

    override fun setAccuracyStr(accuracyStr: String?) = Unit

    override fun getCustomPrimary(): String = getMockValue(object {})!!

    override fun setCustomPrimary(customPrimary: String?) = Unit

    override fun getCustomPrimaryHL(): String = getMockValue(object {})!!

    override fun setCustomPrimaryHL(customPrimaryHL: String?) = Unit

    override fun getCustomAncillary(): String = getMockValue(object {})!!

    override fun setCustomAncillary(customAncillary: String?) = Unit

    override fun getCustomAncillaryHL(): String = getMockValue(object {})!!

    override fun setCustomAncillaryHL(customAncillaryHL: String?) = Unit

    override fun isNoDPSInTooltip(): Boolean = getMockValue(object {})!!

    override fun setNoDPSInTooltip(noDPSInTooltip: Boolean) = Unit

    override fun getGlowColor(): Color = getMockValue(object {})!!

    override fun isInterruptibleBurst(): Boolean = getMockValue(object {})!!

    override fun isNoImpactSounds(): Boolean = getMockValue(object {})!!

    override fun setNoImpactSounds(noImpactSounds: Boolean) = Unit

    override fun getDamageType(): DamageType = getMockValue(object {})!!

    override fun isRenderAboveAllWeapons(): Boolean = getMockValue(object {})!!

    override fun setRenderAboveAllWeapons(renderAboveAllWeapons: Boolean) = Unit

    override fun isNoShieldImpactSounds(): Boolean = getMockValue(object {})!!

    override fun setNoShieldImpactSounds(noShieldImpactSounds: Boolean) = Unit

    override fun isNoNonShieldImpactSounds(): Boolean = getMockValue(object {})!!

    override fun setNoNonShieldImpactSounds(noNonShieldImpactSounds: Boolean) = Unit

    override fun getMinSpread(): Float = getMockValue(object {})!!

    override fun getMaxSpread(): Float = getMockValue(object {})!!

    override fun getSpreadDecayRate(): Float = getMockValue(object {})!!

    override fun getSpreadBuildup(): Float = getMockValue(object {})!!

    override fun setMinSpread(minSpread: Float) = Unit

    override fun setMaxSpread(maxSpread: Float) = Unit

    override fun setSpreadDecayRate(spreadDecayRate: Float) = Unit

    override fun setSpreadBuildup(spreadBuildup: Float) = Unit

    override fun getBurstDuration(): Float = getMockValue(object {})!!

    override fun getAutofireAccBonus(): Float = getMockValue(object {})!!

    override fun setAutofireAccBonus(autofireAccBonus: Float) = Unit

    override fun getProjectileSpec(): Any = getMockValue(object {})!!

    override fun getBeamChargeupTime(): Float = getMockValue(object {})!!

    override fun getBeamChargedownTime(): Float = getMockValue(object {})!!

    override fun isUnaffectedByProjectileSpeedBonuses(): Boolean = getMockValue(object {})!!

    override fun setUnaffectedByProjectileSpeedBonuses(unaffectedByProjectileSpeedBonuses: Boolean) = Unit

    override fun getChargeTime(): Float = getMockValue(object {})!!

    override fun getMountType(): WeaponAPI.WeaponType = getMockValue(object {})!!

    override fun setMountType(mountType: WeaponAPI.WeaponType?) = Unit

    override fun getExtraArcForAI(): Float = getMockValue(object {})!!

    override fun setExtraArcForAI(extraArcForAI: Float) = Unit

    override fun setWeaponName(weaponName: String?) = Unit

    override fun getMaxRange(): Float = getMockValue(object {})!!

    override fun setMaxRange(maxRange: Float) = Unit

    override fun setOrdnancePointCost(armamentCapacity: Float) = Unit

    override fun isShowDamageWhenDecorative(): Boolean = getMockValue(object {})!!

    override fun isBurstBeam(): Boolean = getMockValue(object {})!!

    override fun isStopPreviousFireSound(): Boolean = getMockValue(object {})!!

    override fun setStopPreviousFireSound(stopPreviousFireSound: Boolean) = Unit

    override fun isPlayFullFireSoundOne(): Boolean = getMockValue(object {})!!

    override fun setPlayFullFireSoundOne(playFullFireSoundOne: Boolean) = Unit

    override fun setBeamSpeed(beamSpeed: Float) = Unit

    override fun setMaxAmmo(maxAmmo: Int) = Unit

    override fun setAmmoPerSecond(ammoPerSecond: Float) = Unit

    override fun getReloadSize(): Float = getMockValue(object {})!!

    override fun setReloadSize(reloadSize: Float) = Unit

    override fun setProjectileSpeed(projectileSpeed: Float) = Unit

    override fun getTurnRate(): Float = getMockValue(object {})!!

    override fun setTurnRate(turnRate: Float) = Unit

    override fun isRestrictToSpecifiedMountType(): Boolean = getMockValue(object {})!!

    override fun setRestrictToSpecifiedMountType(restrictToSpecifiedMountType: Boolean) = Unit
}