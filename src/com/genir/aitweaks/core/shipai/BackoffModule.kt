package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.DamageType.HIGH_EXPLOSIVE
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.aitweaks.core.utils.Arc
import com.genir.aitweaks.core.utils.absShortestRotation
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.aitweaks.core.utils.distanceToOrigin
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max
import kotlin.math.sqrt
import com.genir.aitweaks.core.shipai.Preset as AIPreset

class BackoffModule(private val ai: CustomShipAI) {
    private val ship: ShipAPI = ai.ship
    private val damageTracker: DamageTracker = DamageTracker(ship)
    private val updateInterval: IntervalUtil = defaultAIInterval()

    private var dangerousWeapons: List<WeaponAPI> = listOf()
    private var backoffDistance: Float = 0f
    private var isSafe: Boolean = false
    var isBackingOff: Boolean = false

    private companion object Preset {
        const val ventTimeFactor = 1f
        const val shipSpeedFactor = 0.75f
        const val opportunisticVentThreshold = 0.5f

        const val farAway = 1e8f
    }

    fun advance(dt: Float) {
        damageTracker.advance()

        updateInterval.advance(dt)
        if (updateInterval.intervalElapsed()) {
            assesIfSafe()
            updateBackoffStatus()

            ventIfNeeded()
        }
    }

    /** Control the ship heading when backing off. */
    fun setHeading(maneuverTarget: ShipAPI?): Vector2f {
        // Update safe backoff distance.
        when {
            !isSafe -> backoffDistance = farAway

            backoffDistance == farAway && maneuverTarget != null -> {
                (maneuverTarget.location - ship.location).length * 1.1f
            }
        }

        // Calculate backoff heading.
        return when {
            ai.backoff.isSafe -> when {
                // Maintain const distance from maneuver target.
                maneuverTarget != null -> maneuverTarget.location - ai.threatVector.resized(backoffDistance)

                // Stop when there's no maneuver target present.
                else -> EngineController.allStop
            }

            // Move opposite to threat vector.
            ai.threatVector.isNonZero -> ship.location - ai.threatVector.resized(farAway)

            // Continue present course if no threat vector is available.
            else -> ship.location + ship.velocity.resized(farAway)
        }
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

            // Vent if ship is backing off.
            isBackingOff -> true

            // Don't interrupt ship system without necessity.
            ship.system?.isOn == true -> false

            // Flux is not critical, but still could use an opportunity to vent.
            ship.fluxLevel >= opportunisticVentThreshold -> true

            // Vent when the ship is idle.
            ship.allGroupedWeapons.all { it.isIdle } -> true

            else -> false
        }

        if (!shouldVent) return

        val isSafeToVent = when {
            // Trust vanilla missile danger assessment.
            ai.vanilla.missileDangerDir != null -> false

            isSafe -> true

            // Don't get hit by a finisher missile.
            dangerousWeapons.any { it.isFinisherMissile } -> false

            // Attempt to tank a limited amount of damage.
            ship.hullLevel > 0.2f && damageTracker.damage / ship.hullSpec.hitpoints < 0.01f -> true

            else -> false
        }

        if (isSafeToVent) ship.command(ShipCommand.VENT_FLUX)
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
                    // Don't consider overloaded or venting enemies.
                    val tracker = entity.fluxTracker
                    when {
                        !tracker.isOverloadedOrVenting -> enemies.add(entity)
                        ventTime - 2f > max(tracker.timeToVent, tracker.overloadTimeRemaining) -> enemies.add(entity)
                    }

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

        isSafe = dangerousWeapons.isEmpty()
    }

    private fun canHitShip(weapon: WeaponAPI, ventTime: Float, obstacles: List<ShipAPI>): Boolean {
        val toShip = ship.location - weapon.location
        val distSqr = toShip.lengthSquared
        val dist = sqrt(distSqr) - ship.shieldRadiusEvenIfNoShield / 2

        // Check if the ship is out of weapons range.
        val adjustedMovementTime = if (weapon.ship.engineController.isFlamedOut) 0f else ventTime * shipSpeedFactor
        if (dist - adjustedMovementTime * weapon.ship.maxSpeed > weapon.range) {
            return false
        }

        // Check if the ship is out of weapons arc.
        val isGuidedFinisherMissile = weapon.isFinisherMissile && !weapon.isUnguidedMissile
        val weaponArc = Arc(weapon.arc, weapon.absoluteArcFacing).increasedBy(adjustedMovementTime * weapon.ship.maxTurnRate)
        if (!isGuidedFinisherMissile && !weaponArc.contains(toShip.facing)) {
            return false
        }

        // Check if projectile will reach the ship during venting. Take special care of finisher missiles.
        val adjustedVentTime = ventTime * ventTimeFactor - weapon.cooldownRemaining
        if (dist / weapon.maxProjectileSpeed > adjustedVentTime) {
            return false
        }

        // Check if the weapon is busy firing at another target.
        if (absShortestRotation(toShip.facing, weapon.currAngle) > 30f && weapon.isInFiringCycle && !isGuidedFinisherMissile) {
            return false
        }

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

    private val WeaponAPI.isFinisherMissile: Boolean
        get() = isMissile && damageType == HIGH_EXPLOSIVE

    private val WeaponAPI.maxProjectileSpeed: Float
        get() {
            val missileSpec: MissileSpecAPI = spec.projectileSpec as? MissileSpecAPI ?: return projectileSpeed
            val engineSpec: ShipHullSpecAPI.EngineSpecAPI = missileSpec.hullSpec.engineSpec
            val maxSpeedStat = MutableStat(engineSpec.maxSpeed)
            return maxSpeedStat.modifiedValue
        }
}
