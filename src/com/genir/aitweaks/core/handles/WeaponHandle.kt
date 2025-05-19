package com.genir.aitweaks.core.handles

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.fs.starfarer.api.loading.ProjectileSpecAPI
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI
import com.genir.aitweaks.core.shipai.autofire.AutofireAI
import com.genir.aitweaks.core.shipai.autofire.Tag
import com.genir.aitweaks.core.shipai.autofire.firingCycle
import com.genir.aitweaks.core.shipai.autofire.hasAITag
import com.genir.aitweaks.core.state.Config
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
import com.genir.starfarer.combat.entities.ship.weapons.BeamWeapon
import com.genir.starfarer.combat.systems.Weapon
import kotlin.math.floor

class WeaponHandle(weaponAPI: WeaponAPI) : Weapon by (weaponAPI as Weapon) {
    val isAntiFighter: Boolean
        get() = hasAITag(Tag.ANTI_FIGHTER) || hasAIHint(WeaponAPI.AIHints.ANTI_FTR)

    val isStrictlyAntiShield: Boolean
        get() = when {
            !Config.config.enableNeedlerFix -> false

            hasAITag(Tag.ANTI_SHIELD) -> true

            damageType == DamageType.KINETIC && usesAmmo() -> true

            else -> false
        }

    val isStrictlyAntiArmor: Boolean
        get() = when {
            hasAIHint(WeaponAPI.AIHints.USE_LESS_VS_SHIELDS) -> true

            hasAITag(Tag.USE_LESS_VS_SHIELDS) -> true

            isFinisherBeam -> true

            else -> false
        }

    val isFinisherBeam
        get() = when {
            !spec.isBeam -> false

            !hasAITag(Tag.FINISHER_BEAM) -> false

            !ship.variant.hasHullMod("aitweaks_finisher_beam_protocol") -> false

            else -> true
        }

    val isPD: Boolean
        get() = hasAIHint(WeaponAPI.AIHints.PD) || hasAIHint(WeaponAPI.AIHints.PD_ONLY)

    /** Same as WeaponAPI.isPD, except it will ignore weapons that
     * were modified into PD by S-modded Integrated Point Defense AI. */
    val isPDSpec: Boolean
        get() = spec.aiHints.contains(WeaponAPI.AIHints.PD) || spec.aiHints.contains(WeaponAPI.AIHints.PD_ONLY)

    val isMissile: Boolean
        get() = type == WeaponAPI.WeaponType.MISSILE

    val isUnguidedMissile: Boolean
        get() {
            val spec = spec.projectileSpec as? MissileSpecAPI ?: return false
            return spec.maneuverabilityDisplayName == "None"
        }

    val isPlainBeam: Boolean
        get() = isBeam && !conserveAmmo

    val conserveAmmo: Boolean
        get() = usesAmmo() || isBurstBeam || cooldown > 5f

    val hasAmmoToSpare: Boolean
        get() = !usesAmmo() || ammoTracker.let { it.ammoPerSecond > 0 && (it.ammo + it.reloadSize > it.maxAmmo) }

    val hasBestTargetLeading: Boolean
        get() = isPD && !hasAIHint(WeaponAPI.AIHints.STRIKE) && ship.mutableStats.dynamic.getValue("pd_best_target_leading", 0f) >= 1f

    val ignoresFlares: Boolean
        get() = hasAIHint(WeaponAPI.AIHints.IGNORES_FLARES) || ship.mutableStats.dynamic.getValue("pd_ignores_flares", 0f) >= 1f

    /** Is angle in weapon arc in SHIP COORDINATES. */
    fun isAngleInArc(angle: Direction): Boolean {
        return Arc.contains(angle, tolerance = 0.01f)
    }

    val isFrontFacing: Boolean
        get() = isAngleInArc(0f.direction)

    val Arc: Arc
        get() {
            val isMissileHardpoint = type == WeaponAPI.WeaponType.MISSILE && slot.isHardpoint
            val angle = if (isMissileHardpoint) 0f else arc
            return Arc(angle, arcFacing.direction)
        }

