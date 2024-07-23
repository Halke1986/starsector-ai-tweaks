package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.ACTIVE
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.IDLE
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.addLength
import com.genir.aitweaks.core.utils.extensions.resized
import com.genir.aitweaks.core.utils.extensions.rootModule
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.min

/** Burn Drive AI. It replaces the vanilla implementation in ships with custom AI. */
class BurnDrive(val ship: ShipAPI, override val ai: AI) : Coordinable {
    // Used by Movement class to align ship for burn.
    internal var destination: Vector2f = Vector2f()
    internal var shouldBurn = false

    // Used for communication with attack coordinator.
    override var proposedHeadingPoint: Vector2f? = null
    override var reviewedHeadingPoint: Vector2f? = null

    private val system: ShipSystemAPI = ship.system
    private val burnDriveFlatBonus: Float = 200f

    private var destinationDist: Float = 0f
    private var destinationFacing: Float = 0f

    // Stats
    private var maxSpeed: Float = Float.MAX_VALUE
    private var maxBurnDist: Float = 0f

    fun advance(dt: Float) {
        updateMaxBurnDist()
        updateHeadingPoint()
        updateShouldBurn()
        triggerSystem(dt)
    }

    private fun updateMaxBurnDist() {
        // Try to get the unmodified max speed, without burn drive bonus.
        maxSpeed = min(maxSpeed, ship.engineController.maxSpeedWithoutBoost)

        val effectiveBurnDuration = system.chargeActiveDur + (system.chargeUpDur + system.chargeDownDur) / 2f
        maxBurnDist = (maxSpeed + burnDriveFlatBonus) * effectiveBurnDuration
    }

    private fun updateHeadingPoint() {
        // Reset attack coordinator communication.
        val reviewedHeadingPoint = this.reviewedHeadingPoint
        this.proposedHeadingPoint = null
        this.reviewedHeadingPoint = null

        // Choose new burn destination.
        val newDestination = when {
            ai.moveOrderLocation != null -> {
                ai.moveOrderLocation
            }

            // Charge straight at the maneuver target, disregard fleet coordination.
            ai.maneuverTarget != null -> {
                val vectorToTarget = ai.maneuverTarget!!.location - ship.location
                val approachVector = vectorToTarget.addLength(-ai.stats.minRange * Preset.BurnDrive.approachToMinRangeFraction)

                // Let the attack coordinator review the calculated heading point.
                proposedHeadingPoint = approachVector + ship.location
                reviewedHeadingPoint
            }

            else -> null
        }

        // Calculate burn parameters.
        if (newDestination != null) {
            val toDestination = newDestination - ship.location
            destination = toDestination + ship.location
            destinationDist = toDestination.length()
            destinationFacing = abs(MathUtils.getShortestRotation(toDestination.getFacing(), ship.facing))
        } else {
            destination = Vector2f()
            destinationDist = 0f
            destinationFacing = 0f
        }
    }

    private fun updateShouldBurn() {
        shouldBurn = when {
            system.state != IDLE -> false

            !ship.canUseSystemThisFrame() -> false

            destination.isZeroVector() -> false

            ai.isBackingOff -> false

            // Don't burn to move order location if not facing the location.
            ai.moveOrderLocation != null && destinationFacing > Preset.BurnDrive.maxAngleToTarget -> false

            // Don't burn to maneuver target if it's different from the attack target.
            ai.maneuverTarget != null && ai.maneuverTarget != ai.attackTarget -> false

            // Don't burn to destination if it's too close.
            destinationDist < maxBurnDist * Preset.BurnDrive.minBurnDistFraction -> false

            !isRouteClear() -> false

            else -> true
        }
    }

    private fun triggerSystem(dt: Float) {
        val shouldTrigger = when {
            // Launch.
            shouldBurn && destinationFacing < 0.1f -> true

            // Not burning, no need to abort.
            system.state != ACTIVE -> false

            // No target, stop.
            ai.moveOrderLocation == null && ai.maneuverTarget == null -> true

            // Veered off course, stop.
            destinationFacing > Preset.BurnDrive.maxAngleToTarget -> true

            // Avoid collisions.
            isCollisionImminent() -> true

            else -> false
        }

        if (shouldTrigger) ship.command(ShipCommand.USE_SYSTEM)
    }

    private fun findObstacles(center: Vector2f, radius: Float): Sequence<ShipAPI> {
        return shipSequence(center, radius).filter {
            when {
                // Self
                it == ship -> false

                // Fighters
                it.collisionClass != CollisionClass.SHIP -> false

                // Allies
                it.owner == ship.owner -> true

                // Equal or larger hulls. Hitting smaller hulls will not cause flameout.
                it.rootModule.hullSize.ordinal >= ship.hullSize.ordinal -> true

                // Heavy obstacles.
                it.mass >= ship.mass * Preset.BurnDrive.ignoreMassFraction -> true

                else -> false
            }
        }
    }

    private fun isRouteClear(): Boolean {
        val toDestination = destination - ship.location
        val dist = destinationDist.coerceAtMost(maxBurnDist)
        val position = ship.location + toDestination.resized(dist) / 2f
        val obstacles = findObstacles(position, dist / 2f)

        val maxBurnDuration = system.chargeActiveDur + system.chargeUpDur + system.chargeDownDur
        val timeToTarget = maxBurnDuration * (dist / maxBurnDist)
        val effectiveSpeed = maxBurnDist / maxBurnDuration

        return timeToTarget < timeToCollision(obstacles, toDestination.resized(effectiveSpeed), Preset.collisionBuffer)
    }

    private fun isCollisionImminent(): Boolean {
        val radius = maxBurnDist / 2f
        val position = ship.location + unitVector(ship.facing) * radius
        val obstacles = findObstacles(position, radius)

        return timeToCollision(obstacles, ship.velocity, 0f) <= Preset.BurnDrive.stopBeforeCollision
    }

    private fun timeToCollision(obstacles: Sequence<ShipAPI>, shipVelocity: Vector2f, buffer: Float): Float {
        return obstacles.mapNotNull { obstacle ->
            val p = obstacle.location - ship.location
            val v = obstacle.velocity - shipVelocity
            val r = ship.totalCollisionRadius + obstacle.totalCollisionRadius + buffer

            // Calculate time to collision.
            if (p.lengthSquared() <= r * r) 0f
            else solve(Pair(p, v), r)
        }.minOrNull() ?: Float.MAX_VALUE
    }
}