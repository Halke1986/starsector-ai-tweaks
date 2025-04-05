package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import org.lwjgl.util.vector.Vector2f

class Helm(val ship: ShipAPI) {
    // Command.
    val commands: MutableSet<ShipCommand> = mutableSetOf()

    fun advance() {
        commands.clear()
    }

    fun executeCommands() {
        commands.forEach { cmd ->
            ship.giveCommand(cmd, null, 0)
        }
    }

    fun command(cmd: ShipCommand) {
        commands.add(cmd)
    }

    // Linear velocity.
    val velocity: Vector2f
        get() = ship.velocity

    val location: Vector2f
        get() = ship.location

    val maxSpeed: Float
        get() = ship.maxSpeed

    val acceleration: Float
        get() = ship.acceleration

    val deceleration: Float
        get() = ship.deceleration

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
    val angularVelocity: Float
        get() = ship.angularVelocity

    val facing: Direction
        get() = ship.facing.direction

    val turnAcceleration: Float
        get() = ship.turnAcceleration

    val turnDeceleration: Float
        get() = ship.turnDeceleration

    companion object {
        val ShipAPI.helm: Helm
            get() = Helm(this)
    }
}
