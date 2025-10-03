package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle
import com.genir.aitweaks.core.shipai.movement.EngineController.Destination
import com.genir.aitweaks.core.utils.defaultAIInterval
import org.lwjgl.util.vector.Vector2f
import kotlin.math.min
import com.genir.aitweaks.core.shipai.Preset as AIPreset

class BackoffModule(private val ai: CustomShipAI) {
    private val ship: ShipAPI = ai.ship
    private val damageTracker: DamageTracker = DamageTracker(ship)
    private val fluxTracker: FluxTracker = FluxTracker(ship, fluxTrackingPeriod)
    private val updateInterval: IntervalUtil = defaultAIInterval()

    var isBackingOff: Boolean = false
    private var backoffDistance: Float = farAway

    private companion object Preset {
        const val backoffUpperThreshold = 0.75f
        const val backoffLowerThreshold = 0.1f

        const val fluxTrackingPeriod = 4.3f
        const val engageBeforeVentFinish = 1.5f

        const val farAway = 1e8f
    }

    fun advance(dt: Float) {
        damageTracker.advance()
        fluxTracker.advance()

        updateInterval.advance(dt)
        if (updateInterval.intervalElapsed()) {
            isBackingOff = updateBackoffStatus()

            // Update the AI flag.
            if (isBackingOff) {
                ai.flags.set(Flags.Flag.BACKING_OFF)
            } else {
                ai.flags.unset(Flags.Flag.BACKING_OFF)
            }

            // Reset backoff distance.
            if (!isBackingOff) {
                backoffDistance = farAway
            }
        }
    }

    /** Control the ship heading when backing off. */
    fun overrideHeading(maneuverTarget: ShipAPI?): Destination? {
        if (!isBackingOff) {
            return null
        }

        // Update safe backoff distance.
        when {
            !ai.ventModule.isSafeToVent -> backoffDistance = farAway

            backoffDistance == farAway && maneuverTarget != null -> {
                backoffDistance = (maneuverTarget.location - ship.location).length * 1.1f
            }
        }

        // Calculate backoff heading.
        return when {
            ai.ventModule.isSafeToVent -> when {
                // Stop backing off when venting is close to finished.
                // Otherwise, ship will start reversing course too late and back off too far.
                ship.fluxTracker.isVenting && ship.fluxTracker.timeToVent < engageBeforeVentFinish -> {
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
    private fun updateBackoffStatus(): Boolean {
        val underFire = damageTracker.damage / ship.maxFlux > 0.2f

        return when {
            ai.flags.has(Flags.Flag.DO_NOT_BACK_OFF) -> false

            // Enemy is routing, keep the pressure.
            Global.getCombatEngine().isEnemyInFullRetreat -> false

            shouldFinishTarget() -> false

            // Ship with no shield backs off when it can't fire anymore.
            ship.shield == null && ship.allWeapons.map { it.handle }.any { !it.isInFiringSequence && it.fluxCostToFire >= ship.fluxLeft } -> true

            // High flux.
            ship.shield != null && ship.hardFluxLevel > backoffUpperThreshold -> true

            // Flux is predicted to rapidly reach dangerous levels.
            ship.shield != null && ship.fluxLevel + fluxTracker.delta() > AIPreset.holdFireThreshold -> true

            // Shields down and received damage.
            underFire && ship.shield != null && ship.shield.isOff -> true

            // Started venting under fire.
            ship.fluxTracker.isVenting && underFire -> true

            // Threat situation worsened while venting.
            ship.fluxTracker.isVenting && !ai.ventModule.isSafeToVent -> true

            // Stop backing off.
            ship.fluxLevel <= backoffLowerThreshold -> false

            // Continue current backoff status.
            else -> isBackingOff
        }
    }

    /** Determine if ship should forego venting and backing off
     * to instead focus on finishing its target. */
    private fun shouldFinishTarget(): Boolean {
        val target: ShipAPI = ai.attackTarget as? ShipAPI
            ?: return false

        when {
            target.isFighter -> {
                return false
            }

            target.root.isFrigate -> {
                return false
            }

            // Target is behind healthy shields.
            target.shield?.isOn == true && target.fluxLevel < 0.8f -> {
                return false
            }

            // Target is too far.
            ai.currentEffectiveRange(target) > min(ai.attackingGroup.maxRange, 1.1f * ai.attackingGroup.effectiveRange) -> {
                return false
            }

            // The ship is the victim, not the target.
            target.hullLevel > ship.hullLevel -> {
                return false
            }
        }

        // The more damaged the target, the higher flux level
        // the ship is willing to tolerate.
        val damage = 1 - target.hullLevel
        return ship.FluxLevel < damage * damage
    }
}
