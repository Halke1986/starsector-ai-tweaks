package com.genir.aitweaks.core.handles

import com.fs.starfarer.api.AnimationAPI
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.loading.*
import com.fs.starfarer.api.loading.ProjectileSpawnType.BALLISTIC_AS_BEAM
import com.fs.starfarer.api.loading.ProjectileSpawnType.BEAM
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.autofire.AutofireAI
import com.genir.aitweaks.core.shipai.autofire.Tag
import com.genir.aitweaks.core.shipai.autofire.ballistics.*
import com.genir.aitweaks.core.shipai.autofire.firingCycle
import com.genir.aitweaks.core.shipai.autofire.hasAITag
import com.genir.aitweaks.core.state.Config
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection
import com.genir.starfarer.combat.entities.ship.trackers.AimTracker
import com.genir.starfarer.combat.entities.ship.weapons.BeamWeapon
import com.genir.starfarer.combat.systems.Weapon
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.floor

/**
 * WeaponHandle provides access to:
 * - WeaponAPI methods, with some overridden to include AI Tweaks–specific behavior
 * - additional methods extending the WeaponAPI interface
 * - unobfuscated methods from the underlying Weapon engine object
 */
@JvmInline
value class WeaponHandle(val weaponAPI: WeaponAPI) {
    companion object {
        val WeaponAPI.handle: WeaponHandle
            get() = WeaponHandle(this)
    }

    val ballistics: Ballistics
        get() = when {
            isBeam -> Beam(this)

            isUnguidedMissile -> Missile(this)

            else -> Projectile(this)
        }

    val isAntiFighter: Boolean
        get() = hasAITag(Tag.ANTI_FIGHTER) || hasAIHint(AIHints.ANTI_FTR)

    val isStrictlyAntiShield: Boolean
        get() = when {
            !Config.config.enableNeedlerFix -> false

            hasAITag(Tag.ANTI_SHIELD) -> true

            damageType == DamageType.KINETIC && usesAmmo() -> true

            else -> false
        }

    val isStrictlyAntiArmor: Boolean
        get() = when {
            hasAIHint(AIHints.USE_LESS_VS_SHIELDS) -> true

            hasAITag(Tag.USE_LESS_VS_SHIELDS) -> true

            isFinisherBeam -> true

            else -> false
        }

    val isFinisherBeam
        get() = when {
            spec?.isBeam != true -> false

            !hasAITag(Tag.FINISHER_BEAM) -> false

            !ship.variant.hasHullMod("aitweaks_finisher_beam_protocol") -> false

            else -> true
        }

    val isPD: Boolean
        get() = hasAIHint(AIHints.PD) || hasAIHint(AIHints.PD_ONLY)

    /** Same as WeaponAPI.isPD, except it will ignore weapons that
     * were modified into PD by S-modded Integrated Point Defense AI. */
    val isPDSpec: Boolean
        get() = spec?.aiHints?.contains(AIHints.PD) == true || spec?.aiHints?.contains(AIHints.PD_ONLY) == true

    val isMissile: Boolean
        get() = type == WeaponType.MISSILE

    val isUnguidedMissile: Boolean
        get() = (spec?.projectileSpec as? MissileSpecAPI)?.typeString == "ROCKET"

    val isPlainBeam: Boolean
        get() = isBeam && !conserveAmmo

    val conserveAmmo: Boolean
        get() = usesAmmo() || isBurstBeam || cooldown > 5f

    val hasAmmoToSpare: Boolean
        get() = !usesAmmo() || ammoTracker.let { it.ammoPerSecond > 0 && (it.ammo + it.reloadSize > it.maxAmmo) }

    val hasBestTargetLeading: Boolean
        get() = isPD && !hasAIHint(AIHints.STRIKE) && ship.mutableStats.dynamic.getValue("pd_best_target_leading", 0f) >= 1f

    val ignoresFlares: Boolean
        get() = hasAIHint(AIHints.IGNORES_FLARES) || ship.mutableStats.dynamic.getValue("pd_ignores_flares", 0f) >= 1f

    /** Is angle in weapon arc in SHIP COORDINATES. */
    fun isAngleInArc(angle: Direction): Boolean {
        return arc.contains(angle, tolerance = 0.01f)
    }

    val isFrontFacing: Boolean
        get() = isAngleInArc(0f.toDirection)

    /** Effective weapon attack arc. **/
    val arc: Arc
        get() {
            return when {
                // Assume guided missile and special guided weapon like the Voltaic Discharge
                // can attack in full arc, regardless of slot arc.
                hasAIHint(AIHints.GUIDED_POOR) || hasAIHint(AIHints.DO_NOT_AIM) -> {
                    Arc(360f, arcFacing.toDirection)
                }

                // Missile hardpoints can't move at all, even if weapon slot has non-zero arc.
                isMissile && slot.isHardpoint -> {
                    Arc(0f, arcFacing.toDirection)
                }

                else -> {
                    Arc(weaponAPI.arc, arcFacing.toDirection)
                }
            }
        }

    /** Weapon arc in absolute coordinates, instead of ship coordinates */
    val absoluteArc: Arc
        get() = arc.rotated(ship.facing)

    /** Actual weapon range depends on frame duration. */
    val range: Float
        get() {
            // For beam and missile weapons delegate
            // the range calculation to vanilla.
            val spec: WeaponSpecAPI = spec ?: return 0f
            if (spec.projectileSpec == null || type == WeaponType.MISSILE) {
                return weaponAPI.range
            }

            // Projectile always travels for integer number of frames.
            val idealDt: Float = Global.getCombatEngine().timeMult.modifiedValue / 60
            val distPerFrame: Float = projectileSpeed * idealDt

            val flightDistance = weaponAPI.range - projectileLength
            val frames: Int = (flightDistance / distPerFrame).toInt()

            // Special case of scripted weapons with very high
            // projectile speed, like the Rift Lightning.
            if (frames == 0) {
                return weaponAPI.range
            }

            val projectileRange: Float = distPerFrame * frames
            val ensureHitBuffer = 10f
            return projectileSpawnOffset + projectileRange - ensureHitBuffer
        }

    val engagementRange: Float
        get() = range + projectileFadeRange * 0.33f

    val threatRange: Float
        get() {
            val spec: WeaponSpecAPI = spec ?: return 0f
            return when {
                // Missiles may cause damage beyond the weapon engagement range.
                spec.projectileSpec is MissileSpecAPI -> projectileSpeed * (spec.projectileSpec as MissileSpecAPI).maxFlightTime

                else -> engagementRange
            }
        }

    val autofirePlugin: AutofireAIPlugin?
        get() = ship.getWeaponGroupFor(weaponAPI)?.getAutofirePlugin(weaponAPI)

    val customAI: AutofireAI?
        get() = autofirePlugin as? AutofireAI

    private val isBurstWeapon: Boolean
        get() = when {
            // Projectile weapons with bursts of more than one projectile.
            spec is ProjectileWeaponSpecAPI -> (spec as ProjectileWeaponSpecAPI).burstSize > 1

            else -> isBurstBeam
        }

    val isNonInterruptibleBurstWeapon: Boolean
        get() {
            val spec: WeaponSpecAPI? = spec
            return when {
                spec == null -> false

                spec.isInterruptibleBurst -> false

                // Exclude "continuous" burst beams like the IR Autolance.
                isBeam && spec.burstDuration > 0f && cooldown == 0f -> false

                else -> isBurstWeapon
            }
        }

    /** Warmup is the first phase of weapon firing sequence, preceding the first shot.
     * WARMUP */
    val isInWarmup: Boolean
        get() = when {
            isBurstBeam -> false // For burst beams even the warmup is counted towards burst.
            isBeam -> false
            else -> chargeLevel > 0f && chargeLevel < 1f && cooldownRemaining == 0f
        }

    /** Replacement for vanilla isInBurst. As opposed to isInBurst,
     * IsInBurst returns true for active burst beams.
     * BURST */
    val isInBurst: Boolean
        get() {
            return when {
                !isNonInterruptibleBurstWeapon -> {
                    false
                }

                isBurstBeam -> {
                    val state = (weaponAPI as BeamWeapon).chargeTracker.beamChargeTracker_getState()
                    state.name != "IDLE"
                }

                isBeam -> {
                    false
                }

                else -> {
                    chargeLevel == 1f
                }
            }
        }

    /** Weapon is assumed to be in a firing sequence if it will
     * emit projectile or beam even after trigger is let go.
     * WARMUP + BURST */
    val isInFiringSequence: Boolean
        get() = isInWarmup || isInBurst

    /** Similar to WeaponAPI.isFiring, except returns true for the entire firing cycle.
     * WeaponAPI.isFiring returns false between individual burst attacks.
     * WARMUP + BURST + COOLDOWN */
    val isInFiringCycle: Boolean
        get() = chargeLevel != 0f || cooldownRemaining != 0f

    val isIdle: Boolean
        get() = !isInFiringCycle

    val group: WeaponGroupAPI?
        get() = ship.getWeaponGroupFor(weaponAPI)

    val target: CombatEntityAPI?
        get() = autofirePlugin?.let { it.targetShip ?: it.targetMissile } ?: ship.shipTarget

    /** Calculate the barrel offset for the weaponAPI.
     * For multi-barreled weapons, average offset is returned. */
    val barrelOffset: Vector2f
        get() {
            val spec: WeaponSpecAPI = spec
                ?: return Vector2f()

            val offsets: List<Vector2f> = when {
                slot.isHardpoint -> spec.hardpointFireOffsets

                slot.isTurret -> spec.turretFireOffsets

                else -> listOf()
            }

            if (offsets.isEmpty()) {
                return Vector2f()
            }

            val offsetSum: Vector2f = offsets.fold(Vector2f()) { sum, offset -> sum + offset }
            val average: Vector2f = offsetSum / offsets.size.toFloat()

            return average
        }

    /** Distance along the firing angle from the weapon's
     * location where the projectile is spawned. */
    val projectileSpawnOffset: Float
        get() = barrelOffset.x + projectileLength

    private val projectileLength: Float
        get() {
            val projectileSpec: ProjectileSpecAPI? = spec?.projectileSpec as? ProjectileSpecAPI

            return when (projectileSpec?.spawnType) {
                // MovingRay projectile class
                BEAM,
                BALLISTIC_AS_BEAM -> {
                    // Why the projectile length is governed by its width instead of length?
                    // Who knows, possibly a Starsector engine bug.
                    projectileSpec.width / 2
                }

                else -> 0f
            }
        }

    val effectiveDPS: Float
        get() = derivedStats.dps * if (damageType == DamageType.FRAGMENTATION) 0.25f else 1f

    /** A rough estimation of maximum DPS in a given time period. */
    fun peakDPS(duration: Float): Float {
        val cycle = firingCycle

        val burstsNumber = when {
            // Non-burst weapons or burst weapons with short cycle.
            !isBurstWeapon || cycle.duration <= duration -> {
                floor(duration / cycle.duration).coerceAtLeast(1f)
            }

            else -> {
                (duration / cycle.burstDuration).coerceAtMost(1f)
            }
        }

        var baseDamage = burstsNumber * cycle.damage

        if (usesAmmo()) {
            baseDamage /= 2
        }

        if (damageType == DamageType.FRAGMENTATION) {
            baseDamage /= 4
        }

        return baseDamage
    }

    /** The true projectile speed, which may differ from the
     * value returned by vanilla WeaponAPI.projectileSpeed. */
    val projectileSpeed: Float
        get() {
            return when (val projectileSpec = spec?.projectileSpec) {
                is ProjectileSpecAPI -> {
                    projectileSpec.getMoveSpeed(ship.mutableStats, weaponAPI)
                }

                is MissileSpecAPI -> {
                    val engineSpec: ShipHullSpecAPI.EngineSpecAPI = projectileSpec.hullSpec.engineSpec
                    val shipStats: MutableShipStatsAPI = ship.mutableStats
                    val maxSpeedStat = MutableStat(engineSpec.maxSpeed)

                    maxSpeedStat.applyMods(shipStats.missileMaxSpeedBonus)

                    maxSpeedStat.modifiedValue
                }

                else -> weaponAPI.projectileSpeed
            }
        }

    /** Can the weapon shoot over allied ships. */
    val noFF: Boolean
        get() = when (projectileCollisionClass) {
            CollisionClass.MISSILE_NO_FF -> true
            CollisionClass.PROJECTILE_NO_FF -> true
            CollisionClass.RAY_FIGHTER -> true
            CollisionClass.PROJECTILE_FIGHTER -> true
            else -> false
        }

    val isOutOfAmmo: Boolean
        get() = usesAmmo() && ammo == 0

    val isPermanentlyOutOfAmmo: Boolean
        get() = isOutOfAmmo && ammoTracker.ammoPerSecond <= 0f

    val isInLongReload: Boolean
        get() = customAI?.reloadTracker?.isInLongReload == true

    val turnRateWhileIdle: Float
        get() {
            // Use vanilla isFiring instead of the precise isInFiringCycle extension,
            // to match the vanilla method of calculating the turn rate.
            return if (isFiring) {
                // When idle, vanilla multiplies weapon turn rate by 5.
                turnRate * 5
            } else {
                turnRate
            }
        }

    val turnRateWhileFiring: Float
        get() = turnRateWhileIdle / 5

    val rofMultiplier: Float
        get(): Float {
            val base: Float = when (type) {
                WeaponType.BALLISTIC -> ship.mutableStats.ballisticRoFMult.modifiedValue
                WeaponType.ENERGY -> ship.mutableStats.energyRoFMult.modifiedValue
                WeaponType.MISSILE -> ship.mutableStats.missileRoFMult.modifiedValue
                else -> 1f
            }

            return base * weaponAPI.ship.timeMult
        }

    val ammoRegenMultiplier: Float
        get(): Float {
            val base: Float = when (type) {
                WeaponType.BALLISTIC -> ship.mutableStats.ballisticAmmoRegenMult.modifiedValue
                WeaponType.ENERGY -> ship.mutableStats.energyAmmoRegenMult.modifiedValue
                WeaponType.MISSILE -> ship.mutableStats.missileAmmoRegenMult.modifiedValue
                else -> 1f
            }

            return base * weaponAPI.ship.timeMult
        }

    /** Ballistic calculations are performed before the game engine updates weapon states.
     * For beam weapons, which fire after completing rotation, this means the ballistic
     * calculations use outdated facing values.
     * To prevent errors caused by this timing mismatch, the facingWhenFiringThisFrame
     * method computes the expected weapon facing at the moment of firing. */
    val facingWhenFiringThisFrame: Direction
        get() {
            val currAngle = currAngle.toDirection

            // Non-beam weapons fire *before* rotation.
            if (!isBeam) {
                return currAngle
            }

            val ai: AutofireAIPlugin = autofirePlugin
                ?: return currAngle

            val target: CombatEntityAPI = (ai.targetShip ?: ai.targetMissile)
                ?: return currAngle

            // Assume beam weapon will aim directly at the target.
            val expectedAngle = (target.location - location).facing
            val offset = currAngle - expectedAngle
            val turnRate = turnRate * Global.getCombatEngine().elapsedInLastFrame

            return if (offset.length < turnRate) {
                expectedAngle
            } else {
                currAngle
            }
        }

    val aimTracker: AimTracker
        get() = (weaponAPI as Weapon).aimTracker

    /**
     * getAccuracy returns current weapon accuracy.
     * The value is a number in range [1.0;2.0]. 1.0 is perfect accuracy, 2.0f is the worst accuracy.
     * For worst accuracy, the weapon should aim exactly in the middle point between the target actual
     * position and calculated intercept position.
     */
    val currentAccuracy: Float
        get() {
            if (hasBestTargetLeading || Global.getCurrentState() == GameState.TITLE) {
                return 1f
            }

            val accBase = ship.aimAccuracy
            val accBonus = spec?.autofireAccBonus ?: 0f

            // Accuracy increases as the weapon attacks same target.
            val attackTime = customAI?.attackTime ?: 0f
            return (accBase - (accBonus + attackTime / 15f)).coerceAtLeast(1f)
        }

    val currentBallisticsParams: BallisticParams
        get() = BallisticParams(currentAccuracy, 0f)

// ****************************************************************************
// WeaponAPI Implementation

    val id: String
        get() = weaponAPI.id

    val type: WeaponType
        get() = weaponAPI.type

    val size: WeaponAPI.WeaponSize
        get() = weaponAPI.size

    fun setPD(p0: Boolean) {
        weaponAPI.setPD(p0)
    }

    fun distanceFromArc(p0: Vector2f?): Float {
        return weaponAPI.distanceFromArc(p0)
    }

    val isAlwaysFire: Boolean
        get() = weaponAPI.isAlwaysFire

    val currSpread: Float
        get() = weaponAPI.currSpread

    var currAngle: Float
        get() = weaponAPI.currAngle
        set(p0) {
            weaponAPI.currAngle = p0
        }

    val arcFacing: Float
        get() = weaponAPI.arcFacing

//    OVERRIDE
//    val arc: Float
//        get() = weaponAPI.arc

//    OVERRIDE
//    val range: Float
//        get() = weaponAPI.range

    val displayArcRadius: Float
        get() = weaponAPI.displayArcRadius

    val chargeLevel: Float
        get() = weaponAPI.chargeLevel

    val turnRate: Float
        get() = weaponAPI.turnRate

//    OVERRIDE
//    val projectileSpeed: Float
//        get() = weaponAPI.projectileSpeed

    val displayName: String
        get() = weaponAPI.displayName

    var ammo: Int
        get() = weaponAPI.ammo
        set(p0) {
            weaponAPI.ammo = p0
        }

    var maxAmmo: Int
        get() = weaponAPI.maxAmmo
        set(p0) {
            weaponAPI.maxAmmo = p0
        }

    fun resetAmmo() {
        weaponAPI.resetAmmo()
    }

    val cooldownRemaining: Float
        get() = weaponAPI.cooldownRemaining

    val cooldown: Float
        get() = weaponAPI.cooldown

    fun setRemainingCooldownTo(p0: Float) {
        weaponAPI.setRemainingCooldownTo(p0)
    }

    val isBeam: Boolean
        get() = weaponAPI.isBeam

    val isBurstBeam: Boolean
        get() = weaponAPI.isBurstBeam

    val isPulse: Boolean
        get() = weaponAPI.isPulse

    fun requiresFullCharge(): Boolean {
        return weaponAPI.requiresFullCharge()
    }

    val location: Vector2f
        get() = weaponAPI.location

    val isFiring: Boolean
        get() = weaponAPI.isFiring

    fun usesAmmo(): Boolean {
        return weaponAPI.usesAmmo()
    }

    fun usesEnergy(): Boolean {
        return weaponAPI.usesEnergy()
    }

    fun hasAIHint(p0: AIHints?): Boolean {
        return weaponAPI.hasAIHint(p0)
    }

    val projectileCollisionClass: CollisionClass
        get() = weaponAPI.projectileCollisionClass

    fun beginSelectionFlash() {
        weaponAPI.beginSelectionFlash()
    }

    val fluxCostToFire: Float
        get() = weaponAPI.fluxCostToFire

    val maxHealth: Float
        get() = weaponAPI.maxHealth

    var currHealth: Float
        get() = weaponAPI.currHealth
        set(p0) {
            weaponAPI.currHealth = p0
        }

    val isDisabled: Boolean
        get() = weaponAPI.isDisabled

    val disabledDuration: Float
        get() = weaponAPI.disabledDuration

    val isPermanentlyDisabled: Boolean
        get() = weaponAPI.isPermanentlyDisabled

    val damageType: DamageType
        get() = weaponAPI.damageType

    val ship: ShipAPI
        get() = weaponAPI.ship

    val derivedStats: WeaponAPI.DerivedWeaponStatsAPI
        get() = weaponAPI.derivedStats

    val animation: AnimationAPI
        get() = weaponAPI.animation

    val sprite: SpriteAPI
        get() = weaponAPI.sprite

    val underSpriteAPI: SpriteAPI
        get() = weaponAPI.underSpriteAPI

    val barrelSpriteAPI: SpriteAPI
        get() = weaponAPI.barrelSpriteAPI

    fun renderBarrel(p0: SpriteAPI?, p1: Vector2f?, p2: Float) {
        weaponAPI.renderBarrel(p0, p1, p2)
    }

    val isRenderBarrelBelow: Boolean
        get() = weaponAPI.isRenderBarrelBelow

    fun disable() {
        weaponAPI.disable()
    }

    fun disable(p0: Boolean) {
        weaponAPI.disable(p0)
    }

    fun repair() {
        weaponAPI.repair()
    }

    val spec: WeaponSpecAPI?
        get() = weaponAPI.spec

    val slot: WeaponSlotAPI
        get() = weaponAPI.slot

    val effectPlugin: EveryFrameWeaponEffectPlugin
        get() = weaponAPI.effectPlugin

    val missileRenderData: List<*>
        get() = weaponAPI.missileRenderData

    val damage: DamageAPI
        get() = weaponAPI.damage

    val projectileFadeRange: Float
        get() = weaponAPI.projectileFadeRange

    val isDecorative: Boolean
        get() = weaponAPI.isDecorative

    fun ensureClonedSpec() {
        weaponAPI.ensureClonedSpec()
    }

    val ammoPerSecond: Float
        get() = weaponAPI.ammoPerSecond

    fun setPDAlso(p0: Boolean) {
        weaponAPI.setPDAlso(p0)
    }

    val muzzleFlashSpec: MuzzleFlashSpec
        get() = weaponAPI.muzzleFlashSpec

    val beams: List<*>
        get() = weaponAPI.beams

    fun getFirePoint(p0: Int): Vector2f {
        return weaponAPI.getFirePoint(p0)
    }

    fun setTurnRateOverride(p0: Float?) {
        weaponAPI.setTurnRateOverride(p0)
    }

    val glowSpriteAPI: SpriteAPI
        get() = weaponAPI.glowSpriteAPI

    val ammoTracker: AmmoTrackerAPI
        get() = weaponAPI.ammoTracker

    fun setFacing(p0: Float) {
        weaponAPI.setFacing(p0)
    }

    fun updateBeamFromPoints() {
        weaponAPI.updateBeamFromPoints()
    }

    var isKeepBeamTargetWhileChargingDown: Boolean
        get() = weaponAPI.isKeepBeamTargetWhileChargingDown
        set(p0) {
            weaponAPI.isKeepBeamTargetWhileChargingDown = p0
        }

    fun setScaleBeamGlowBasedOnDamageEffectiveness(p0: Boolean) {
        weaponAPI.setScaleBeamGlowBasedOnDamageEffectiveness(p0)
    }

    fun setForceFireOneFrame(p0: Boolean) {
        weaponAPI.setForceFireOneFrame(p0)
    }

    fun setGlowAmount(p0: Float, p1: Color?) {
        weaponAPI.setGlowAmount(p0, p1)
    }

    fun setSuspendAutomaticTurning(p0: Boolean) {
        weaponAPI.setSuspendAutomaticTurning(p0)
    }

    val burstFireTimeRemaining: Float
        get() = weaponAPI.burstFireTimeRemaining

    var renderOffsetForDecorativeBeamWeaponsOnly: Vector2f?
        get() = weaponAPI.renderOffsetForDecorativeBeamWeaponsOnly
        set(p0) {
            weaponAPI.renderOffsetForDecorativeBeamWeaponsOnly = p0
        }

    var refireDelay: Float
        get() = weaponAPI.refireDelay
        set(p0) {
            weaponAPI.refireDelay = p0
        }

    fun forceShowBeamGlow() {
        weaponAPI.forceShowBeamGlow()
    }

//    OVERRIDE
//    val isInBurst: Boolean
//        get() = weaponAPI.isInBurst

    val originalSpec: WeaponSpecAPI
        get() = weaponAPI.originalSpec

    fun setWeaponGlowWidthMult(p0: Float) {
        weaponAPI.setWeaponGlowWidthMult(p0)
    }

    fun setWeaponGlowHeightMult(p0: Float) {
        weaponAPI.setWeaponGlowHeightMult(p0)
    }

    fun stopFiring() {
        weaponAPI.stopFiring()
    }

    var isForceDisabled: Boolean
        get() = weaponAPI.isForceDisabled
        set(p0) {
            weaponAPI.isForceDisabled = p0
        }

    var custom: Any?
        get() = weaponAPI.custom
        set(p0) {
            weaponAPI.custom = p0
        }

    var isForceNoFireOneFrame: Boolean
        get() = weaponAPI.isForceNoFireOneFrame
        set(p0) {
            weaponAPI.isForceNoFireOneFrame = p0
        }
}
