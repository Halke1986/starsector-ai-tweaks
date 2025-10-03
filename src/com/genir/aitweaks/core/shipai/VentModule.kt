package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.autofire.ballistics.willHitBounds
import com.genir.aitweaks.core.shipai.autofire.ballistics.willHitShield
import com.genir.aitweaks.core.shipai.autofire.firingCycle
import com.genir.aitweaks.core.shipai.threat.MissileThreat
import com.genir.aitweaks.core.shipai.threat.WeaponThreat
import com.genir.aitweaks.core.utils.defaultAIInterval
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class VentModule(private val ai: ShipAI) {
    private val ship: ShipAPI = ai.ship
    private val damageTracker: DamageTracker = DamageTracker(ship)
    private val weaponThreat: WeaponThreat = WeaponThreat(ship)
    private val missileThreat: MissileThreat = MissileThreat(ship)
    private val updateInterval: IntervalUtil = defaultAIInterval()

    var shouldFinishTarget: Boolean = false

    private var shouldInitVent: Boolean = false
    private var ventTrigger: Boolean = false
    var isSafeToVent: Boolean = false

    private companion object Preset {
        const val idleVentThreshold = 0.30f
        const val ventThreshold = 0.5f
        const val ventLowerThreshold = 0.1f

        const val ventTimeFlatModifierOptimistic = -1.0f
        const val ventTimeFlatModifierPessimistic = 0.5f
    }

    fun advance(dt: Float) {
        damageTracker.advance()

        updateInterval.advance(dt)
        if (updateInterval.intervalElapsed()) {
//            shouldFinishTarget = shouldFinishTarget() // TODO
            isSafeToVent = estimateIfSafe()
            shouldInitVent = shouldInitVent()

            ventTrigger = when {
                // Init vent operation.
                shouldInitVent && isSafeToVent -> true

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

    private fun shouldInitVent(): Boolean {
        return when {
            // Can't vent right now.
            ship.fluxTracker.isOverloadedOrVenting -> false

            // No need to vent.
            ship.FluxLevel < ventLowerThreshold -> false

            shouldFinishTarget -> false

            // Don't interrupt ship system without necessity.
            ship.system?.isOn == true -> false

            // Flux is not critical, but still could use an opportunity to vent.
            ship.FluxLevel >= ventThreshold -> true

            // Vent when the ship is idle.
            ship.FluxLevel >= idleVentThreshold && isIdle() -> true

            else -> false
        }
    }

    /** Decide if it's safe to vent. */
    private fun estimateIfSafe(): Boolean {
        // The Vent time modifier causes ships to start venting eagerly
        // and become more cautious once the vent is in progress.
        val modifier = if (ship.fluxTracker.isOverloadedOrVenting) {
            ventTimeFlatModifierPessimistic
        } else {
            ventTimeFlatModifierOptimistic
        }
        val duration = max(0f, ship.fluxTracker.timeToVent + modifier)
        val (finisherMissileDanger, weaponDamage) = weaponThreat.potentialDamage(duration)

        // Don't get hit by a finisher missile.
        if (finisherMissileDanger) {
            return false
        }

        val allProjectiles = ai.globalAI.projectileTracker.threats(ship)
        val projectiles = filterRelevantProjectiles(allProjectiles)
        val projectileDamage = effectiveDamage(projectiles)

        val missiles = missileThreat.threats(duration)
        val missileDamage = effectiveDamage(missiles)

        val effectiveHP: Float = ship.hitpoints * ship.hullLevel.let { it * it * it * it }
        return when {
            projectileDamage + missileDamage > effectiveHP * 0.095f -> {
                false
            }

            // Received negligible damage.
            damageTracker.damage < effectiveHP * 0.03f -> {
                true
            }

            // Attempt to tank a limited amount of damage. 0.1f may seem like a large fraction,
            // but potential damage calculation is the absolute worst case scenario.
            projectileDamage + missileDamage + weaponDamage > effectiveHP * 0.1f -> {
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

            // Weight the damage, to account for armor being
            // effective against weak projectiles.
            val weight = 1f - (1f - damageBase / shipHp).pow(32)

            // Increase the perceived damage of projectiles fired
            // by enemies that have already been destroyed, so the
            // ship won’t vent immediately after winning a duel if
            // enemy projectiles are still inbound.
            val deadEnemyBonus = if (projectile.weapon?.ship?.isAlive == false) 10f else 1f

            damageBase * weight * deadEnemyBonus
        }
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
