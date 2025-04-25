package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.ExtendedShipAI
import com.genir.aitweaks.core.shipai.Preset
import com.genir.aitweaks.core.shipai.autofire.Hit.Type.ROTATE_BEAM
import com.genir.aitweaks.core.shipai.autofire.Hit.Type.SHIELD
import com.genir.aitweaks.core.shipai.autofire.HoldFire.*
import com.genir.aitweaks.core.shipai.autofire.UpdateTarget.Companion.TARGET_SEARCH_MULT
import com.genir.aitweaks.core.state.Config.Companion.config
import com.genir.aitweaks.core.utils.firstShipAlongLineOfFire
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
import com.genir.aitweaks.core.utils.types.RotationMatrix
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotated
import org.lwjgl.util.vector.Vector2f
import kotlin.math.min

open class AutofireAI(private val weapon: WeaponAPI) : AutofireAIPlugin {
    private val ship: ShipAPI = weapon.ship
    var syncFire: SyncFire = SyncFire(weapon, null)
    val reloadTracker: ReloadTracker = ReloadTracker(weapon)

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

//        debug()

        // Advance intervals.
        updateTargetInterval.advance(dt)
        shouldFireInterval.advance(dt)

        trackAttackTimes(dt)
        updateAim(dt)
        syncFire.advance()
        reloadTracker.advance()

        val updateTargetImmediately = shouldUpdateTargetImmediately(target)
        val updateTargetInterval = updateTargetInterval.intervalElapsed() && shouldUpdateTarget(target)

        val updateShouldFireImmediately = updateShouldFireImmediately(target)
        val updateShouldFireInterval = shouldFireInterval.intervalElapsed()

        if (updateTargetImmediately || updateTargetInterval) {
            updateTarget(dt)
        }

