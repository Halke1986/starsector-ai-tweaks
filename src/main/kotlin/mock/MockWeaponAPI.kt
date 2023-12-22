package com.genir.aitweaks.mock

import com.fs.starfarer.api.AnimationAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.loading.MuzzleFlashSpec
import com.fs.starfarer.api.loading.WeaponSlotAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.properties.Delegates

open class MockWeaponAPI : WeaponAPI {
    override fun getId(): String {
        TODO("Unexpected mock method call")
    }

    override fun getType(): WeaponAPI.WeaponType {
        TODO("Unexpected mock method call")
    }

    override fun getSize(): WeaponAPI.WeaponSize {
        TODO("Unexpected mock method call")
    }

    override fun setPD(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun distanceFromArc(p0: Vector2f?): Float {
        TODO("Unexpected mock method call")
    }

    override fun isAlwaysFire(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getCurrSpread(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getCurrAngle(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getArcFacing(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getArc(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setCurrAngle(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getRange(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getDisplayArcRadius(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getChargeLevel(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getTurnRate(): Float {
        TODO("Unexpected mock method call")
    }

    var getProjectileSpeedMock by Delegates.notNull<Float>()
    override fun getProjectileSpeed(): Float = getProjectileSpeedMock

    override fun getDisplayName(): String {
        TODO("Unexpected mock method call")
    }

    override fun getAmmo(): Int {
        TODO("Unexpected mock method call")
    }

    override fun getMaxAmmo(): Int {
        TODO("Unexpected mock method call")
    }

    override fun setMaxAmmo(p0: Int) {
        TODO("Unexpected mock method call")
    }

    override fun resetAmmo() {
        TODO("Unexpected mock method call")
    }

    override fun getCooldownRemaining(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getCooldown(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setRemainingCooldownTo(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun isBeam(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isBurstBeam(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun isPulse(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun requiresFullCharge(): Boolean {
        TODO("Unexpected mock method call")
    }

    lateinit var getLocationMock: Vector2f
    override fun getLocation(): Vector2f = getLocationMock

    override fun isFiring(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun usesAmmo(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun usesEnergy(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun hasAIHint(p0: WeaponAPI.AIHints?): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getProjectileCollisionClass(): CollisionClass {
        TODO("Unexpected mock method call")
    }

    override fun beginSelectionFlash() {
        TODO("Unexpected mock method call")
    }

    override fun getFluxCostToFire(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getMaxHealth(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getCurrHealth(): Float {
        TODO("Unexpected mock method call")
    }

    override fun isDisabled(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getDisabledDuration(): Float {
        TODO("Unexpected mock method call")
    }

    override fun isPermanentlyDisabled(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getDamageType(): DamageType {
        TODO("Unexpected mock method call")
    }

    lateinit var getShipMock: ShipAPI
    override fun getShip(): ShipAPI = getShipMock

    override fun getDerivedStats(): WeaponAPI.DerivedWeaponStatsAPI {
        TODO("Unexpected mock method call")
    }

    override fun setAmmo(p0: Int) {
        TODO("Unexpected mock method call")
    }

    override fun getAnimation(): AnimationAPI {
        TODO("Unexpected mock method call")
    }

    override fun getSprite(): SpriteAPI {
        TODO("Unexpected mock method call")
    }

    override fun getUnderSpriteAPI(): SpriteAPI {
        TODO("Unexpected mock method call")
    }

    override fun getBarrelSpriteAPI(): SpriteAPI {
        TODO("Unexpected mock method call")
    }

    override fun renderBarrel(p0: SpriteAPI?, p1: Vector2f?, p2: Float) {
        TODO("Unexpected mock method call")
    }

    override fun isRenderBarrelBelow(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun disable() {
        TODO("Unexpected mock method call")
    }

    override fun disable(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun repair() {
        TODO("Unexpected mock method call")
    }

    override fun getSpec(): WeaponSpecAPI {
        TODO("Unexpected mock method call")
    }

    override fun getSlot(): WeaponSlotAPI {
        TODO("Unexpected mock method call")
    }

    override fun getEffectPlugin(): EveryFrameWeaponEffectPlugin {
        TODO("Unexpected mock method call")
    }

    override fun getMissileRenderData(): MutableList<MissileRenderDataAPI> {
        TODO("Unexpected mock method call")
    }

    override fun getDamage(): DamageAPI {
        TODO("Unexpected mock method call")
    }

    override fun getProjectileFadeRange(): Float {
        TODO("Unexpected mock method call")
    }

    override fun isDecorative(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun ensureClonedSpec() {
        TODO("Unexpected mock method call")
    }

    override fun getAmmoPerSecond(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setPDAlso(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun setCurrHealth(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getMuzzleFlashSpec(): MuzzleFlashSpec {
        TODO("Unexpected mock method call")
    }

    override fun getBeams(): MutableList<BeamAPI> {
        TODO("Unexpected mock method call")
    }

    override fun getFirePoint(p0: Int): Vector2f {
        TODO("Unexpected mock method call")
    }

    override fun setTurnRateOverride(p0: Float?) {
        TODO("Unexpected mock method call")
    }

    override fun getGlowSpriteAPI(): SpriteAPI {
        TODO("Unexpected mock method call")
    }

    override fun getAmmoTracker(): AmmoTrackerAPI {
        TODO("Unexpected mock method call")
    }

    override fun setRefireDelay(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun setFacing(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun updateBeamFromPoints() {
        TODO("Unexpected mock method call")
    }

    override fun isKeepBeamTargetWhileChargingDown(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setKeepBeamTargetWhileChargingDown(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun setScaleBeamGlowBasedOnDamageEffectiveness(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun setForceFireOneFrame(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun setGlowAmount(p0: Float, p1: Color?) {
        TODO("Unexpected mock method call")
    }

    override fun setForceNoFireOneFrame(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun setSuspendAutomaticTurning(p0: Boolean) {
        TODO("Unexpected mock method call")
    }

    override fun getBurstFireTimeRemaining(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getRenderOffsetForDecorativeBeamWeaponsOnly(): Vector2f {
        TODO("Unexpected mock method call")
    }

    override fun setRenderOffsetForDecorativeBeamWeaponsOnly(p0: Vector2f?) {
        TODO("Unexpected mock method call")
    }

    override fun getRefireDelay(): Float {
        TODO("Unexpected mock method call")
    }

    override fun forceShowBeamGlow() {
        TODO("Unexpected mock method call")
    }

    override fun isInBurst(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun getOriginalSpec(): WeaponSpecAPI {
        TODO("Unexpected mock method call")
    }

    override fun setWeaponGlowWidthMult(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun setWeaponGlowHeightMult(p0: Float) {
        TODO("Unexpected mock method call")
    }
}