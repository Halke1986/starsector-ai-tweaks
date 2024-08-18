package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.core.features.shipai.autofire.HoldFire.*
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.*
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.min

private var autofireAICount = 0

class AutofireAI(private val weapon: WeaponAPI) : AutofireAIPlugin {
    private val ship: ShipAPI = weapon.ship

    private var target: CombatEntityAPI? = null
    private var aimPoint: Vector2f? = null

    private var attackTime: Float = 0f
    private var idleTime: Float = 0f
    private var onTargetTime: Float = 0f
    private var isForcedOff: Boolean = false

    private var selectTargetInterval = IntervalTracker(0.25F, 0.50F)
    private var shouldFireInterval = IntervalTracker(0.1F, 0.2F)

    // Fields accessed by custom ship AI
    var intercept: Vector2f? = null // intercept may be different from aim location for hardpoint weapons
    var shouldHoldFire: HoldFire? = NO_TARGET

    private val debugIdx = autofireAICount++

    override fun advance(dt: Float) {
        when {
            // Don't operate the weapon in refit screen.
            Global.getCurrentState() == GameState.CAMPAIGN -> return
            Global.getCombatEngine().isPaused -> return
        }

        // Update time trackers.
        selectTargetInterval.advance(dt)
        shouldFireInterval.advance(dt)

        // Update target if needed.
        if (shouldUpdateTarget()) {
            selectTargetInterval.reset()
            val previousTarget = target
            target = UpdateTarget(weapon, target, ship.attackTarget, currentParams()).target

            // Target changed, do cleanup.
            if (previousTarget != target) {
                attackTime = 0f
                onTargetTime = 0f

                shouldFireInterval.forceElapsed()
            }
        }

        trackAttackTimes(dt)
        updateAimLocation()

        // Calculate if weapon should fire at current target.
        if (shouldFireInterval.intervalElapsed()) {
            shouldFireInterval.reset()
            shouldHoldFire = calculateShouldFire(dt)
        }
    }

    override fun shouldFire(): Boolean {
        val off = isForcedOff
        isForcedOff = false
        return !off && shouldHoldFire == null
    }

    override fun forceOff() {
        isForcedOff = true
    }

    override fun getTarget(): Vector2f? = aimPoint
    override fun getTargetShip(): ShipAPI? = target as? ShipAPI
    override fun getWeapon(): WeaponAPI = weapon
    override fun getTargetMissile(): MissileAPI? = target as? MissileAPI

    fun plotIntercept(target: CombatEntityAPI): Vector2f? {
        return intercept(weapon, AttackTarget(target), currentParams())
    }

    private fun trackAttackTimes(dt: Float) {
        if (weapon.isFiring) {
            attackTime += dt
            idleTime = 0f
        } else idleTime += dt

        if (idleTime >= 3f) attackTime = 0f
    }

    private fun shouldUpdateTarget(): Boolean {
        return when {
            // Target became invalid last frame, update immediately.
            // This is important for PD weapons defending against swarms of missiles.
            target?.isValidTarget == false -> {
                target = null
                true
            }

            // Target can no longer be tracked.
            target != null && !canTrack(weapon, AttackTarget(target!!), currentParams()) -> {
                target = null
                true
            }

            // Do not interrupt firing sequence.
            target != null && weapon.isInFiringSequence -> false

            // Outside above special circumstances, refresh target periodically.
            else -> selectTargetInterval.intervalElapsed()
        }
    }

    private fun calculateShouldFire(dt: Float): HoldFire? {
        if (target == null) return NO_TARGET
        if (intercept == null) return NO_HIT_EXPECTED

        // Fire only when the selected target can be hit. That way the weapon doesn't fire
        // on targets that are only briefly in the line of sight, when the weapon is turning.
        val ballisticParams = currentParams()
        val expectedHit = target?.let { analyzeHit(weapon, target!!, ballisticParams) }
        if (expectedHit == null) {
            onTargetTime = 0f
            return NO_HIT_EXPECTED
        }

        // Hold fire for a period of time after initially acquiring
        // the target to increase first volley accuracy. PD weapons
        // should fire with no delay. Use intercept point instead
        // aim point for hardpoints, to not get false accuracy because
        // of predictive aiming.
        onTargetTime += dt
        if (!weapon.isPD && onTargetTime < min(2f, weapon.firingCycle.duration)) {
            val angleToTarget = (intercept!! - weapon.location).facing
            val inaccuracy = abs(MathUtils.getShortestRotation(weapon.currAngle, angleToTarget))
            if (inaccuracy > 1f) return STABILIZE_ON_TARGET
        }

        // Check what actually will get hit, and hold fire if it's an ally or hulk.
        val actualHit = firstShipAlongLineOfFire(weapon, ballisticParams)
        avoidFriendlyFire(weapon, expectedHit, actualHit)?.let { return it }

        // Rest of the should-fire decisioning will be based on the actual hit.
        val hit = when {
            actualHit == null -> expectedHit
            actualHit.range > expectedHit.range -> expectedHit
            else -> actualHit
        }

        when {
            hit.shieldHit && hit.range > weapon.range -> return OUT_OF_RANGE
            !hit.shieldHit && hit.range > weapon.totalRange -> return OUT_OF_RANGE
        }

        return AttackRules(weapon, hit, ballisticParams).shouldHoldFire
    }

    private fun updateAimLocation() {
        intercept = null
        aimPoint = null

        if (target == null) return

        intercept = plotIntercept(target!!) ?: return
        aimPoint = if (weapon.slot.isTurret) intercept
        else aimHardpoint(intercept!!, target!!)
    }

    /** get current weapon attack parameters */
    private fun currentParams() = BallisticParams(
        getAccuracy(),
        // TODO Currently, the weapon attack delay time is disabled.
        0f,
    )

    /**
     * getAccuracy returns current weapon accuracy.
     * The value is a number in range [1.0;2.0]. 1.0 is perfect accuracy, 2.0f is the worst accuracy.
     * For worst accuracy, the weapon should aim exactly in the middle point between the target actual
     * position and calculated intercept position.
     */
    private fun getAccuracy(): Float {
        if (weapon.hasBestTargetLeading || Global.getCurrentState() == GameState.TITLE) return 1f

        val accBase = ship.aimAccuracy
        val accBonus = weapon.spec.autofireAccBonus
        return (accBase - (accBonus + attackTime / 15f)).coerceAtLeast(1f)
    }

    /** predictive aiming for hardpoints */
    private fun aimHardpoint(intercept: Vector2f, target: CombatEntityAPI): Vector2f {
        // Weapon is already facing the target. Return the
        // original intercept location to not overcompensate.
        if (vectorInArc(intercept - weapon.location, Arc(weapon.arc, weapon.absoluteArcFacing))) return intercept

        val expectedFacing = ship.customShipAI?.movement?.expectedFacing ?: (target.location - ship.location).facing
        val angleToTarget = MathUtils.getShortestRotation(ship.facing, expectedFacing)

        // Aim the hardpoint as if the ship was facing the target directly.
        return rotateAroundPivot(intercept, ship.location, angleToTarget)
    }
}
