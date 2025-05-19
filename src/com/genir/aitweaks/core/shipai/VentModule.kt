package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle
import com.genir.aitweaks.core.shipai.autofire.firingCycle
import com.genir.aitweaks.core.shipai.movement.EngineController.Destination
import com.genir.aitweaks.core.utils.defaultAIInterval
import org.lwjgl.util.vector.Vector2f
import kotlin.math.min
import com.genir.aitweaks.core.shipai.Preset as AIPreset

class VentModule(private val ai: CustomShipAI) {
    private val ship: ShipAPI = ai.ship
    private val damageTracker: DamageTracker = DamageTracker(ship)
    private val updateInterval: IntervalUtil = defaultAIInterval()

    var isBackingOff: Boolean = false
    var shouldFinishTarget: Boolean = false
    private var ventTime: Float = ship.fluxTracker.timeToVent
    private var shouldInitVent: Boolean = false
    private var ventTrigger: Boolean = false
    private var isSafe: Boolean = false
    private var backoffDistance: Float = farAway

    private companion object Preset {
        const val ventTimeFactor = 0.8f
        const val idleVentThreshold = 0.30f
        const val opportunisticVentThreshold = 0.5f

        const val farAway = 1e8f
    }

    fun advance(dt: Float) {
        debug()
        damageTracker.advance()

        updateInterval.advance(dt)
        if (updateInterval.intervalElapsed()) {
            ventTime = ship.fluxTracker.timeToVent
            shouldFinishTarget = shouldFinishTarget()
            updateBackoffStatus()
            shouldInitVent = shouldInitVent()

            if (!isBackingOff) {
                backoffDistance = farAway
            }

            // isSafe status is required for backoff
            // maneuver and for deciding if to vent.
            if (isBackingOff || shouldInitVent) {
                isSafe = isSafe()
            }

            ventTrigger = when {
                // Init vent operation.
                shouldInitVent && isSafe -> true

                // Continue initiated vent operation, even if not feeling safe anymore.
                // Otherwise, the ship could lose all opportunities to vent.
                shouldInitVent && ventTrigger -> true

                else -> false
            }
        }

        // Wait for weapon bursts to subside before venting. Run the test
        // every frame, to effectively force-off all burst weapons.
        if (ventTrigger && !waitForBursts()) {
            ship.command(ShipCommand.VENT_FLUX)
        }
    }

    private fun debug() {
        if (ship.owner != 0) {
            return
        }

//        if (shouldFinishTarget && ship.fluxLevel > AIPreset.backoffUpperThreshold) {
//            Debug.drawCircle(ship.location, ship.collisionRadius / 2, BLUE)
//        }

//        if (isBackingOff && !ship.fluxTracker.isVenting) {
//            Debug.drawCircle(ship.location, ship.collisionRadius / 2, YELLOW)
//
//            findDangerousWeapons().filter { it.isFinisherMissile }.forEach {
//                Debug.drawLine(ship.location, it.location, RED)
//            }
//        }
    }

    /** Control the ship heading when backing off. */
    fun overrideHeading(maneuverTarget: ShipAPI?): Destination? {
        if (!isBackingOff) {
            return null
        }

        // Update safe backoff distance.
        when {
            !isSafe -> backoffDistance = farAway

            backoffDistance == farAway && maneuverTarget != null -> {
                backoffDistance = (maneuverTarget.location - ship.location).length * 1.1f
            }
        }

        // Calculate backoff heading.
        return when {
            ai.ventModule.isSafe -> when {
                // Stop backing off when venting is close to finished.
                // Otherwise, ship will start reversing course too late and back off too far.
                ship.fluxTracker.isVenting && ship.fluxTracker.timeToVent < 3f -> {
                    null
                }

                // Maintain const distance from the maneuver target.
                maneuverTarget != null -> {
                    val location = maneuverTarget.location - ai.threatVector.resized(backoffDistance)
                    Destination(location, maneuverTarget.timeAdjustedVelocity)
                }

                else -> {
                    null
                }
            }

            // Move opposite to threat vector.
            ai.threatVector.isNonZero -> {
                val location = ship.location - ai.threatVector.resized(farAway)
                Destination(location, Vector2f())
            }

            // Continue present course if no threat vector is available.
            else -> {
                Destination(ship.location, ship.velocity.resized(farAway))
            }
        }
    }

