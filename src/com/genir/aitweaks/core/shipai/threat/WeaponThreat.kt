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
import com.genir.aitweaks.core.utils.distanceToOrigin
import com.genir.aitweaks.core.utils.sqrt
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
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
            when {
                entity.root == ship.root -> Unit
                entity.isFighter -> Unit

                entity.isHostile(ship) -> {
                    // Don't consider overloaded or venting enemies.
                    val tracker = entity.fluxTracker
                    when {
                        !tracker.isOverloadedOrVenting -> enemies.add(entity)
                        duration - 2f > max(tracker.timeToVent, tracker.overloadTimeRemaining) -> enemies.add(entity)
                    }

                    // Consider slow enemies as weapon obstacles.
                    if (!entity.isFast) obstacles.add(entity)
                }

                else -> if (!entity.isFast) obstacles.add(entity)
            }
        }

        return enemies.flatMap { it.allWeapons.map { weaponAPI -> weaponAPI.handle } }.filter { weapon ->
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
        val toShip = ship.location - weapon.location
        val distSqr = toShip.lengthSquared
        val dist = sqrt(distSqr) - ship.shieldRadiusEvenIfNoShield / 2

        // Check if the ship is out of weapons range.
        val adjustedMovementTime = if (weapon.ship.engineController.isFlamedOut) 0f else duration
        if (dist - adjustedMovementTime * weapon.ship.maxSpeed > weapon.totalRange) {
            return false
        }

        // Check if the ship is out of weapons arc.
        val isGuidedFinisherMissile = weapon.isFinisherMissile && !weapon.isUnguidedMissile
        val weaponArc = weapon.absoluteArc.extendedBy(adjustedMovementTime * weapon.ship.maxTurnRate)
        if (!isGuidedFinisherMissile && !weaponArc.contains(toShip.facing)) {
            return false
        }

        // Check if projectile will reach the ship during venting.
        val adjustedVentTime = duration - weapon.cooldownRemaining
        if (dist / weapon.maxProjectileSpeed > adjustedVentTime) {
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
