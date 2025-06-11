package com.genir.aitweaks.core.shipai.threat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.movement.Kinematics.Companion.kinematics
import com.genir.aitweaks.core.utils.vectorProjectionLength
import kotlin.math.min

class MissileThreat(val ship: ShipAPI) {
    fun potentialDamage(duration: Float): Float {
        val allMissiles: Sequence<MissileAPI> = Global.getCombatEngine().missiles.asSequence()
        val allies: List<ShipAPI> = findAllies()

        val missiles = allMissiles.filter { missile ->
            when {
                !missile.isValidTarget -> false

                missile.isFlare -> false

                missile.damageAmount == 0f -> false

                !missile.isHostile(ship) -> false

                !isShipInRange(missile, duration) -> false

                !isShipTheClosestTarget(missile, allies) -> false

                else -> true
            }
        }

        return missiles.asIterable().sumOf { damage(it) }
    }

    private fun findAllies(): List<ShipAPI> {
        return Global.getCombatEngine().ships.filter { otherShip ->
            when {
                !otherShip.isValidTarget -> false

                otherShip.isFighter -> false

                otherShip.owner != ship.owner -> false

                // Same ship.
                otherShip.root == ship.root -> false

                else -> true
            }
        }
    }

    private fun isShipInRange(missile: MissileAPI, duration: Float): Boolean {
        val flightTimeLeft = missile.maxFlightTime - missile.flightTime
        if (flightTimeLeft <= 0f) {
            return false
        }

        val toMissile = missile.location - ship.location
        val shipSpeedToMissile = vectorProjectionLength(ship.kinematics.velocity, toMissile)
        val approachSpeed = shipSpeedToMissile + missile.maxSpeed

        val timeLeft = min(duration, flightTimeLeft)
        val rangeLeft = approachSpeed * timeLeft
        val dist = toMissile.length - ship.boundsRadius

        return rangeLeft > dist
    }

    private fun isShipTheClosestTarget(missile: MissileAPI, allies: List<ShipAPI>): Boolean {
        val shipDist = effectiveDistance(ship, missile)

        return allies.none { ally ->
            val dist = effectiveDistance(ally, missile)
            return@none dist < shipDist
        }
    }

    private fun damage(missile: MissileAPI): Float {
        val damageMultiplier = when (missile.damageType) {
            DamageType.HIGH_EXPLOSIVE -> 2f

            DamageType.FRAGMENTATION -> 0.25f

            else -> 1f
        }

        return missile.damageAmount * damageMultiplier
    }

    private fun effectiveDistance(target: ShipAPI, missile: MissileAPI): Float {
        val toTarget = target.location - missile.location
        val dist = toTarget.length - ship.boundsRadius
        if (dist > missile.maxRange) {
            return Float.MAX_VALUE
        }

        val angle = (toTarget.facing - missile.facing).length
        val angleNormalized = (1f - angle / 180f)

        return dist / (angleNormalized * angleNormalized)
    }
}
