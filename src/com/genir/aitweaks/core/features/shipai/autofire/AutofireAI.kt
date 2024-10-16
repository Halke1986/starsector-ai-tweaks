package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.core.features.shipai.Preset
import com.genir.aitweaks.core.features.shipai.WrapperShipAI
import com.genir.aitweaks.core.features.shipai.autofire.HoldFire.*
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.*
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.min

open class AutofireAI(private val weapon: WeaponAPI) : AutofireAIPlugin {
    private val ship: ShipAPI = weapon.ship

    protected var target: CombatEntityAPI? = null
    private var attackTime: Float = 0f
    private var idleTime: Float = 0f
    private var idleFrames: Int = 0
    private var onTargetTime: Float = 0f
    private var isForcedOff: Boolean = false

    private var selectTargetInterval = Interval(0.25F, 0.50F)
    private var shouldFireInterval = Interval(0.1F, 0.2F)

    // Aiming data.
    private var aimPoint: Vector2f? = null
    private var prevTurretIntercept: Vector2f? = null
    private var prevShipFacing: Float = 0f
    private var shouldHoldFire: HoldFire? = NO_TARGET
    var predictedHit: Hit? = null

    var syncState: SyncState? = null

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
        if (shouldFireInterval.elapsed()) {
            shouldFireInterval.reset()
            shouldHoldFire = calculateShouldFire()
        }
    }

    override fun shouldFire(): Boolean {
        val syncState = syncState
        return when {
            isForcedOff -> {
                isForcedOff = false
                false
            }

            shouldHoldFire != null -> false

            syncState != null -> syncFire(syncState)

            else -> true
        }
    }

    override fun forceOff() {
        isForcedOff = true
    }

    override fun getTarget(): Vector2f? = aimPoint
    override fun getTargetShip(): ShipAPI? = target as? ShipAPI
    override fun getWeapon(): WeaponAPI = weapon
    override fun getTargetMissile(): MissileAPI? = target as? MissileAPI

    fun plotIntercept(target: CombatEntityAPI): Vector2f {
        return intercept(weapon, BallisticTarget.entity(target), currentParams())
    }

    /** Make weapons sync fire in staggered firing mode. */
    private fun syncFire(syncState: SyncState): Boolean {
        // No need to sync a single weapon.
        if (syncState.weapons == 1) return true

        // Weapon is the middle of firing cycle, it won't stop now.
        if (weapon.isInFiringCycle) return true

        val timestamp = Global.getCombatEngine().getTotalElapsedTime(false)
        val cycleDuration = weapon.firingCycle.duration
        val sinceLastAttack = timestamp - syncState.lastAttack
        val stagger = cycleDuration / syncState.weapons
        val dt = Global.getCombatEngine().elapsedInLastFrame

        val shouldFire = when {
            // Combined rate of fire is too fast to sync.
            stagger < dt * 2f -> true

            // Weapon finished its firing cycle. Assume it's not yet out of
            // sync and continue attack, unless another weapon attacked the
            // same frame. NOTE: there's also an idle frame between shots in
            // a burst, but that case if handled by 'if (weapon.isInFiringCycle)
            // return true' case.
            sinceLastAttack >= stagger && idleFrames == 1 -> true

            // Weapons of same type didn't attack for at least entire firing cycle,
            // meaning all of them are ready to attack. The weapon may fire immediately.
            sinceLastAttack > cycleDuration -> true

            // Wait for opportunity to attack aligned with staggered attack cycle.
            else -> sinceLastAttack >= stagger && sinceLastAttack % stagger <= dt
        }

        if (shouldFire) syncState.lastAttack = timestamp

        return shouldFire
    }

    private fun trackAttackTimes(dt: Float) {
        // Update attack time, used by vanilla accuracy increase mechanism.
        if (weapon.isFiring) {
            attackTime += dt
            idleTime = 0f
            idleFrames = 0
        } else {
            idleTime += dt
            idleFrames++
        }

        if (idleTime >= 3f) attackTime = 0f

        // Update time on target, used by STABILIZE_ON_TARGET firing rule.
        if (shouldHoldFire == NO_TARGET || shouldHoldFire == NO_HIT_EXPECTED) onTargetTime = 0f
        else onTargetTime += dt
    }

    private fun shouldUpdateTarget(): Boolean {
        val currentTarget: CombatEntityAPI? = target

        return when {
            currentTarget == null -> selectTargetInterval.elapsed()

            // Target became invalid last frame, update immediately.
            // This is important for PD weapons defending against swarms of missiles.
            !currentTarget.isValidTarget -> {
                target = null
                true
            }

            // Target can no longer be tracked. This does not apply to hardpoint weapons tracking ship attack target.
            (currentTarget != ship.attackTarget || weapon.slot.isTurret) && !canTrack(weapon, BallisticTarget.entity(target!!), currentParams()) -> {
                target = null
                true
            }

            // Do not interrupt firing sequence.
            weapon.isInFiringSequence -> false

            // Outside above special circumstances, refresh target periodically.
            else -> selectTargetInterval.elapsed()
        }
    }

    protected open fun calculateShouldFire(): HoldFire? {
        predictedHit = null

        if (target?.isValidTarget != true) return NO_TARGET

        holdFireIfOverfluxed()?.let { return it }

        stabilizeOnTarget()?.let { return it }

        // Fire only when the selected target can be hit. That way the weapon doesn't fire
        // on targets that are only briefly in the line of sight, when the weapon is turning.
        val ballisticParams = currentParams()
        val expectedHit = target?.let { analyzeHit(weapon, target!!, ballisticParams) }

        if (expectedHit == null) return NO_HIT_EXPECTED

        predictedHit = expectedHit

        // Check what actually will get hit, and hold fire if it's an ally or hulk.
        val actualHit = firstShipAlongLineOfFire(weapon, ballisticParams)

        // Rest of the should-fire decisioning will be based on the actual hit.
        val hit = when {
            actualHit == null -> expectedHit
            actualHit.range > expectedHit.range -> expectedHit
            else -> actualHit
        }
        predictedHit = hit

        avoidFriendlyFire(weapon, expectedHit, actualHit)?.let { return it }

        when {
            hit.shieldHit && hit.range > weapon.range -> return OUT_OF_RANGE
            !hit.shieldHit && hit.range > weapon.totalRange -> return OUT_OF_RANGE
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
            weapon.isPD || !target!!.isShip -> return null

            // Weapon is on target for long enough already.
            onTargetTime >= min(2f, weapon.firingCycle.duration) -> return null
        }

        val arc = interceptArc(weapon, BallisticTarget.entity(target!!), currentParams()) ?: return STABILIZE_ON_TARGET
        val inaccuracy = abs(MathUtils.getShortestRotation(weapon.currAngle, arc.facing))
        if (inaccuracy * 4f > arc.arc) return STABILIZE_ON_TARGET

        return null
    }

    private fun updateAimLocation() {
        val target: CombatEntityAPI? = this.target
        if (target == null) {
            prevTurretIntercept = null
            aimPoint = null
            return
        }

        aimPoint = if (weapon.slot.isTurret) aimTurret(target)
        else aimHardpoint(target)
    }

    /** get current weapon attack parameters */
    protected fun currentParams() = BallisticParams(
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

    private fun aimTurret(target: CombatEntityAPI): Vector2f {
        val intercept = plotIntercept(target)

        // ship.angularVelocity() returns 0 for modules and stations, so the ship
        // true angular velocity is estimated by calculating the change in ship facing.
        val shipRotation = ship.facing - prevShipFacing
        val prevIntercept = prevTurretIntercept ?: intercept
        val interceptRotation = getShortestRotation(prevIntercept, weapon.location, intercept)

        // Save current state for next frame.
        prevShipFacing = ship.facing
        prevTurretIntercept = intercept

        // Combat engine fires weapons before setting their aim location. This means
        // the aim location returned by this method will take effect the next frame.
        // Therefore, the weapon facing needs to be extrapolated based on intercept
        // point's and ship's angular velocity.
        val r = Rotation(interceptRotation - shipRotation)
        return intercept.rotatedAroundPivot(r, weapon.location)
    }

    /** predictive aiming for hardpoints */
    private fun aimHardpoint(target: CombatEntityAPI): Vector2f {
        // Try to read expected facing from custom AI implementations.
        val customAIFacing = ship.customShipAI?.movement?.expectedFacing
        val wrapperAIFacing = (ship.ai as? WrapperShipAI)?.expectedFacing
        val expectedFacing = customAIFacing ?: wrapperAIFacing ?: (target.location - ship.location).facing

        // If no expected facing was found, assume the ship is controlled by vanilla AI.
        // Vanilla AI lacks precise aiming, so hardpoints need flexibility to compensate.
        // Aim directly at the intercept point when the ship is close to aligned.
        if (customAIFacing == null && wrapperAIFacing == null) {
            val intercept = plotIntercept(target)
            if (Arc(weapon.arc, weapon.absoluteArcFacing).contains(intercept - weapon.location))
                return intercept
        }

        // Aim the hardpoint as if the ship was facing the target directly.
        val r = Rotation(MathUtils.getShortestRotation(expectedFacing, ship.facing))
        val v = target.velocity.rotated(r)
        val p = target.location.rotatedAroundPivot(r, weapon.ship.location)
        return intercept(weapon, BallisticTarget(v, p, target.collisionRadius), currentParams())
    }
}
