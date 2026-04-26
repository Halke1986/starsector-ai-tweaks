package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.ExtendedShipAI
import com.genir.aitweaks.core.shipai.Preset
import com.genir.aitweaks.core.shipai.autofire.HoldFire.*
import com.genir.aitweaks.core.shipai.autofire.SelectTarget.Companion.TARGET_SEARCH_MULT
import com.genir.aitweaks.core.shipai.autofire.ballistics.*
import com.genir.aitweaks.core.shipai.autofire.ballistics.Hit.Type.ROTATE_BEAM
import com.genir.aitweaks.core.shipai.autofire.ballistics.Hit.Type.SHIELD
import com.genir.aitweaks.core.shipai.global.TargetTracker
import com.genir.aitweaks.core.state.Config.Companion.config
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection
import com.genir.aitweaks.core.utils.types.RotationMatrix
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotated
import org.lwjgl.util.vector.Vector2f

open class AutofireAI(val weapon: WeaponHandle, val targetTracker: TargetTracker) : AutofireAIPlugin {
    private val ship: ShipAPI = weapon.ship
    var syncFire: SyncFire = SyncFire(weapon, null)
    val reloadTracker: ReloadTracker = ReloadTracker(weapon)

    // Aiming data.
    protected var target: CombatEntityAPI? = null
    private var aimPoint: Vector2f? = null
    var shouldHoldFire: HoldFire = NO_TARGET
    private val interceptTracker = InterceptTracker(weapon)