        if (updateTargetImmediately || updateTargetInterval || updateShouldFireImmediately || updateShouldFireInterval) {
            shouldHoldFire = calculateShouldFire()
        }
    }

    fun debug() {
        if (weapon.ship.owner != 0) {
            return
        }

        Debug.print[weapon] = "${weapon.Id}  $shouldHoldFire"
    }

    override fun shouldFire(): Boolean {
        val syncFire = syncFire
        return when {
            isForcedOff -> {
                isForcedOff = false
                false
            }

            shouldHoldFire != null -> false

            else -> syncFire.shouldFire()
        }
    }

    override fun forceOff() {
        isForcedOff = true
    }

    override fun getTarget(): Vector2f? {
        return aimPoint?.let { it + weapon.location }
    }

    override fun getWeapon(): WeaponAPI {
        return weapon
    }

    override fun getTargetShip(): ShipAPI? {
        return target as? ShipAPI
    }

    override fun getTargetMissile(): MissileAPI? {
        return target as? MissileAPI
    }

    private fun trackAttackTimes(dt: Float) {
        // Update attack time, used by vanilla accuracy increase mechanism.
        if (weapon.isFiring) {
            attackTime += dt
            idleTime = 0f
        } else {
            idleTime += dt
        }

        if (idleTime >= 3f) {
            attackTime = 0f
        }

        // Update time on target, used by STABILIZE_ON_TARGET firing rule.
        if (shouldHoldFire == NO_TARGET || shouldHoldFire == NO_HIT_EXPECTED) onTargetTime = 0f
        else onTargetTime += dt
    }

    /** Switching targets immediately after one becomes invalid, instead of waiting
     * for the update interval, greatly improves PD weapon effectiveness. */
    private fun shouldUpdateTargetImmediately(target: CombatEntityAPI?): Boolean {
        return when {
            // Leave the case of no target to slow update interval.
            target == null -> false

            // Target was destroyed the previous frame.
            !target.isValidTarget -> true

            // Next step does not apply to hardpoint weapons tracking ship attack target.
            target == ship.attackTarget && weapon.slot.isHardpoint -> false

            // Weapon can no longer track the target. Use double the weapon.totalRange to allow
            // tracking potential targets when there are no targets within the actual firing range.
            !canTrack(weapon, BallisticTarget.collisionRadius(target), currentParams(), weapon.totalRange * TARGET_SEARCH_MULT) -> true

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

    /** For PD weapons, it is important to open fire immediately
     * after acquiring a target. Any delay—even tenths of a second—can
     * reduce effectiveness against swarms of low HP threats. */
    private fun updateShouldFireImmediately(target: CombatEntityAPI?): Boolean {
        return when {
            target?.isPDTarget != true -> false

            !weapon.isPD -> false

            shouldHoldFire == NO_HIT_EXPECTED -> true

            shouldHoldFire == OUT_OF_RANGE -> true

            else -> false
        }
    }

    private fun updateTarget(dt: Float) {
        val previousTarget = target
        target = UpdateTarget(weapon, target, ship.attackTarget, currentParams()).target()

        // Nothing changed, return early.
        if (target == previousTarget) {
            return
        }

        // Cleanup
        attackTime = 0f
        onTargetTime = 0f
        interceptTracker.clear()

        trackAttackTimes(dt)
        updateAim(dt)
    }

    protected open fun calculateShouldFire(): HoldFire? {
        val target = target
        if (target == null || !target.isValidTarget) {
            return NO_TARGET
        }

        holdFireIfOverfluxed(target)?.let { reason ->
            return reason
        }

        stabilizeOnTarget()?.let { reason ->
            return reason
        }

        // Fire only when the selected target can be hit. That way the weapon doesn't fire
        // on targets that are only briefly in the line of sight, when the weapon is turning.
        val ballisticParams = currentParams()
        var expectedHit = analyzeHit(weapon, target, ballisticParams)

        // Mock an expected hit for beams that should keep firing when in transition between targets.
        if (expectedHit == null && shouldSweepBeam(target)) {
            val range = intercept(weapon, BallisticTarget.collisionRadius(target), currentParams()).length
            expectedHit = Hit(target, range, ROTATE_BEAM)
        }

        if (expectedHit == null) {
            return NO_HIT_EXPECTED
        }

        when {
            (expectedHit.type == SHIELD || weapon.conserveAmmo) && expectedHit.range > weapon.Range -> {
                return OUT_OF_RANGE
            }

            expectedHit.type != SHIELD && expectedHit.range > weapon.totalRange -> {
                return OUT_OF_RANGE
            }
        }

        // Check what actually will get hit, and hold fire if it's an ally or hulk.
        val actualHit = firstShipAlongLineOfFire(weapon, target, ballisticParams)

        avoidFriendlyFire(weapon, expectedHit, actualHit)?.let { reason ->
            return reason
        }

        // Rest of the should-fire decisioning will be based on the actual hit.
        val hit = when {
            actualHit == null -> expectedHit

            // For PD targets, the actual hit may be detected at a longer range than
            // the initially expected hit, since the projectile is assumed likely to miss.
            actualHit.range > expectedHit.range -> expectedHit

            else -> actualHit
        }

        return AttackRules(weapon, hit, ballisticParams).shouldHoldFire
    }

    protected fun holdFireIfOverfluxed(target: CombatEntityAPI): HoldFire? {
        return when {
            // Ships with no shields don't need to preserve flux.
            ship.shield == null -> null

            weapon.isPD -> null

            weapon.ship.isUnderManualControl -> null

            shouldFinishTarget(target) -> null

            // Ship will be overfluxed after the attack.
            ship.fluxTracker.currFlux + weapon.fluxCostToFire >= ship.fluxTracker.maxFlux * Preset.holdFireThreshold -> SAVE_FLUX

            else -> null
        }
    }

    /** Should the weapon try to kill ship target even if the ship is overfluxed.  */
    private fun shouldFinishTarget(target: CombatEntityAPI): Boolean {
        // Don't try to decipher vanilla AI behavior.
        val ai: CustomShipAI = ship.customShipAI ?: return false

        // Weapon is not attacking the ship target.
        if (target != ai.attackTarget) {
            return false
        }

        return ai.ventModule.shouldFinishTarget
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

        val arc = interceptArc(weapon, BallisticTarget.collisionRadius(target!!), currentParams())
        val inaccuracy = (arc.facing - weapon.currAngle.direction).length
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
    private fun shouldSweepBeam(target: CombatEntityAPI): Boolean {
        when {
            !config.enableBeamSweep -> return false

            !weapon.isPlainBeam -> return false

            !weapon.isInFiringCycle -> return false

            weapon.slot.isHardpoint -> return false

            // Check if center of the target is in the firing arc. This prevents the beam getting stuck
            // in a perpetual sweep, when target shield radius is in the firing arc, but target shields
            // are down. The check shouldn't interfere with PD fire, since missiles and fighters are
            // point-like targets.
            !weapon.absoluteArc.contains(target.location - weapon.location) -> return false
        }

        val r = (target.location - weapon.location).facing - weapon.currAngle.direction
        val w = weapon.turnRateWhileFiring - interceptTracker.interceptVelocity * r.sign

        // The beam's turn rate is too low to track the target.
        if (w < 0) {
            return false
        }

        // Decide if it's faster to rotate the beam to target
        // or start a new beam and let it reach the target.
        val toTarget = (target.location - weapon.location)
        val wFast = weapon.turnRateWhileIdle - interceptTracker.interceptVelocity * r.sign
        val rotationTime = (r.length / w)
        val rotationTimeFast = (r.length / wFast)
        val realignmentTime = rotationTimeFast + toTarget.length / (weapon.spec as BeamWeaponSpecAPI).beamSpeed

        // Favor beam rotation, just because it looks cool.
        val flightTimeMultiplier = 1.3f
        return rotationTime < realignmentTime * flightTimeMultiplier
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
        val intercept = intercept(weapon, BallisticTarget.collisionRadius(target), currentParams())

        interceptTracker.advance(dt, intercept)

        aimPoint = when {
            // Can't aim fixed weapons.
            weapon.slot.arc == 0f -> null

            weapon.slot.isHardpoint -> aimHardpoint(target, intercept)

            // If the turret's rotation rate is slower than the ship's turn rate, aim
            // the weapon with the entire ship, similar to hardpoints. Otherwise, the
            // ship rotation could bring the weapon off target. If the weapon is attacking
            // a target other than the ship target, allow it to behave as a turret.
            weapon.isSlowTurret && target == ship.attackTarget -> aimHardpoint(target, intercept)

            else -> aimTurret(dt, intercept)
        }
    }

    private fun aimTurret(dt: Float, intercept: Vector2f): Vector2f {
        // Beam weapons in turrets can be aimed directly at the target location.
        if (weapon.isBeam) {
            return intercept
        }

        // Combat engine fires weapons before setting their aim location. This means
        // the aim location returned by this method will take effect the next frame.
        // Therefore, the weapon angle needs to be adjusted for slot angular velocity
        // towards target.
        val r = RotationMatrix(interceptTracker.interceptVelocity * dt)
        return intercept.rotated(r)
    }

    /** predictive aiming for hardpoints */
    private fun aimHardpoint(target: CombatEntityAPI, intercept: Vector2f): Vector2f {
        // Try to read expected facing from custom AI implementations.
        val customAIFacing = ship.customShipAI?.movement?.expectedFacing
        val wrapperAIFacing = (ship.ai as? ExtendedShipAI)?.expectedFacing
        val expectedFacing: Direction = customAIFacing ?: wrapperAIFacing ?: (target.location - ship.location).facing

        // If no expected facing was found, assume the ship is controlled by vanilla AI.
        // Vanilla AI lacks precise aiming, so hardpoints need flexibility to compensate.
        // Aim directly at the intercept point when the ship is close to aligned.
        if (customAIFacing == null && wrapperAIFacing == null) {
            if (weapon.absoluteArc.contains(intercept)) {
                return intercept
            }
        }

        // Aim the hardpoint as if the ship was already rotated to the expected facing.
        // That way the correct weapon facing can be predicted.
        val facingStash = ship.facing
        val correction = (ship.facing.direction - expectedFacing).rotationMatrix
        try {
            ship.facing = expectedFacing.degrees
            return intercept(weapon, BallisticTarget.collisionRadius(target), currentParams()).rotated(correction)
        } finally {
            ship.facing = facingStash
        }
    }

    /** Tracks intercept angular velocity and angular distance in weapon slot frame of reference. */
    private class InterceptTracker(private val weapon: WeaponAPI) {
        private var prevAngleToIntercept: Direction? = null
        var interceptVelocity = 0f

        fun advance(dt: Float, intercept: Vector2f?) {
            if (intercept == null) {
                return
            }

            val angleToIntercept: Direction = intercept.facing - weapon.absoluteArc.facing
            interceptVelocity = (angleToIntercept - (prevAngleToIntercept ?: angleToIntercept)).degrees / dt
            prevAngleToIntercept = angleToIntercept
        }

        /** clear should be called after target change,
         * to avoid false intercept velocity estimation. */
        fun clear() {
            prevAngleToIntercept = null
            interceptVelocity = 0f
        }
    }

    private val WeaponAPI.isSlowTurret: Boolean
        get() = turnRateWhileFiring < ship.baseTurnRate
}
