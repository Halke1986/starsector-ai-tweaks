package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.Target
import com.genir.aitweaks.utils.extensions.firingCycle
import com.genir.aitweaks.utils.extensions.hasBestTargetLeading
import com.genir.aitweaks.utils.extensions.timeToAttack
import com.genir.aitweaks.utils.extensions.totalRange
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.min

// TODO
/** Low priority / won't do */
// don't switch targets mid burst
// fog of war
// sometimes station bulk does get attacked

private var autofireAICount = 0

class AutofireAI(private val weapon: WeaponAPI) : AutofireAIPlugin {
    private var target: CombatEntityAPI? = null
    private var prevFrameTarget: CombatEntityAPI? = null
    private var targetTracker = ShipTargetTracker(weapon.ship)

    private var attackTime: Float = 0f
    private var idleTime: Float = 0f
    private var onTargetTime: Float = 0f

    private var selectTargetInterval = IntervalUtil(0.25F, 0.50F)
    private var shouldFireInterval = IntervalUtil(0.1F, 0.2F)

    private var shouldHoldFire: HoldFire? = HoldFire.NO_TARGET
    private var targetLocation: Vector2f? = null

    override fun advance(timeDelta: Float) {
        if (Global.getCurrentState() == GameState.CAMPAIGN) return

        trackAttackTimes(timeDelta)

        val targetDiedLastFrame = when (target) {
            null -> prevFrameTarget != null
            is ShipAPI -> !(target as ShipAPI).isAlive
            else -> !Global.getCombatEngine().isEntityInPlay(target)
        }
        prevFrameTarget = target

        // Force recalculate if target died last frame.
        if (targetDiedLastFrame) {
            selectTargetInterval.forceIntervalElapsed()
            target = null
            shouldHoldFire = HoldFire.NO_TARGET
            targetLocation = null
            attackTime = 0f
            onTargetTime = 0f
        }

        // Select target.
        selectTargetInterval.advance(timeDelta)
        if (selectTargetInterval.intervalElapsed()) {
            targetTracker.advance()
            target = SelectTarget(weapon, target, targetTracker.target, currentParams()).target
            if (target == null) {
                shouldHoldFire = HoldFire.NO_TARGET
                return
            }
        }

        targetLocation = calculateTargetLocation()

        // Calculate if weapon should fire.
        shouldFireInterval.advance(timeDelta)
        if (shouldFireInterval.intervalElapsed()) {
            shouldHoldFire = calculateShouldFire(selectTargetInterval.elapsed)
        }
    }

    override fun shouldFire(): Boolean = shouldHoldFire == null

    override fun forceOff() {
        shouldHoldFire = HoldFire.FORCE_OFF
    }

    override fun getTarget(): Vector2f? = targetLocation
    override fun getTargetShip(): ShipAPI? = target as? ShipAPI
    override fun getWeapon(): WeaponAPI = weapon
    override fun getTargetMissile(): MissileAPI? = target as? MissileAPI

    private fun trackAttackTimes(timeDelta: Float) {
        if (weapon.isFiring) {
            attackTime += timeDelta
            idleTime = 0f
        } else idleTime += timeDelta

        if (idleTime >= 3f) attackTime = 0f
    }

    private fun calculateShouldFire(timeDelta: Float): HoldFire? {
        if (target == null) return HoldFire.NO_TARGET
        if (targetLocation == null) return HoldFire.NO_HIT_EXPECTED

        // Fire only when the selected target can be hit. That way the weapon doesn't fire
        // on targets that are only briefly in the line of sight, when the weapon is turning.
        val expectedHit = target?.let { analyzeHit(weapon, target!!, currentParams()) }
        onTargetTime += timeDelta
        if (expectedHit == null) {
            onTargetTime = 0f
            return HoldFire.NO_HIT_EXPECTED
        }

        // Hold fire for a period of time after initially
        // acquiring the target to increase first volley accuracy.
        if (onTargetTime < min(2f, weapon.firingCycle.duration)) {
            val angleToTarget = VectorUtils.getFacing(targetLocation!! - weapon.location)
            val inaccuracy = abs(MathUtils.getShortestRotation(weapon.currAngle, angleToTarget))
            if (inaccuracy > 1f) return HoldFire.STABILIZE_ON_TARGET
        }

        // Check what actually will get hit, and hold fire if it's an ally or hulk.
        val actualHit = firstShipAlongLineOfFire(weapon, currentParams())
        avoidFriendlyFire(weapon, expectedHit, actualHit)?.let { return it }

        // Rest of the should-fire decisioning will be based on the actual hit.
        val hit = when {
            actualHit == null -> expectedHit
            actualHit.range > expectedHit.range -> expectedHit
            else -> actualHit
        }

        when {
            hit.shieldHit && hit.range > weapon.range -> return HoldFire.OUT_OF_RANGE
            !hit.shieldHit && hit.range > weapon.totalRange -> return HoldFire.OUT_OF_RANGE
        }

        return AttackRules(weapon, hit, currentParams()).shouldHoldFire
    }

    private fun calculateTargetLocation(): Vector2f? {
        if (target == null) return null

        val intercept = intercept(weapon, Target(target!!), currentParams()) ?: return null
        return if (weapon.slot.isTurret) intercept
        else aimHardpoint(intercept)
    }

    /** get current weapon attack parameters */
    private fun currentParams() = BallisticParams(
        getAccuracy(),
        // Provide weapon attack delay time only for turrets. It's not required for hardpoints,
        // since the ship rotating to face the target will compensate for the delay.
        if (weapon.slot.isTurret) weapon.timeToAttack else 0f,
    )

    /**
     * getAccuracy returns current weapon accuracy.
     * The value is a number in range [1.0;2.0]. 1.0 is perfect accuracy, 2.0f is the worst accuracy.
     * For worst accuracy, the weapon should aim exactly in the middle point between the target actual
     * position and calculated intercept position.
     */
    private fun getAccuracy(): Float {
        if (weapon.hasBestTargetLeading || Global.getCurrentState() == GameState.TITLE) return 1f

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
