package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.div
import com.genir.aitweaks.core.utils.extensions.facing
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.lengthSquared
import com.genir.aitweaks.core.utils.extensions.resized
import com.genir.aitweaks.core.utils.getShortestRotation
import com.genir.aitweaks.core.utils.times
import com.genir.aitweaks.core.utils.unitVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max
import com.fs.starfarer.combat.entities.ship.OOoOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO as EngineStats
import com.fs.starfarer.combat.entities.ship.`class` as MutableShipStats
import com.fs.starfarer.loading.specs.o00o as MissileSpec

class SimulateMissile {
    data class Frame(val velocity: Vector2f, val location: Vector2f)

    companion object {

        /** Iteratively calculate the intercept point for dumb-fire missile weapon.
         * Note: the operation is computationally expensive. */
        fun missileIntercept(dt: Float, weapon: WeaponAPI, target: BallisticTarget): Vector2f {
            var facing: Float = (target.location - weapon.location).facing

            for (i in 0..3) {
                val path: Sequence<Frame> = missilePath(dt, weapon, unitVector(facing))
                val error: Float = angularDistanceToPath(dt, weapon, target, path)
                facing += error
            }

            return unitVector(facing) * (target.location - weapon.location).length + weapon.location
        }

        /** Calculate the angular distance between missile path and
         * target location at the point where the two are the closest. */
        private fun angularDistanceToPath(dt: Float, weapon: WeaponAPI, target: BallisticTarget, path: Sequence<Frame>): Float {
            val p0: Vector2f = target.location
            val v: Vector2f = target.velocity * dt

            var minDist: Float = Float.MAX_VALUE
            var rotation = 0f

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
        fun missilePath(dt: Float, weapon: WeaponAPI, facingVector: Vector2f): Sequence<Frame> {
            val projectileSpec: MissileSpec = weapon.spec.projectileSpec as MissileSpec
            val shipStats: MutableShipStatsAPI = weapon.ship.mutableStats
            val engineStats: EngineStats = MutableShipStats.create(projectileSpec)

            engineStats.maxSpeed.applyMods(shipStats.missileMaxSpeedBonus);
            engineStats.acceleration.applyMods(shipStats.missileAccelerationBonus);
            engineStats.deceleration.applyMods(shipStats.missileAccelerationBonus);

            val p0: Vector2f = weapon.location + weapon.barrelOffset(facingVector)
            val vMax: Float = engineStats.maxSpeed.modifiedValue * dt
            val v0: Vector2f = (weapon.ship.velocity + facingVector * projectileSpec.launchSpeed) * dt
            val a: Vector2f = facingVector * engineStats.acceleration.modifiedValue * dt * dt
            val decel: Float = max(engineStats.acceleration.modifiedValue, engineStats.deceleration.modifiedValue) * 2f * dt * dt

            return generateSequence(Frame(v0, p0)) {
                val v2: Vector2f = it.velocity + a
                val speed: Float = v2.length
                Frame(
                    if (speed <= vMax) v2 else v2.resized(max(vMax, speed - decel)),
                    it.location + it.velocity,
                )
            }.take((projectileSpec.maxFlightTime / dt).toInt())
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
    }
}
