package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.WrapperShipAI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.Preset
import com.genir.aitweaks.core.shipai.autofire.Hit.Type.ROTATE_BEAM
import com.genir.aitweaks.core.shipai.autofire.Hit.Type.SHIELD
import com.genir.aitweaks.core.shipai.autofire.HoldFire.*
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.Rotation.Companion.rotated
import com.genir.aitweaks.core.utils.Rotation.Companion.rotatedAroundPivot
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

open class AutofireAI(private val weapon: WeaponAPI) : AutofireAIPlugin {
    private val ship: ShipAPI = weapon.ship
    var syncFire: SyncFire = SyncFire(weapon, null)

    // Aiming data.
    protected var target: CombatEntityAPI? = null
    private var aimPoint: Vector2f? = null
    var shouldHoldFire: HoldFire? = NO_TARGET
    private val interceptTracker = InterceptTracker(weapon)

    // Timers.
    private var updateTargetInterval = IntervalUtil(0.25F, 0.50F)
    private var shouldFireInterval = IntervalUtil(0.1F, 0.2F)
    private var attackTime: Float = 0f
    private var idleTime: Float = 0f
    private var onTargetTime: Float = 0f

    private var isForcedOff: Boolean = false

    override fun advance(dt: Float) {
        when {
            // Don't operate the weapon in the refit screen.
            Global.getCurrentState() == GameState.CAMPAIGN -> return
            Global.getCombatEngine().isPaused -> return
        }

        // Advance intervals.
        updateTargetInterval.advance(dt)
        shouldFireInterval.advance(dt)

        trackAttackTimes(dt)
        updateAim(dt)
        syncFire?.advance()

        // Calculate if weapon should fire at the current target.
        if (shouldFireInterval.intervalElapsed()) {
            shouldHoldFire = calculateShouldFire()

            // Possibly update the target and calculate a new firing solution
            // in the same frame as the previous target became invalid.
            if (shouldUpdateTargetImmediately(target)) updateTarget(dt)
        }

        if (updateTargetInterval.intervalElapsed() && shouldUpdateTarget(target)) {
            updateTarget(dt)
        }
    }

    override fun shouldFire(): Boolean {
        val syncFire = syncFire
        return when {
            isForcedOff -> {
                isForcedOff = false
                false
            }

            shouldHoldFire != null -> false

            syncFire != null -> {
                syncFire.shouldFire()
            }

            else -> true
        }
    }

    override fun forceOff() {
        isForcedOff = true
    }

    override fun getTarget(): Vector2f? {
        return aimPoint?.let { it + weapon.location }
    }

    override fun getTargetShip(): ShipAPI? = target as? ShipAPI
    override fun getWeapon(): WeaponAPI = weapon
    override fun getTargetMissile(): MissileAPI? = target as? MissileAPI

    private fun trackAttackTimes(dt: Float) {
        // Update attack time, used by vanilla accuracy increase mechanism.
        if (weapon.isFiring) {
            attackTime += dt
            idleTime = 0f
        } else {
            idleTime += dt
        }

        if (idleTime >= 3f) attackTime = 0f

        // Update time on target, used by STABILIZE_ON_TARGET firing rule.
        if (shouldHoldFire == NO_TARGET || shouldHoldFire == NO_HIT_EXPECTED) onTargetTime = 0f
        else onTargetTime += dt
    }

    private fun shouldUpdateTargetImmediately(target: CombatEntityAPI?): Boolean {
        return when {
            // There is no current target.
            target == null -> false

            !target.isValidTarget -> true

            // Target can no longer be tracked. This does not apply to hardpoint weapons tracking ship attack target.
            (target != ship.attackTarget || weapon.slot.isTurret) && !canTrack(weapon, BallisticTarget.entity(target), currentParams()) -> true

            else -> false
        }
    }

    private fun shouldUpdateTarget(target: CombatEntityAPI?): Boolean {
        return when {
            target == null -> true

            // Do not interrupt firing sequence.
            weapon.isInFiringSequence -> false

            // Outside above special circumstances, refresh target periodically.
            else -> true
        }
    }

