package com.genir.aitweaks.core.shipai.threat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle
import com.genir.aitweaks.core.shipai.autofire.ballistics.BallisticParams
import com.genir.aitweaks.core.shipai.autofire.ballistics.BallisticTarget
import com.genir.aitweaks.core.shipai.autofire.ballistics.closestHitRange
import com.genir.aitweaks.core.shipai.movement.Kinematics
import com.genir.aitweaks.core.shipai.movement.Kinematics.Companion.kinematics
import com.genir.aitweaks.core.utils.distanceToOrigin
import com.genir.aitweaks.core.utils.solve
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
import com.genir.aitweaks.core.utils.types.LinearMotion
import kotlin.math.max

class WeaponThreat(private val ship: ShipAPI) {
    data class Damage(
        val finisherMissileDanger: Boolean,
        val damage: Float,
    )

    fun potentialDamage(duration: Float): Damage {
        val dangerousWeapons = findDangerousWeapons(duration)

        return Damage(
            finisherMissileDanger = dangerousWeapons.any { weapon ->
                weapon.isFinisherMissile
            },

            damage = dangerousWeapons.sumOf { weapon ->
                potentialDamage(duration, weapon)
            }
        )
    }

    private fun findDangerousWeapons(duration: Float): List<WeaponHandle> {
        val enemies: MutableList<ShipAPI> = mutableListOf()
        val obstacles: MutableList<ShipAPI> = mutableListOf()

        Global.getCombatEngine().ships.asSequence().forEach { entity ->
            if (entity.root == ship.root) {
                return@forEach
            }

            if (entity.isFighter) {
                return@forEach
            }

            if (!entity.isFast) {
                obstacles.add(entity)
            }

            if (entity.isHostile(ship)) {
                // Don't consider overloaded or venting enemies.
                if (entity.offlineTimeRemaining <= duration) {
                    enemies.add(entity)
                }
            }
        }

        val allEnemyWeapons = enemies.flatMap { it.allWeapons.map { weaponAPI -> weaponAPI.handle } }

        return allEnemyWeapons.filter { weapon ->
            when {
                weapon.isDisabled -> false

                weapon.isPermanentlyDisabled -> false

                // Non-weapons, such as decoratives.
                weapon.derivedStats.dps == 0f -> false

                // Assume the ship can recognize an empty
                // missile launcher, same as the player can.
                weapon.isMissile && weapon.isPermanentlyOutOfAmmo -> false

                !canWeaponHitShip(duration, weapon, obstacles) -> false

                else -> true
            }
        }
    }

    private fun canWeaponHitShip(duration: Float, weapon: WeaponHandle, obstacles: List<ShipAPI>): Boolean {
        val enemy: Kinematics = weapon.ship.kinematics
        val toShip = ship.location - weapon.location
        val distSqr = toShip.lengthSquared

        // Check if projectile will reach the ship during venting.
        val attackStart = max(
            weapon.cooldownRemaining,
            weapon.ship.offlineTimeRemaining,
        )

        val timeToRange = timeToHit(
            weapon,
            BallisticTarget.shieldRadius(ship),
            weapon.engagementRange,
            weapon.maxProjectileSpeed,
            BallisticParams(accuracy = 1f, delay = attackStart),
        )

        if (timeToRange > duration) {
            return false
        }

        // Check if the ship is out of weapons arc.
        val isGuidedFinisherMissile = weapon.isFinisherMissile && !weapon.isUnguidedMissile
        val weaponArc = weapon.absoluteArc.extendedBy(duration * enemy.maxTurnRate)
        if (!isGuidedFinisherMissile && !weaponArc.contains(toShip.facing)) {
            return false
        }

        // Check if the weapon is busy firing at another target.
        if ((weapon.currAngle.direction - toShip.facing).length > 30f && weapon.isInFiringCycle && !isGuidedFinisherMissile) {
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

    /** Time after which projectile fired by the weapon can hit the target. */
    private fun timeToHit(weapon: WeaponHandle, target: BallisticTarget, range: Float, projectileSpeed: Float, params: BallisticParams): Float {
        if (range <= 0) {
            return 0f
        }

        val currentRange = closestHitRange(weapon, target, params)

        when {
            // Already in range. Assume weapon fires immediately
            // (while accounting for delay parameter).
            currentRange <= range -> {
                return params.delay + currentRange / projectileSpeed
            }

            // Not possible to hit the target if
            // it's faster than the projectile.
            currentRange == Float.POSITIVE_INFINITY -> {
                return Float.POSITIVE_INFINITY
            }
        }

        // Target motion in weapon frame of reference. The projectile velocity
        // is not relevant in the following calculation, therefore the target
        // velocity is not normalized to the projectile speed.
        val vAbs = target.velocity - weapon.ship.velocity
        val targetMotion = LinearMotion(
            position = target.location - weapon.location + vAbs * params.delay,
            velocity = vAbs,
        )

        // Time after which the target crosses the weapon range radius.
        // If the equation has no positive solutions, the target will
        // never cross the radius.
        val timeToCross = solve(targetMotion, range + target.radius)?.smallerNonNegative
            ?: return Float.POSITIVE_INFINITY

        // Time it takes the projectile to reach the weapon range radius.
        val projectileFlightTime = (range - weapon.projectileSpawnOffset) / weapon.projectileSpeed

        // Target began inside range but moving away; the earlier
        // currentRange <= range would have caught any hittable case.
        if (timeToCross < projectileFlightTime) {
            return Float.POSITIVE_INFINITY
        }

        // Weapon should be fired in advance of the target entering the range
        // threshold, so that the projectile meets it at the edge of its range.
        return params.delay + timeToCross
    }

    private fun potentialDamage(duration: Float, weapon: WeaponHandle): Float {
        val adjustedVentTime = duration - weapon.cooldownRemaining
        val rawDamage = max(weapon.derivedStats.dps * adjustedVentTime, weapon.derivedStats.burstDamage)

        val damageMultiplier = when {
            weapon.isFinisherMissile -> 8f

            weapon.damageType == DamageType.HIGH_EXPLOSIVE -> 2f

            weapon.damageType == DamageType.FRAGMENTATION -> 0.5f

            else -> 1f
        }

        return rawDamage * damageMultiplier
    }

    private val WeaponHandle.isFinisherMissile: Boolean
        get() = isMissile && damageType == DamageType.HIGH_EXPLOSIVE

    private val WeaponHandle.maxProjectileSpeed: Float
        get() {
            val missileSpec: MissileSpecAPI = spec.projectileSpec as? MissileSpecAPI ?: return projectileSpeed
            val engineSpec: ShipHullSpecAPI.EngineSpecAPI = missileSpec.hullSpec.engineSpec
            val maxSpeedStat = MutableStat(engineSpec.maxSpeed)
            return maxSpeedStat.modifiedValue
        }
}
