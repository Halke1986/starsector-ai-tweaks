package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.times
import com.genir.aitweaks.core.utils.types.Direction
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
import org.lwjgl.util.vector.Vector2f

class Kinematics(val ship: ShipAPI) {
    // Linear velocity.
    val location: Vector2f
        get() = ship.location

    val velocity: Vector2f
        get() = ship.velocity * timeMult

    val maxSpeed: Float
        get() = ship.maxSpeed * timeMult

    val acceleration: Float
        get() = ship.acceleration * timeMult

    val deceleration: Float
        get() = ship.deceleration * timeMult

    val strafeAcceleration: Float
        get() = acceleration * when (ship.hullSize) {
            ShipAPI.HullSize.FIGHTER -> 0.75f
            ShipAPI.HullSize.FRIGATE -> 1.0f
            ShipAPI.HullSize.DESTROYER -> 0.75f
            ShipAPI.HullSize.CRUISER -> 0.5f
            ShipAPI.HullSize.CAPITAL_SHIP -> 0.25f
            else -> 1.0f
        }

    /** Ship deceleration for collision avoidance purposes. */
    fun collisionDeceleration(collisionFacing: Direction): Float {
        val angleFromBow = (collisionFacing - facing).length
        return when {
            angleFromBow < 30f -> deceleration
            angleFromBow < 150f -> strafeAcceleration
            else -> acceleration
        }
    }

    // Angular velocity.
    val facing: Direction
        get() = ship.facing.direction

    val angularVelocity: Float
        get() = ship.angularVelocity * timeMult

    val maxTurnRate: Float
        get() = ship.maxTurnRate * timeMult

    val turnAcceleration: Float
        get() = ship.turnAcceleration * timeMult

    val turnDeceleration: Float
        get() = ship.turnDeceleration * timeMult

    // Helpers.
    private val timeMult: Float
        get() = ship.mutableStats.timeMult.modifiedValue

    companion object {
        val ShipAPI.kinematics: Kinematics
            get() = Kinematics(this)
    }
}
