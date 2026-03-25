package com.genir.aitweaks.core.shipai.autofire.ballistics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotated
import org.lwjgl.util.vector.Vector2f

class Missile(private val weapon: WeaponHandle) : Projectile(weapon) {
    /** Iteratively calculate the intercept point for dumb-fire missile weapon.
     * Note: the operation is computationally expensive. */
    override fun intercept(target: BallisticTarget, params: BallisticParams): Vector2f {
        val missileSpec: MissileSpecAPI = weapon.spec?.projectileSpec as? MissileSpecAPI
            ?: return target.location - weapon.location

        // Missile path is always computed using global time rate;
        // make sure not to use ship-specific time rate.
        val dt: Float = Global.getCombatEngine().elapsedInLastFrame
        val missileStats = MissileStats(weapon, missileSpec)

        var facing: Direction = super.intercept(target, params).facing
        for (i in 0..5) {
            val error: Direction = angularDistanceToPath(dt, facing, target, missileStats)
            facing -= error
            if (error.length < 1f) {
                break
            }
        }

        return facing.unitVector * (target.location - weapon.location).length
    }

    /** Calculate the angular distance between missile path and
     * target location at the point where the two are the closest. */
    private fun angularDistanceToPath(dt: Float, weaponFacing: Direction, target: BallisticTarget, missileStats: MissileStats): Direction {
        val vMax: Float = missileStats.maxSpeed * dt
        val decel: Float = maxOf(missileStats.acceleration, missileStats.deceleration) * 2f * dt * dt
        val steps: Int = (missileStats.maxFlightTime / dt).toInt()
        val a: Vector2f = weaponFacing.unitVector * missileStats.acceleration * dt * dt

        val pZero: Vector2f = weapon.location + weapon.barrelOffset(weaponFacing)

        val pTarget: Vector2f = target.location - pZero
        val vTarget: Vector2f = target.velocity * dt

        val pMissile: Vector2f = Vector2f()
        val vMissile: Vector2f = (weapon.ship.velocity + weaponFacing.unitVector * missileStats.launchSpeed) * dt

        val offset: Vector2f = pTarget - pMissile
        var minDist: Float = offset.lengthSquared

        var wasDistanceClosing = false

        for (i in 0..steps) {
            offset.x = pTarget.x - pMissile.x
            offset.y = pTarget.y - pMissile.y

            val dist: Float = offset.lengthSquared
            if (dist < minDist) {
                minDist = dist
                wasDistanceClosing = true
            } else if (wasDistanceClosing) {
                // Reached the minimum distance.
                return pMissile.facing - pTarget.facing
            } else {
                // While the missile is still accelerating, its distance to the target can increase.
                // Do not treat this as divergence; wait until the distance begins decreasing.
            }

            vMissile.x += a.x
            vMissile.y += a.y

            val speed: Float = vMissile.length
            if (speed > vMax) {
                val cappedSpeed: Float = maxOf(vMax, speed - decel)
                val scale: Float = cappedSpeed / speed

                vMissile.x *= scale
                vMissile.y *= scale
            }

            pMissile.x += vMissile.x
            pMissile.y += vMissile.y

            pTarget.x += vTarget.x
            pTarget.y += vTarget.y
        }

        return pMissile.facing - pTarget.facing
    }

    /** Calculate the barrel offset for the weapon, given weapon facing.
     * For multi-barreled weapons, average offset is returned. */
    private fun WeaponHandle.barrelOffset(weaponFacing: Direction): Vector2f {
        return barrelOffset.rotated(weaponFacing.rotationMatrix)
    }

    /** Missile stats after applying ship bonuses. */
    private class MissileStats(weapon: WeaponHandle, missileSpec: MissileSpecAPI) {
        val maxSpeed: Float
        val acceleration: Float
        val deceleration: Float
        val launchSpeed: Float
        val maxFlightTime: Float

        init {
            val engineSpec: ShipHullSpecAPI.EngineSpecAPI = missileSpec.hullSpec.engineSpec
            val shipStats: MutableShipStatsAPI = weapon.ship.mutableStats

            val maxSpeedStat = MutableStat(engineSpec.maxSpeed)
            val accelerationStat = MutableStat(engineSpec.acceleration)
            val decelerationStat = MutableStat(engineSpec.deceleration)

            maxSpeedStat.applyMods(shipStats.missileMaxSpeedBonus)
            accelerationStat.applyMods(shipStats.missileAccelerationBonus)
            decelerationStat.applyMods(shipStats.missileAccelerationBonus)

            maxSpeed = maxSpeedStat.modifiedValue
            acceleration = accelerationStat.modifiedValue
            deceleration = decelerationStat.modifiedValue

            launchSpeed = missileSpec.launchSpeed
            maxFlightTime = missileSpec.maxFlightTime
        }
    }
}