    private fun updateTarget(dt: Float) {
        val previousTarget = target
        target = UpdateTarget(weapon, target, ship.attackTarget, currentParams()).target()

        // Nothing changed, return early.
        if (target == previousTarget) return

        // Cleanup
        attackTime = 0f
        onTargetTime = 0f
        interceptTracker.clear()

        trackAttackTimes(dt)
        updateAim(dt)

        shouldHoldFire = calculateShouldFire()
    }

    protected open fun calculateShouldFire(): HoldFire? {
        val target: CombatEntityAPI = target ?: return NO_TARGET
        if (!target.isValidTarget) return NO_TARGET

        holdFireIfOverfluxed()?.let { return it }
        stabilizeOnTarget()?.let { return it }

        // Fire only when the selected target can be hit. That way the weapon doesn't fire
        // on targets that are only briefly in the line of sight, when the weapon is turning.
        val ballisticParams = currentParams()
        var expectedHit = analyzeHit(weapon, target, ballisticParams)

        // Mock an expected hit for beams that should keep firing when in transition between targets.
        if (expectedHit == null && shouldHoldBeam(target)) {
            val range = intercept(weapon, BallisticTarget.entity(target), currentParams()).length
            expectedHit = Hit(target, range, ROTATE_BEAM)
        }

        if (expectedHit == null) return NO_HIT_EXPECTED

        // Check what actually will get hit, and hold fire if it's an ally or hulk.
        val actualHit = firstShipAlongLineOfFire(weapon, target, ballisticParams)

        // Rest of the should-fire decisioning will be based on the actual hit.
        val hit = when {
            actualHit == null -> expectedHit
            actualHit.range > expectedHit.range -> expectedHit
            else -> actualHit
        }

        avoidFriendlyFire(weapon, expectedHit, actualHit)?.let { return it }

        when {
            hit.type == SHIELD && hit.range > weapon.range -> return OUT_OF_RANGE

            hit.type != SHIELD && hit.range > weapon.totalRange -> return OUT_OF_RANGE
        }

        return AttackRules(weapon, hit, ballisticParams).shouldHoldFire
    }

    protected fun holdFireIfOverfluxed(): HoldFire? {
        return when {
            // Ships with no shields don't need to preserve flux.
            ship.shield == null -> null

            weapon.isPD -> null

            weapon.fluxCostToFire == 0f -> null

            weapon.ship.isUnderManualControl -> null

            // Ship will be overfluxed after the attack.
            ship.fluxTracker.currFlux + weapon.fluxCostToFire >= ship.fluxTracker.maxFlux * Preset.holdFireThreshold -> SAVE_FLUX

            else -> null
        }
    }

    /** Hold fire for a period of time after initially acquiring
     * the target to increase first volley accuracy. */
    private fun stabilizeOnTarget(): HoldFire? {
        when {
            // PD weapons should fire with no delay.
            weapon.isPD || target!!.isPDTarget -> return null

            // Normal beams are accurate enough to fire with no delay.
            weapon.isPlainBeam -> return null

            // Weapon is on target for long enough already.
            onTargetTime >= min(2f, weapon.firingCycle.duration) -> return null
        }

        val arc = interceptArc(weapon, BallisticTarget.entity(target!!), currentParams())
        val inaccuracy = absShortestRotation(weapon.currAngle, arc.facing)
        if (inaccuracy * 4f > arc.angle) return STABILIZE_ON_TARGET

        return null
    }

    /** get current weapon attack parameters */
    protected fun currentParams() = BallisticParams(
        getAccuracy(),
        // TODO Currently, the weapon attack delay time is disabled.
        0f,
    )

