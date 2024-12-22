package com.genir.aitweaks.core.shipai.systems

import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.ACTIVE
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.IDLE
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.AttackCoord
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.Preset.Companion.backoffUpperThreshold
import com.genir.aitweaks.core.utils.*
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lwjgl.util.vector.Vector2f
import kotlin.math.min

/** Burn Drive AI. It replaces the vanilla implementation in ships with custom AI. */
class BurnDriveToggle(ai: CustomShipAI) : SystemAI(ai), AttackCoord.Coordinable {
    private var headingPoint: Vector2f = Vector2f()
    private var shouldBurn = false

    // Used for communication with attack coordinator.
    override var proposedHeadingPoint: Vector2f? = null
    override var reviewedHeadingPoint: Vector2f? = null

    private val burnDriveFlatBonus: Float = 200f

    private var destinationDist: Float = 0f
    private var destinationFacing: Float = 0f

    // Stats // TODO should refresh
    private var maxSpeed: Float = Float.MAX_VALUE
    private var maxBurnDist: Float = 0f

    @Suppress("ConstPropertyName")
    companion object Preset {
        const val approachToMinRangeFraction = 0.75f
        const val maxAngleToTarget = 45f
        const val stopBeforeCollision = 0.2f // seconds
        const val ignoreMassFraction = 0.25f
        const val minBurnDistFraction = 0.33f
    }

    override fun advance(dt: Float) {
        updateMaxBurnDist()
        updateHeadingPoint()
        updateShouldBurn()
        triggerSystem()
    }

    override fun overrideHeading(): Vector2f? {
        return if (shouldBurn) headingPoint
        else null
    }

    override fun overrideFacing(): Float? {
        return if (shouldBurn) (headingPoint - ship.location).facing
        else null
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
            ai.assignment.navigateTo != null -> {
                ai.assignment.navigateTo
            }

            // Charge straight at the maneuver target, disregard fleet coordination.
            ai.maneuverTarget != null -> {
                val vectorToTarget = ai.maneuverTarget!!.location - ship.location
                val distance = ai.vanilla.flags.get<Float>(AIFlags.MANEUVER_RANGE_FROM_TARGET)
                    ?: (ai.attackingGroup.minRange * approachToMinRangeFraction)
                val approachVector = vectorToTarget.addLength(-distance)

                // Let the attack coordinator review the calculated heading point.
                proposedHeadingPoint = approachVector + ship.location
                reviewedHeadingPoint
            }

            else -> null
        }

        // Calculate burn parameters.
        if (newDestination != null) {
            val toDestination = newDestination - ship.location
            headingPoint = toDestination + ship.location
            destinationDist = toDestination.length()
            destinationFacing = absShortestRotation(toDestination.facing, ship.facing)
        } else {
            headingPoint = Vector2f()
            destinationDist = 0f
            destinationFacing = 0f
        }
    }

    private fun updateShouldBurn() {
        shouldBurn = when {
            system.state != IDLE -> false

            !ship.canUseSystemThisFrame() -> false

            headingPoint.isZero -> false

            ai.backoff.isBackingOff -> false

            // Don't burn to move order location if not facing the location.
            ai.assignment.navigateTo != null && destinationFacing > maxAngleToTarget -> false

            // Don't burn to maneuver target if it's different from the attack target.
            ai.maneuverTarget != null && ai.maneuverTarget != ai.attackTarget -> false

            // Don't burn to destination if it's too close.
            destinationDist < maxBurnDist * minBurnDistFraction -> false

            // Don't burn to attack target when high on flux.
            ai.assignment.navigateTo == null && ship.fluxLevel > backoffUpperThreshold * 0.75f -> false

            !isRouteClear() -> false

            else -> true
        }
    }

    private fun triggerSystem() {
        val shouldTrigger = when {
            // Launch.
            shouldBurn && destinationFacing < 0.1f -> true

            // Not burning, no need to abort.
            system.state != ACTIVE -> false

            // No target, stop.
            ai.assignment.navigateTo == null && ai.maneuverTarget == null -> true

            // Veered off course, stop.
            destinationFacing > maxAngleToTarget -> true

            // Avoid collisions.
            isCollisionImminent() -> true

            else -> false
        }

        if (shouldTrigger) ship.command(ShipCommand.USE_SYSTEM)
    }

    private fun findObstacles(center: Vector2f, radius: Float): Sequence<ShipAPI> {
        return Grid.ships(center, radius).filter {
            when {
                // Self
                it == ship -> false

                // Fighters
                it.collisionClass != CollisionClass.SHIP -> false

                // Allies
                it.owner == ship.owner -> true

                // Equal or larger hulls. Hitting smaller hulls will not cause flameout.
                it.root.hullSize.ordinal >= ship.hullSize.ordinal -> true

                // Heavy obstacles.
                it.mass >= ship.mass * ignoreMassFraction -> true

                else -> false
            }
        }
    }

    private fun isRouteClear(): Boolean {
        val toDestination = headingPoint - ship.location
        val dist = destinationDist.coerceAtMost(maxBurnDist)
        val position = ship.location + toDestination.resized(dist) / 2f
        val obstacles = findObstacles(position, dist / 2f)

        val maxBurnDuration = system.chargeActiveDur + system.chargeUpDur + system.chargeDownDur
        val timeToTarget = maxBurnDuration * (dist / maxBurnDist)
        val effectiveSpeed = maxBurnDist / maxBurnDuration

        val collisionBuffer = com.genir.aitweaks.core.shipai.Preset.collisionBuffer
        return timeToTarget < timeToCollision(obstacles, toDestination.resized(effectiveSpeed), collisionBuffer)
    }

    private fun isCollisionImminent(): Boolean {
        val radius = maxBurnDist / 2f
        val position = ship.location + unitVector(ship.facing) * radius
        val obstacles = findObstacles(position, radius)

        return timeToCollision(obstacles, ship.velocity, 0f) <= stopBeforeCollision
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
