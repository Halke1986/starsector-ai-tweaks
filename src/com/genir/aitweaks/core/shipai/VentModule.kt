package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.DamageType.FRAGMENTATION
import com.fs.starfarer.api.combat.DamageType.HIGH_EXPLOSIVE
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.autofire.firingCycle
import com.genir.aitweaks.core.utils.Arc
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.absShortestRotation
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.aitweaks.core.utils.distanceToOrigin
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
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
        const val shipSpeedFactor = 0.75f
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
    fun overrideHeading(maneuverTarget: ShipAPI?): Vector2f? {
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
                ship.fluxTracker.isVenting && ship.fluxTracker.timeToVent < 3f -> null

                // Maintain const distance from maneuver target.
                maneuverTarget != null -> maneuverTarget.location - ai.threatVector.resized(backoffDistance)

                else -> null
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

            shouldFinishTarget -> false

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

        val dangerousWeapons = findDangerousWeapons()
        val effectiveHP: Float = ship.hitpoints * ship.hullLevel.let { it * it * it * it }

        return when {
            dangerousWeapons.isEmpty() -> true

            // Don't get hit by a finisher missile.
            dangerousWeapons.any { it.isFinisherMissile } -> false

            // Received negligible damage.
            damageTracker.damage / effectiveHP < 0.03f -> true

            // Attempt to tank a limited amount of damage. 0.1f may seem like a large fraction,
            // but potential damage calculation is the absolute worst case scenario.
            dangerousWeapons.sumOf { potentialDamage(it) } / effectiveHP < 0.1f -> true

            else -> false
        }
    }

    private fun findDangerousWeapons(): List<WeaponAPI> {
        val enemies: MutableList<ShipAPI> = mutableListOf()
        val obstacles: MutableList<ShipAPI> = mutableListOf()

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

                else -> if (!entity.isFast) obstacles.add(entity)
            }
        }

        return enemies.flatMap { it.allGroupedWeapons }.filter { weapon ->
            when {
                weapon.isDisabled -> false
                weapon.isPermanentlyDisabled -> false

                // Assume the ship can recognize an empty
                // missile launcher, same as the player can.
                weapon.isMissile && weapon.isPermanentlyOutOfAmmo -> false

                !canWeaponHitShip(weapon, obstacles) -> false

                else -> true
            }
        }
    }

    private fun canWeaponHitShip(weapon: WeaponAPI, obstacles: List<ShipAPI>): Boolean {
        val toShip = ship.location - weapon.location
        val distSqr = toShip.lengthSquared
        val dist = sqrt(distSqr) - ship.shieldRadiusEvenIfNoShield / 2

        // Check if the ship is out of weapons range.
        val adjustedMovementTime = if (weapon.ship.engineController.isFlamedOut) 0f else ventTime * shipSpeedFactor
        if (dist - adjustedMovementTime * weapon.ship.maxSpeed > weapon.Range) {
            return false
        }

        // Check if the ship is out of weapons arc.
        val isGuidedFinisherMissile = weapon.isFinisherMissile && !weapon.isUnguidedMissile
        val weaponArc = Arc(weapon.arc, weapon.absoluteArcFacing).extendedBy(adjustedMovementTime * weapon.ship.maxTurnRate)
        if (!isGuidedFinisherMissile && !weaponArc.contains(toShip.facing)) {
            return false
        }

        // Check if projectile will reach the ship during venting.
        val adjustedVentTime = ventTime * ventTimeFactor - weapon.cooldownRemaining
        if (dist / weapon.maxProjectileSpeed > adjustedVentTime) {
            return false
        }

        // Check if the weapon is busy firing at another target.
        if (absShortestRotation(toShip.facing, weapon.currAngle.direction) > 30f && weapon.isInFiringCycle && !isGuidedFinisherMissile) {
            return false
        }

        return obstacles.none { obstacle ->
            val p = weapon.location - obstacle.location

            when {
                // Obstacle does not block its own weapons.
                obstacle.root == weapon.ship.root -> return@none false

                // Weapon can shoot over allies.
                weapon.noFF && obstacle.owner == weapon.ship.owner -> return@none false

                // Special case for frigates. Assume they can quickly
                // maneuver around an obstacle and deliver a torpedo.
                weapon.ship.root.isFrigate && weapon.isFinisherMissile -> return@none false

                // Obstacle is farther than the ship.
                p.lengthSquared > distSqr -> return@none false
            }

            val radius = if (obstacle.isAlive) obstacle.shieldRadiusEvenIfNoShield * 1.5f
            else obstacle.boundsRadius

            distanceToOrigin(p, toShip) <= radius
        }
    }

    private fun potentialDamage(weapon: WeaponAPI): Float {
        val adjustedVentTime = ventTime * ventTimeFactor - weapon.cooldownRemaining
        val rawDamage = max(weapon.derivedStats.dps * adjustedVentTime, weapon.derivedStats.burstDamage)

        val damageMultiplier = when {
            weapon.isFinisherMissile -> 4f

            weapon.damageType == HIGH_EXPLOSIVE -> 2f

            weapon.damageType == FRAGMENTATION -> 0.5f

            else -> 1f
        }

        return rawDamage * damageMultiplier
    }

    /** Determine if ship should forego venting and backing off
     * to instead focus on finishing its target. */
    private fun shouldFinishTarget(): Boolean {
        val target: ShipAPI = ai.attackTarget as? ShipAPI ?: return false

        when {
            target.isFighter -> return false

            target.root.isFrigate -> return false

            // Target is behind healthy shields.
            target.shield?.isOn == true && target.fluxLevel < 0.8f -> return false

            // Target is too far.
            ai.currentEffectiveRange(target) > ai.attackingGroup.effectiveRange -> return false

            // The ship is the victim, not the target.
            target.hullLevel > ship.hullLevel -> return false
        }

        // The more damaged the target, the higher flux level
        // the ship is willing to tolerate.
        val damage = 1 - target.hullLevel
        return ship.FluxLevel < damage * damage * damage
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
        val longestBursts: Float = burstWeapons.filter { it.IsInBurst }.maxOfOrNull { it.burstFireTimeRemaining } ?: 0f

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
