package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.utils.extensions.hasBestTargetLeading
import com.genir.aitweaks.utils.extensions.maneuverTarget
import com.genir.aitweaks.utils.rotateAroundPivot
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

// TODO
// take high-tech station shield into account
// check normal station shield detection

// no_aitweaks

// don't switch targets mid burst
// paladin ff
// track ship target for player

/** Low priority / won't do */
// fog
// target selection
// STRIKE never targets fighters ??

var count = 0

class AutofireAI(private val weapon: WeaponAPI) : AutofireAIPlugin {
    private var target: CombatEntityAPI? = null
    private var maneuverTarget: ShipAPI? = null
    private var prevTarget: CombatEntityAPI? = null

    private var attackTime: Float = 0f
    private var idleTime: Float = 0f

    private var selectTargetInterval = IntervalUtil(0.25F, 0.5F);

    private var idx = 0

    init {
        if (weapon.spec.weaponId == "hil") {
            count++
            idx = count
        }
    }

    override fun advance(timeDelta: Float) {
        trackManeuverTarget()
        trackTimes(timeDelta)
        selectTargetInterval.advance(timeDelta)

        if (selectTargetInterval.intervalElapsed()) target = selectTarget(weapon, target, maneuverTarget)
    }

    override fun shouldFire(): Boolean {
//        debugPlugin[idx] = "-"

        if (target == null || Global.getCurrentState() != GameState.COMBAT) return holdFire

        // Fire only when the selected target is in range.
        val expectedHit = analyzeHit(weapon, target!!) ?: return holdFire
        if (expectedHit.range > weapon.range) return holdFire

        val actualHit = closestEntityFinder2(weapon, weapon.range)// ?: return fire

        if (weapon.spec.weaponId == "hil") {
//            debugPlugin[idx] = "$expectedHit $actualHit"
        }
        if (actualHit == null) {
            return fire
        }

        if (!avoidFriendlyFire1(weapon, expectedHit, actualHit)) return holdFire

        val hit = if (actualHit.range < expectedHit.range) actualHit else expectedHit

        return when {
            !avoidPhased(weapon, hit) -> holdFire
            !avoidShields(weapon, hit) -> holdFire
            !avoidExposedHull(weapon, hit) -> holdFire
            //!avoidWastingTorpedo(weapon, target!!, willHitShield) -> holdFire
//            !avoidFriendlyFire(weapon, target!!, range) -> holdFire
            else -> fire
        }
    }

    override fun forceOff() {
        target = null
    }

    override fun getTarget(): Vector2f? {
        if (target == null) return null

        val intercept = intercept(weapon, Target(target!!), getAccuracy()) ?: return null

        return if (weapon.slot.isTurret) intercept
        else aimHardpoint(intercept)
    }

    override fun getTargetShip(): ShipAPI? = target as? ShipAPI
    override fun getWeapon(): WeaponAPI = weapon
    override fun getTargetMissile(): MissileAPI? = target as? MissileAPI

    private fun trackManeuverTarget() {
        val newTarget = weapon.ship.maneuverTarget
        if (newTarget != null || maneuverTarget?.isAlive != true) maneuverTarget = newTarget
    }

    private fun trackTimes(timeDelta: Float) {
        val currentTarget = target
        if (currentTarget != null && prevTarget != currentTarget) {
            prevTarget = currentTarget
            attackTime = 0f
        }

        if (weapon.isFiring) {
            attackTime += timeDelta
            idleTime = 0f
        } else idleTime += timeDelta

        if (idleTime >= 3f) attackTime = 0f
    }

    /**
     * getAccuracy returns current weapon accuracy.
     * The value is a number in range [1.0;2.0]. 1.0 is perfect accuracy, 2.0f is the worst accuracy.
     * For worst accuracy, the weapon should aim exactly in the middle point between the target actual
     * position and calculated intercept position.
     */
    private fun getAccuracy(): Float {
        if (weapon.hasBestTargetLeading) return 1f

        val accBase = weapon.ship.aimAccuracy
        val accBonus = weapon.spec.autofireAccBonus
        return (accBase - (accBonus + attackTime / 15f)).coerceAtLeast(1f)
    }

    /** predictive aiming for hardpoints */
    private fun aimHardpoint(intercept: Vector2f): Vector2f {
        val tgtLocation = target!!.location - weapon.ship.location
        val tgtFacing = VectorUtils.getFacing(tgtLocation)
        val angleToTarget = MathUtils.getShortestRotation(tgtFacing, weapon.ship.facing)

        // Ship is already facing the target. Return the
        // original target location to not overcompensate.
        if (abs(angleToTarget) < weapon.arc / 2f) {
            return intercept
        }

        return rotateAroundPivot(intercept, weapon.ship.location, angleToTarget)
    }
}

/** Analyzes the potential collision between projectile and target. Returns collision range.
 * Boolean return parameter is true if projectile will hit target shield; always false for
 * fighters and missiles. Null if no collision. */
fun analyzeHit(weapon: WeaponAPI, target: CombatEntityAPI): Hit? {
    // Simple circumference collision is enough for missiles and fighters.
    if (target !is ShipAPI || target.isFighter) {
        val range = willHitCircumference(weapon, Target(target)) ?: return null
        return Hit(target, range, false)
    }

    willHitShield(weapon, target)?.let { return Hit(target, it, true) }
    willHitBounds(weapon, target)?.let { return Hit(target, it, false) } ?: return null
}