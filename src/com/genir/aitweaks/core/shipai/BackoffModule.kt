package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.DamageType.HIGH_EXPLOSIVE
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.aitweaks.core.utils.Arc
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.aitweaks.core.utils.distanceToOrigin
import kotlin.math.max
import kotlin.math.sqrt
import com.genir.aitweaks.core.shipai.Preset as AIPreset

class BackoffModule(private val ai: CustomShipAI) {
    private val ship: ShipAPI = ai.ship
    private val damageTracker: DamageTracker = DamageTracker(ship)
    private val updateInterval: IntervalUtil = defaultAIInterval()

    private var dangerousWeapons: List<WeaponAPI> = listOf()
    var isBackingOff: Boolean = false
    val isSafe: Boolean
        get() = dangerousWeapons.isEmpty()

    fun advance(dt: Float) {
        damageTracker.advance()

        updateInterval.advance(dt)
        if (updateInterval.intervalElapsed()) {
            updateBackoffStatus()
            if (isBackingOff) assesIfSafe()

            ventIfNeeded()
        }

        if (!isBackingOff) dangerousWeapons = listOf()
    }

    /** Decide if ships needs to back off due to high flux level */
    private fun updateBackoffStatus() {
        val fluxLevel = ship.fluxTracker.fluxLevel
        val underFire = damageTracker.damage / ship.maxFlux > 0.2f

        isBackingOff = when {
            // Enemy is routing, keep the pressure.
            Global.getCombatEngine().isEnemyInFullRetreat -> false

            // Ship with no shield backs off when it can't fire anymore.
            ship.shield == null && ship.allWeapons.any { !it.isInFiringSequence && it.fluxCostToFire >= ship.fluxLeft } -> true

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

        if (isBackingOff) ai.aiFlags.setFlag(ShipwideAIFlags.AIFlags.BACKING_OFF)
        else ai.aiFlags.unsetFlag(ShipwideAIFlags.AIFlags.BACKING_OFF)
    }

    private fun ventIfNeeded() {
        val shouldVent = when {
            // Can't vent right now.
            ship.fluxTracker.isOverloadedOrVenting -> false

            // No need to vent.
            ship.fluxLevel < AIPreset.backoffLowerThreshold -> false

            // Vent if no danger, even if not backing off.
            ai.threatVector.isZero && ship.shield?.isOff == true -> true

            // Vent only if ship is backing off or is idle.
            !isBackingOff && !shipIsIdle() -> false

            // Trust vanilla missile danger assessment.
            ai.vanilla.missileDangerDir != null -> false

            isSafe -> true

            // Don't get hit by a finisher torpedo.
            dangerousWeapons.any { it.isMissile && it.damageType == HIGH_EXPLOSIVE } -> false

            // Attempt to tank a limited amount of damage.
            ship.hullLevel > 0.2f && damageTracker.damage / ship.hullSpec.hitpoints < 0.01f -> true

            else -> false
        }

        if (shouldVent) ship.command(ShipCommand.VENT_FLUX)
    }

    private fun shipIsIdle(): Boolean {
        return when {
            ship.system?.isOn == true -> false

            ship.shield?.isOn == true -> false

            ship.allGroupedWeapons.any { it.isInFiringCycle } -> false

            else -> true
        }
    }

    private fun assesIfSafe() {
        val enemies: MutableList<ShipAPI> = mutableListOf()
        val obstacles: MutableList<ShipAPI> = mutableListOf()

        val ventTime = ship.fluxTracker.timeToVent

        Global.getCombatEngine().ships.asSequence().forEach { entity ->
            when {
                entity.root == ship.root -> Unit
                entity.isFighter -> Unit

                entity.isHostile(ship) -> {
                    val tracker = entity.fluxTracker
                    val maxEnemyVentTime = if (!tracker.isOverloadedOrVenting) 0f
                    else max(tracker.timeToVent, tracker.overloadTimeRemaining)

                    // Don't consider overloaded or venting enemies.
                    if (ventTime - 2f > maxEnemyVentTime) enemies.add(entity)

                    // Consider slow enemies as weapon obstacles.
                    if (!entity.isFast) obstacles.add(entity)
                }

                else -> if (!entity.root.isFrigate) obstacles.add(entity)
            }
        }

        dangerousWeapons = enemies.flatMap { it.allGroupedWeapons }.filter { weapon ->
            when {
                weapon.isDisabled -> false
                weapon.isPermanentlyDisabled -> false

                // Assume the ship can recognize an empty
                // missile launcher, same as the player can.
                weapon.isMissile && weapon.isOutOfAmmo -> false

                !canHitShip(weapon, ventTime, obstacles) -> false

                else -> true
            }
        }
    }

    @Suppress("ConstPropertyName")
    companion object Preset {
        const val ventTimeFactor = 0.75f
        const val shipSpeedFactor = 0.75f
    }

    private fun canHitShip(weapon: WeaponAPI, ventTime: Float, obstacles: List<ShipAPI>): Boolean {
        val toShip = ship.location - weapon.location
        val distSqr = toShip.lengthSquared
        val dist = sqrt(distSqr)

        // Check if projectile will reach the ship during venting.
        val adjustedVentTime = ventTime * ventTimeFactor - weapon.cooldownRemaining
        if (dist / weapon.projectileSpeed > adjustedVentTime) return false

        // Check if the ship is out of weapons range.
        val shipSpeedFactor = if (weapon.ship.engineController.isFlamedOut) 0f else shipSpeedFactor
        if (dist - shipSpeedFactor * weapon.ship.maxSpeed > weapon.range) return false

        // Check if the ship is out of weapons arc.
        val isGuidedFinisherMissile = weapon.isMissile && !weapon.isUnguidedMissile && weapon.damageType == HIGH_EXPLOSIVE
        val weaponArc = Arc(weapon.arc, weapon.absoluteArcFacing).increasedBy(shipSpeedFactor * ship.maxTurnRate)
        if (!isGuidedFinisherMissile && !weaponArc.contains(toShip.facing)) return false

        return obstacles.none { obstacle ->
            // Obstacle does not block its own weapons.
            if (obstacle.root == weapon.ship.root) return@none false

            val p = weapon.location - obstacle.location
            val v = toShip

            // Obstacle is behind the ship.
            if (p.lengthSquared > distSqr) return@none false

            val radius = if (obstacle.isAlive) obstacle.shieldRadiusEvenIfNoShield * 1.5f
            else state.bounds.radius(obstacle)

            distanceToOrigin(p, v) <= radius
        }
    }
}