    /** weapon arc in absolute coordinates, instead of ship coordinates */
    val absoluteArc: Arc
        get() = Arc.rotated(ship.facing)

    val noFadeRange: Float
        get() = range + barrelOffset

    val totalRange: Float
        get() = noFadeRange + projectileFadeRange * 0.33f

    val autofirePlugin: AutofireAIPlugin?
        get() = ship.getWeaponGroupFor(this)?.getAutofirePlugin(this)

    val customAI: AutofireAI?
        get() = autofirePlugin as? AutofireAI

    val isBurstWeapon: Boolean
        get() = when {
            // Projectile weapons with bursts of more than one projectile.
            spec is ProjectileWeaponSpecAPI -> (spec as ProjectileWeaponSpecAPI).burstSize > 1

            else -> isBurstBeam
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
    val IsInBurst: Boolean
        get() = when {
            !isBurstWeapon -> {
                false
            }

            isBurstBeam -> {
                val state = (this as BeamWeapon).chargeTracker.beamChargeTracker_getState()
                state.name != "IDLE"
            }

            isBeam -> {
                false
            }

            else -> {
                chargeLevel == 1f
            }
        }

    /** Weapon is assumed to be in a firing sequence if it will
     * emit projectile or beam even after trigger is let go.
     * WARMUP + BURST */
    val isInFiringSequence: Boolean
        get() = isInWarmup || IsInBurst

    /** Similar to WeaponAPI.isFiring, except returns true for the entire firing cycle.
     * WeaponAPI.isFiring returns false between individual burst attacks.
     * WARMUP + BURST + COOLDOWN */
    val isInFiringCycle: Boolean
        get() = chargeLevel != 0f || cooldownRemaining != 0f

    val isIdle: Boolean
        get() = !isInFiringCycle

    val group: WeaponGroupAPI?
        get() = this.ship.getWeaponGroupFor(this)

    val target: CombatEntityAPI?
        get() = autofirePlugin?.let { it.targetShip ?: it.targetMissile } ?: ship.shipTarget

    val barrelOffset: Float
        get() {
            // Beams do have a defined barrel offset, but
            // the beam itself starts at the weapon center.
            if (isBeam) return 0f

            val offsets = if (slot.isHardpoint) spec.hardpointFireOffsets
            else spec.turretFireOffsets

            return offsets[0]?.x ?: 0f
        }

    val effectiveDPS: Float
        get() = derivedStats.dps * if (damageType == DamageType.FRAGMENTATION) 0.25f else 1f

    /** A rough estimation of maximum DPS in a two-second period. */
    val peakDPS: Float
        get() {
            val cycle = firingCycle
            val t = 2f

            val burstsNumber = when {
                // Non-burst weapons or burst weapons with short cycle.
                !isBurstWeapon || cycle.duration <= t -> {
                    floor(t / cycle.duration).coerceAtLeast(1f)
                }

                else -> {
                    (t / cycle.burstDuration).coerceAtMost(1f)
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

    /** The true projectile speed, which may differ from the value returned by vanilla WeaponAPI.projectileSpeed. */
    val trueProjectileSpeed: Float
        get() = (spec.projectileSpec as? ProjectileSpecAPI)?.getMoveSpeed(ship.mutableStats, this) ?: projectileSpeed

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
        get() = when (type) {
            WeaponAPI.WeaponType.BALLISTIC -> ship.mutableStats.ballisticRoFMult.modifiedValue
            WeaponAPI.WeaponType.ENERGY -> ship.mutableStats.energyRoFMult.modifiedValue
            WeaponAPI.WeaponType.MISSILE -> ship.mutableStats.missileRoFMult.modifiedValue
            else -> 1f
        }

    companion object {
        val WeaponAPI.handle: WeaponHandle
            get() = WeaponHandle(this)
    }
}
