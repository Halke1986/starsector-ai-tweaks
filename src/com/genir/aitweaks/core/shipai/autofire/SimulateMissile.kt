package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.combat.ShipHullSpecAPI.EngineSpecAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.getShortestRotation
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max

// TODO enable for AI (Perdition specifically)

class SimulateMissile {
    data class Frame(val velocity: Vector2f, val location: Vector2f)

    companion object {

        /** Iteratively calculate the intercept point for dumb-fire missile weapon.
         * Note: the operation is computationally expensive. */
        fun missileIntercept(weapon: WeaponAPI, target: BallisticTarget): Vector2f {
            // Missile path is always computed using global time rate;
            // make sure not to use ship-specific time rate.
            val dt: Float = Global.getCombatEngine().elapsedInLastFrame
            val missileStats = MissileStats(weapon)
            var facing: Direction = (target.location - weapon.location).facing

            for (i in 0..3) {
                val path: Sequence<Frame> = missilePath(dt, weapon, facing.unitVector, missileStats)
                val error: Direction = angularDistanceToPath(dt, weapon, target, path)
                facing += error
            }

            return facing.unitVector * (target.location - weapon.location).length
        }

        fun missilePath(weapon: WeaponAPI): Sequence<Frame> {
            val dt: Float = Global.getCombatEngine().elapsedInLastFrame
            return missilePath(dt, weapon, weapon.currAngle.direction.unitVector, MissileStats(weapon))
        }

        /** Calculate the angular distance between missile path and
         * target location at the point where the two are the closest. */
        private fun angularDistanceToPath(dt: Float, weapon: WeaponAPI, target: BallisticTarget, path: Sequence<Frame>): Direction {
            val p0: Vector2f = target.location
            val v: Vector2f = target.velocity * dt

            var minDist: Float = Float.MAX_VALUE
            var rotation = Direction(0f)

            path.forEachIndexed { idx, frame ->
                val p = p0 + v * idx.toFloat()
                val dist = (p - frame.location).lengthSquared

                if (dist < minDist) {
                    minDist = dist
                    rotation = getShortestRotation(frame.location, weapon.location, p)
                }
            }

            return rotation
        }

        /** Predict the entire path of a missile, given weapon facing,
         * starting from the weapon barrel location. */
        private fun missilePath(dt: Float, weapon: WeaponAPI, facingVector: Vector2f, missileStats: MissileStats): Sequence<Frame> {
            val p0: Vector2f = weapon.location + weapon.barrelOffset(facingVector)
            val vMax: Float = missileStats.maxSpeed * dt
            val v0: Vector2f = (weapon.ship.velocity + facingVector * missileStats.launchSpeed) * dt
            val a: Vector2f = facingVector * missileStats.acceleration * dt * dt
            val decel: Float = max(missileStats.acceleration, missileStats.deceleration) * 2f * dt * dt

            return generateSequence(Frame(v0, p0)) {
                val v2: Vector2f = it.velocity + a
                val speed: Float = v2.length
                Frame(
                    if (speed <= vMax) v2 else v2.resized(max(vMax, speed - decel)),
                    it.location + it.velocity,
                )
            }.take((missileStats.maxFlightTime / dt).toInt())
        }

        /** Calculate the barrel offset for the weapon, given weapon facing.
         * For multi-barreled weapons, average offset is returned. */
        private fun WeaponAPI.barrelOffset(facingVector: Vector2f): Vector2f {
            val offsets: List<Vector2f> = when {
                slot.isHardpoint -> spec.hardpointFireOffsets
                slot.isTurret -> spec.turretFireOffsets
                else -> listOf()
            }

            val offsetSum: Vector2f = offsets.fold(Vector2f()) { sum, offset -> sum + offset }
            val average: Vector2f = offsetSum / offsets.size.toFloat()

            return facingVector * average.length
        }

        /** Missile stats after applying ship bonuses. */
        private class MissileStats(weapon: WeaponAPI) {
            val maxSpeed: Float
            val acceleration: Float
            val deceleration: Float
            val launchSpeed: Float
            val maxFlightTime: Float

            init {
                val missileSpec: MissileSpecAPI = weapon.spec.projectileSpec as MissileSpecAPI
                val engineSpec: EngineSpecAPI = missileSpec.hullSpec.engineSpec
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
}