    // Timers.
    private var updateTargetInterval = IntervalUtil(0.25F, 0.50F)
    private var shouldFireInterval = IntervalUtil(0.1F, 0.2F)
    var attackTime: Float = 0f
        private set
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

//        debug()
    }

    fun debug() {
        if (weapon.ship.owner != 0) {
            return
        }

        Debug.print[weapon] = "${weapon.id}  $shouldHoldFire"
    }

    override fun shouldFire(): Boolean {
        val syncFire = syncFire
        return when {
            isForcedOff -> {
                isForcedOff = false
                false
            }

            shouldHoldFire != FIRE -> false

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
        return weapon.weaponAPI
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
            target == null -> {
                false
            }

            // Target was destroyed the previous frame.
            !target.isValidTarget -> {
                true
            }

            // Next step does not apply to hardpoint weapons tracking ship attack target.
            target == ship.attackTarget && weapon.slot.isHardpoint -> {
                false
            }

            // Weapon can no longer track the target. Use double the weapon.totalRange to allow
            // tracking potential targets when there are no targets within the actual firing range.
            !weapon.ballistics.canEngage(BallisticTarget.collisionRadius(target), weapon.currentBallisticsParams, weapon.engagementRange * TARGET_SEARCH_MULT) -> {
                true
            }

            else -> {
                false
            }
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
        target = SelectTarget(weapon, target, ship.attackTarget, weapon.currentBallisticsParams, targetTracker).target()

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

    protected open fun calculateShouldFire(): HoldFire {
        val prevShouldHoldFire = this.shouldHoldFire
        val shouldHoldFire = calculateShouldFireInner()

        if (prevShouldHoldFire == FIRE && shouldHoldFire != FIRE && jitterBeamShutdown()) {
            return FIRE
        }

        return shouldHoldFire
    }

    private fun calculateShouldFireInner(): HoldFire {
        val target = target
        if (target == null || !target.isValidTarget) {
            return NO_TARGET
        }

        val reasonOverfluxed = holdFireIfOverfluxed(target)
        if (reasonOverfluxed != FIRE) {
            return reasonOverfluxed
        }

        // Fire only when the selected target can be hit. That way the weapon doesn't fire
        // on targets that are only briefly in the line of sight, when the weapon is turning.
        val ballisticParams = weapon.currentBallisticsParams
        var expectedHit: Hit? = analyzeHit(weapon, target, ballisticParams)

        // Mock an expected hit for beams that should keep firing when in transition between targets.
        if (expectedHit == null && shouldSweepBeam(target)) {
            val range = weapon.ballistics.intercept(BallisticTarget.collisionRadius(target), ballisticParams).length
            expectedHit = Hit(target, range, ROTATE_BEAM)
        }

        if (expectedHit == null) {
            return NO_HIT_EXPECTED
        }

        if (!isHitInRange(expectedHit)) {
            return OUT_OF_RANGE
        }

        val reasonStabilize = stabilizeOnTarget()
        if (reasonStabilize != FIRE) {
            return reasonStabilize
        }

        // Check what actually will get hit, and hold fire if it's an ally or hulk.
        val actualHit = firstShipAlongLineOfFire(weapon, target, ballisticParams)
        val reasonAvoidFF = avoidFriendlyFire(weapon, expectedHit, actualHit)
        if (reasonAvoidFF != FIRE) {
            return reasonAvoidFF
        }

        // Rest of the should-fire decisioning will be based on the actual hit.
        val hit = when {
            actualHit == null -> expectedHit

            // For PD targets, the actual hit may be detected at a longer range than
            // the initially expected hit, since the projectile is assumed likely to miss.
            actualHit.range > expectedHit.range -> expectedHit

            else -> actualHit
        }

        return AttackRules(weapon, hit, ballisticParams).shouldHoldFire()
    }

    /** PD beam weapons are updated each frame, which can cause many beams to shut down
     * simultaneously, appearing abrupt and unnatural. This method extends beam firing
     * by a short random duration to ensure a more gradual cessation.*/
    private fun jitterBeamShutdown(): Boolean {
        return when {
            // Jitter applies only to normal beams.
            !weapon.isPlainBeam -> false

            // Only PD weapons are updated each frame.
            !weapon.isPD -> false

            // Allow the beam to shut down eventually.
            updateTargetInterval.intervalElapsed() -> false

            else -> true
        }
    }

    protected fun holdFireIfOverfluxed(target: CombatEntityAPI): HoldFire {
        return when {
            // Ships with no shields don't need to preserve flux.
            !ship.hasShield -> FIRE

            weapon.isPD -> FIRE

            weapon.ship.isUnderManualControl -> FIRE

            shouldFinishTarget(target) -> FIRE

            // Ship will be overfluxed after the attack.
            ship.fluxTracker.currFlux + weapon.fluxCostToFire >= ship.fluxTracker.maxFlux * Preset.holdFireThreshold -> {
                SAVE_FLUX
            }

            else -> FIRE
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
    private fun stabilizeOnTarget(): HoldFire {
        when {
            // PD weapons should fire with no delay.
            weapon.isPD || target!!.isPDTarget -> return FIRE

            // Normal beams are accurate enough to fire with no delay.
            weapon.isPlainBeam -> return FIRE

            // Weapon is on target for long enough already.
            onTargetTime >= minOf(2f, weapon.firingCycle.duration) -> return FIRE
        }

        val arc = weapon.ballistics.interceptArc(BallisticTarget.collisionRadius(target!!), weapon.currentBallisticsParams)
        val inaccuracy = (arc.facing - weapon.currAngle.toDirection).length
        if (inaccuracy * 4f > arc.angle) {
            return STABILIZE_ON_TARGET
        }

        return FIRE
    }

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

        val r = (target.location - weapon.location).facing - weapon.currAngle.toDirection
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

    private fun isHitInRange(hit: Hit): Boolean {
        return when {
            weapon.conserveAmmo -> {
                hit.range <= weapon.range
            }

            // Do not attack shields from beyond weapon range,
            // as this deals only soft flux damage.
            hit.type == SHIELD -> {
                hit.range <= weapon.range
            }

            // Start PD fire when missile enters the very fringe of weapon range.
            hit.target.isMissile && weapon.isPD -> {
                hit.range <= weapon.range + weapon.projectileFadeRange * 0.75f
            }

            else -> {
                hit.range <= weapon.engagementRange
            }
        }
    }

    private fun updateAim(dt: Float) {
        aimPoint = null

        val target = this.target ?: return
        val intercept = weapon.ballistics.intercept(BallisticTarget.collisionRadius(target), weapon.currentBallisticsParams)

        interceptTracker.advance(dt, intercept)

        aimPoint = when {
            // Can't aim fixed weapons.
            weapon.slot.arc == 0f -> null

            // If the turret's rotation rate is slower than the ship's turn rate, aim
            // the weapon with the entire ship, similar to hardpoints. Otherwise, the
            // ship rotation could bring the weapon off target. If the weapon is attacking
            // a target other than the ship target, allow it to behave as a turret.
            weapon.isSlowRotating && target == ship.attackTarget -> aimHardpoint(target, intercept)

            else -> aimTurret(dt, intercept)
        }
    }

    private fun aimTurret(dt: Float, intercept: Vector2f): Vector2f {
        // Combat engine updated beam weapon aim location before firing them.
        // Therefore, beam weapons in turrets can be aimed directly at the
        // target location.
        if (weapon.isBeam) {
            return intercept
        }

        // As opposed to beam weapons, combat engine fires projectile weapons before
        // updating their aim location. This means the aim location returned by this
        // method will take effect the next frame. Therefore, the weapon angle needs
        // to be adjusted for slot angular velocity towards target.
        val r = RotationMatrix(interceptTracker.interceptVelocity * dt)
        return intercept.rotated(r)
    }

    /** predictive aiming for hardpoints */
    private fun aimHardpoint(target: CombatEntityAPI, intercept: Vector2f): Vector2f {
        // Try to read expected facing from custom AI implementations.
        val customAIFacing = ship.customShipAI?.maneuver?.expectedFacing
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
        val actualFacing = ship.facing
        val toActualFacing = ship.facing.toDirection - expectedFacing
        try {
            ship.facing = expectedFacing.degrees
            val ballisticTarget: BallisticTarget = BallisticTarget.collisionRadius(target)
            val allowedArc: Arc = weapon.ballistics.interceptArc(ballisticTarget, weapon.currentBallisticsParams).rotated(toActualFacing.degrees)

            // Allow limited weapon freedom, constrained to the arc the target will
            // occupy once the ship reaches its expected facing. This speeds up target
            // acquisition without leaving the weapon misaligned after the ship finishes
            // rotating.
            return allowedArc.coerceVector(intercept)
        } finally {
            ship.facing = actualFacing
        }
    }

    fun firstShipAlongLineOfFire(weapon: WeaponHandle, target: CombatEntityAPI, params: BallisticParams): Hit? {
        val range: Float = weapon.engagementRange

        val obstacles = targetTracker.getObstacles().filter { ship ->
            when {
                ship.root == weapon.ship.root -> false
                ship.owner == weapon.ship.owner -> true

                (ship.location - weapon.location).length > range -> false

                ship.isPhased -> false
                else -> true
            }
        }

        val evaluated = obstacles.mapNotNull { ship ->
            if (ship.owner == weapon.ship.owner) {
                analyzeAllyHit(weapon, target, ship, params)
            } else {
                analyzeHit(weapon, ship, params)
            }
        }

        return evaluated.minWithOrNull(compareBy { it.range })
    }

    private val WeaponHandle.isSlowRotating: Boolean
        get() {
            return when {
                turnRateWhileIdle < ship.baseTurnRate -> true

                isNonInterruptibleBurstWeapon && turnRateWhileFiring < ship.baseTurnRate -> true

                else -> false
            }
        }
}
