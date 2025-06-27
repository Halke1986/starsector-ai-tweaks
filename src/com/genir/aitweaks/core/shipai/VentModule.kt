package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle
import com.genir.aitweaks.core.shipai.autofire.ballistics.willHitBounds
import com.genir.aitweaks.core.shipai.autofire.ballistics.willHitShield
import com.genir.aitweaks.core.shipai.autofire.firingCycle
import com.genir.aitweaks.core.shipai.movement.EngineController.Destination
import com.genir.aitweaks.core.shipai.threat.MissileThreat
import com.genir.aitweaks.core.shipai.threat.WeaponThreat
import com.genir.aitweaks.core.utils.defaultAIInterval
import org.lwjgl.util.vector.Vector2f
import kotlin.math.min
import kotlin.math.pow
import com.genir.aitweaks.core.shipai.Preset as AIPreset

class VentModule(private val ai: CustomShipAI) {
    private val ship: ShipAPI = ai.ship
    private val damageTracker: DamageTracker = DamageTracker(ship)
    private val weaponThreat: WeaponThreat = WeaponThreat(ship)
    private val missileThreat: MissileThreat = MissileThreat(ship)
    private val fluxTracker: FluxTracker = FluxTracker(ship, ventTrackingPeriod)
    private val updateInterval: IntervalUtil = defaultAIInterval()

    var isBackingOff: Boolean = false
    var shouldFinishTarget: Boolean = false

    private var shouldInitVent: Boolean = false
    private var ventTrigger: Boolean = false
    private var isSafe: Boolean = false
    private var backoffDistance: Float = farAway

    private companion object Preset {
        const val ventTimeFactor = 0.8f
        const val idleVentThreshold = 0.30f
        const val opportunisticVentThreshold = 0.5f
        const val backoffUpperThreshold = 0.75f
        const val backoffLowerThreshold = 0.1f

        const val ventTrackingPeriod = 4.3f
        const val engageBeforeVentFinish = 1.5f

        const val farAway = 1e8f
    }

    fun advance(dt: Float) {
        debug()
        damageTracker.advance()
        fluxTracker.advance()

        updateInterval.advance(dt)
        if (updateInterval.intervalElapsed()) {
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

//        missileThreat.potentialDamage(ship.fluxTracker.timeToVent)

//        if (shouldFinishTarget && ship.fluxLevel > AIPreset.backoffUpperThreshold) {
//            Debug.drawCircle(ship.location, ship.collisionRadius / 2, BLUE)
//        }

//        if (isBackingOff && !ship.fluxTracker.isVenting) {
//            Debug.drawCircle(ship.location, ship.collisionRadius / 2, YELLOW)
//
//            weaponThreat.findDangerousWeapons(ship.fluxTracker.timeToVent * ventTimeFactor).forEach {
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
    private fun updateBackoffStatus() {
        val underFire = damageTracker.damage / ship.maxFlux > 0.2f

        isBackingOff = when {
            ai.flags.has(Flags.Flag.DO_NOT_BACK_OFF) -> false

            // Enemy is routing, keep the pressure.
            Global.getCombatEngine().isEnemyInFullRetreat -> false

            shouldFinishTarget -> false

            // Ship with no shield backs off when it can't fire anymore.
            ship.shield == null && ship.allWeapons.map { it.handle }.any { !it.isInFiringSequence && it.fluxCostToFire >= ship.fluxLeft } -> true

            // High flux.
            ship.shield != null && ship.hardFluxLevel > backoffUpperThreshold -> true

            // Flux is predicted to rapidly reach dangerous levels.
            ship.shield != null && ship.fluxLevel + fluxTracker.delta() > AIPreset.holdFireThreshold -> true

            // Shields down and received damage.
            underFire && ship.shield != null && ship.shield.isOff -> true

            // Started venting under fire.
            underFire && ship.fluxTracker.isVenting -> true

            // Stop backing off.
            ship.fluxLevel <= backoffLowerThreshold -> false

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
            ship.FluxLevel < backoffLowerThreshold -> false

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
        val effectiveHP: Float = ship.hitpoints * ship.hullLevel.let { it * it * it * it }

        val duration = ship.fluxTracker.timeToVent * ventTimeFactor
        val (finisherMissileDanger, potentialDamage) = weaponThreat.potentialDamage(duration)

        val projectileDamage = run {
            val projectiles = ai.globalAI.projectileTracker.threats(ship)
            effectiveDamage(filterRelevantProjectiles(projectiles))
        }

        val missileDamage = effectiveDamage(missileThreat.threats(duration))

        return when {
            // Don't get hit by a finisher missile.
            finisherMissileDanger -> {
                false
            }

            projectileDamage + missileDamage > effectiveHP * 0.095f -> {
                false
            }

            // The ship is not in range of any weapons, but there are projectiles inbound.
            // Assume the ship has just destroyed its duel opponent, and no more projectiles
            // will be incoming. Wait for the remaining projectiles to be intercepted by the
            // shield, and vent only after the duel is fully concluded.
            potentialDamage == 0f && projectileDamage > 0 -> {
                false
            }

            // Received negligible damage.
            damageTracker.damage < effectiveHP * 0.03f -> {
                true
            }

            // Attempt to tank a limited amount of damage. 0.1f may seem like a large fraction,
            // but potential damage calculation is the absolute worst case scenario.
            potentialDamage > effectiveHP * 0.1f -> {
                false
            }

            else -> {
                true
            }
        }
    }

    private fun filterRelevantProjectiles(projectiles: Sequence<DamagingProjectileAPI>): Sequence<DamagingProjectileAPI> {
        // If the ship has no shields, venting does not affect incoming projectiles.
        // An exception might be a ship system, but this is currently ignored.
        if (!ship.hasShield) {
            return sequenceOf()
        }

        return projectiles.filter { projectile ->
            when {
                // Ignore fighters, for now at least. In vanilla, it's viable
                // because fighters have weak weapons but can swarm backing off
                // ships and prevent them from venting indefinitely.
                projectile.weapon?.ship?.isFighter == true -> {
                    false
                }

                willHitBounds(projectile, ship) == null -> {
                    false
                }

                // Assume an omni shield has the chance to intercept each projectile.
                ship.shield.type == ShieldAPI.ShieldType.OMNI -> {
                    true
                }

                // Projectile will likely bypass shields.
                // Assume venting will not affect its intercept.
                willHitShield(projectile, ship) == null -> {
                    false
                }

                else -> {
                    true
                }
            }
        }
    }

    private fun effectiveDamage(threats: Sequence<DamagingProjectileAPI>): Float {
        val shipHp = ship.hitpoints * ship.hullLevel

        return threats.asIterable().sumOf { projectile ->
            val damageBase = projectile.damageAmount * projectile.damageType.armorMult
            if (damageBase >= shipHp) {
                return@sumOf damageBase
            }

            val weight = 1f - (1f - damageBase / shipHp).pow(32)

            damageBase * weight
        }
    }

    /** Determine if ship should forego venting and backing off
     * to instead focus on finishing its target. */
    private fun shouldFinishTarget(): Boolean {
        val target: ShipAPI = ai.attackTarget as? ShipAPI ?: return false

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

    private fun isIdle(): Boolean {
        return when {
            ship.shield?.isOn == true -> false

            ship.system?.isOn == true -> false

            else -> ship.allGroupedWeapons.all { it.isIdle || it.cooldownRemaining > ship.fluxTracker.timeToVent }
        }
    }

    private fun waitForBursts(): Boolean {
        val burstWeapons = ship.allGroupedWeapons.filter { it.isNonInterruptibleBurstWeapon }
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