    /** Given the finite speed of beam weapons, when the weapon is off target,
     * it may be more effective to rotate the active beam towards the target
     * rather than turning it off and reactivating it after realignment. */
    private fun shouldHoldBeam(target: CombatEntityAPI): Boolean {
        when {
            !weapon.isPlainBeam -> return false

            !weapon.isFiring -> return false

            weapon.slot.isHardpoint -> return false
        }

        val r = shortestRotation(weapon.currAngle, (target.location - weapon.location).facing)
        val w = weapon.turnRate - interceptTracker.interceptVelocity * r.sign

        // The beam's turn rate is too low to track the target.
        if (w < 0) return false

        // Decide if it's faster to rotate the beam to target
        // or start a new beam and let it reach the target.
        val toTarget = (target.location - weapon.location)
        val turnRateMultiplier = 5f // vanilla turn rate multiplier for non-firing weapons
        val wFast = weapon.turnRate * turnRateMultiplier - interceptTracker.interceptVelocity * r.sign
        val rotationTime = (abs(r) / w)
        val rotationTimeFast = (abs(r) / wFast)
        val flightTime = rotationTimeFast + toTarget.length / (weapon.spec as BeamWeaponSpecAPI).beamSpeed

        // Favor beam rotation, just because it looks cool.
        val flightTimeMultiplier = 1.3f
        return rotationTime < flightTime * flightTimeMultiplier
    }

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

    private fun updateAim(dt: Float) {
        aimPoint = null

        val target = this.target ?: return
        val intercept = intercept(weapon, BallisticTarget.entity(target), currentParams())

        interceptTracker.advance(dt, intercept)

        aimPoint = when {
            // Can't aim fixed weapons.
            weapon.slot.arc == 0f -> null

            weapon.slot.isHardpoint -> aimHardpoint(target, intercept)

            else -> aimTurret(dt, intercept)
        }
    }

    private fun aimTurret(dt: Float, intercept: Vector2f): Vector2f {
        // Beam weapons in turrets can be aimed directly at the target location.
        if (weapon.isBeam) return intercept

        // Combat engine fires weapons before setting their aim location. This means
        // the aim location returned by this method will take effect the next frame.
        // Therefore, the weapon angle needs to be adjusted for slot angular velocity
        // towards target.
        val r = Rotation(interceptTracker.interceptVelocity * dt)
        return intercept.rotated(r)
    }

    /** predictive aiming for hardpoints */
    private fun aimHardpoint(target: CombatEntityAPI, intercept: Vector2f): Vector2f {
        // Try to read expected facing from custom AI implementations.
        val customAIFacing = ship.customShipAI?.movement?.expectedFacing
        val wrapperAIFacing = (ship.ai as? WrapperShipAI)?.expectedFacing
        val expectedFacing = customAIFacing ?: wrapperAIFacing ?: (target.location - ship.location).facing

        // If no expected facing was found, assume the ship is controlled by vanilla AI.
        // Vanilla AI lacks precise aiming, so hardpoints need flexibility to compensate.
        // Aim directly at the intercept point when the ship is close to aligned.
        if (customAIFacing == null && wrapperAIFacing == null) {
            if (Arc(weapon.arc, weapon.absoluteArcFacing).contains(intercept))
                return intercept
        }

        // Aim the hardpoint as if the ship was facing the target directly.
        val r = Rotation(shortestRotation(expectedFacing, ship.facing))
        val v = target.timeAdjustedVelocity.rotated(r)
        val p = target.location.rotatedAroundPivot(r, weapon.ship.location)
        return intercept(weapon, BallisticTarget(p, v, target.collisionRadius), currentParams())
    }

    /** Tracks intercept angular velocity and angular distance in weapon slot frame of reference. */
    private class InterceptTracker(private val weapon: WeaponAPI) {
        private var prevAngleToIntercept: Float? = null
        var interceptVelocity = 0f

        fun advance(dt: Float, intercept: Vector2f?) {
            if (intercept == null) return

            val angleToIntercept = shortestRotation(weapon.absoluteArcFacing, intercept.facing)
            interceptVelocity = shortestRotation(prevAngleToIntercept ?: angleToIntercept, angleToIntercept) / dt
            prevAngleToIntercept = angleToIntercept
        }

        /** clear should be called after target change,
         * to avoid false intercept velocity estimation. */
        fun clear() {
            prevAngleToIntercept = null
            interceptVelocity = 0f
        }
    }
}