    /** Decide if ships needs to back off due to high flux level */
    private fun updateBackoffStatus() {
        val fluxLevel = ship.fluxTracker.fluxLevel
        val underFire = damageTracker.damage / ship.maxFlux > 0.2f

        isBackingOff = when {
            ai.flags.has(Flags.Flag.DO_NOT_BACK_OFF) -> false

            // Enemy is routing, keep the pressure.
            Global.getCombatEngine().isEnemyInFullRetreat -> false

            shouldFinishTarget -> false

            // Ship with no shield backs off when it can't fire anymore.
            ship.shield == null && ship.allWeapons.map { it.handle }.any { !it.isInFiringSequence && it.fluxCostToFire >= ship.fluxLeft } -> true

            // High flux.
            ship.shield != null && fluxLevel > AIPreset.backoffUpperThreshold -> true

            // Shields down and received damage.
            underFire && ship.shield != null && ship.shield.isOff -> true

            // Started venting under fire.
            underFire && ship.fluxTracker.isVenting -> true

            // Stop backing off.
            fluxLevel <= AIPreset.backoffLowerThreshold -> false

            // Continue current backoff status.
            else -> isBackingOff
        }

        if (isBackingOff) {
            ai.flags.set(Flags.Flag.BACKING_OFF)
        } else {
            ai.flags.unset(Flags.Flag.BACKING_OFF)
        }
    }

    private fun shouldInitVent(): Boolean {
        return when {
            // Can't vent right now.
            ship.fluxTracker.isOverloadedOrVenting -> false

            // No need to vent.
            ship.FluxLevel < AIPreset.backoffLowerThreshold -> false

            shouldFinishTarget -> false

            // Vent if ship is backing off.
            isBackingOff -> true

            // Don't interrupt ship system without necessity.
            ship.system?.isOn == true -> false

            // Flux is not critical, but still could use an opportunity to vent.
            ship.FluxLevel >= opportunisticVentThreshold -> true

            // Vent when the ship is idle.
            ship.FluxLevel >= idleVentThreshold && isIdle() -> true

            else -> false
        }
    }

    /** Decide if it's safe to vent. */
    private fun isSafe(): Boolean {
        // Trust vanilla missile danger assessment.
        if (ai.vanilla.missileDangerDir != null) {
            return false
        }

        val estimator = DamageEstimator(ship)
        val (finisherMissileDanger, potentialDamage) = estimator.potentialDamage(ventTime * ventTimeFactor)
        val effectiveHP: Float = ship.hitpoints * ship.hullLevel.let { it * it * it * it }

        return when {
            // Don't get hit by a finisher missile.
            finisherMissileDanger -> false

            // Received negligible damage.
            damageTracker.damage / effectiveHP < 0.03f -> true

            // Attempt to tank a limited amount of damage. 0.1f may seem like a large fraction,
            // but potential damage calculation is the absolute worst case scenario.
            potentialDamage / effectiveHP < 0.1f -> true

            else -> false
        }
    }

    /** Determine if ship should forego venting and backing off
     * to instead focus on finishing its target. */
    private fun shouldFinishTarget(): Boolean {
        val target: ShipAPI = ai.attackTarget as? ShipAPI ?: return false

        when {
            target.isFighter -> return false

            target.root.isFrigate -> return false

            // Target is behind healthy shields.
            target.shield?.isOn == true && target.fluxLevel < 0.8f -> {
                return false
            }

            // Target is too far.
            ai.currentEffectiveRange(target) > min(ai.attackingGroup.maxRange, 1.1f * ai.attackingGroup.effectiveRange) -> {
                return false
            }

            // The ship is the victim, not the target.
            target.hullLevel > ship.hullLevel -> return false
        }

        // The more damaged the target, the higher flux level
        // the ship is willing to tolerate.
        val damage = 1 - target.hullLevel
        return ship.FluxLevel < damage * damage
    }

    private fun isIdle(): Boolean {
        return when {
            ship.shield?.isOn == true -> false

            ship.system?.isOn == true -> false

            else -> ship.allGroupedWeapons.all { it.isIdle || it.cooldownRemaining > ventTime }
        }
    }

    private fun waitForBursts(): Boolean {
        val burstWeapons = ship.allGroupedWeapons.filter { it.isBurstWeapon && !it.spec.isInterruptibleBurst }
        val longestBursts: Float = burstWeapons.filter { it.isInBurst }.maxOfOrNull { it.burstFireTimeRemaining } ?: 0f

        // Vent when all the bursts have ended.
        if (longestBursts == 0f) {
            return false
        }

        // Do not start bursts that will not end before vent.
        val timeToVent = min(longestBursts, 2f)
        burstWeapons.forEach {
            val cycle = it.firingCycle
            val duration = cycle.warmupDuration + cycle.burstDuration
            if (duration >= timeToVent) {
                it.autofirePlugin?.forceOff()
            }
        }

        return true
    }
}
