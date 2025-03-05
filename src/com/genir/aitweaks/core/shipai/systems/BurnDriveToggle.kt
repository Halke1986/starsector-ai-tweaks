package com.genir.aitweaks.core.shipai.systems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.*
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.Preset.Companion.backoffUpperThreshold
import com.genir.aitweaks.core.utils.Direction
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.Grid
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.aitweaks.core.utils.solve
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lwjgl.util.vector.Vector2f
import kotlin.math.min

/** Burn Drive AI. It replaces the vanilla implementation for ships with custom AI. */
class BurnDriveToggle(ai: CustomShipAI) : SystemAI(ai) {
    private val updateInterval: IntervalUtil = defaultAIInterval()

    private var burnVector: Vector2f = Vector2f() // In ship frame of reference.
    private var shouldInitBurn: Boolean = false
    private var prevAngleToDestination: Direction = 0f.direction

    private val burnDriveFlatBonus: Float = 200f // Hardcoded vanilla value.
    private var maxBurnDist: Float = 0f

    companion object Preset {
        const val maxAngleToTarget = 45f
        const val stopBeforeCollision = 0.2f // seconds
        const val ignoreMassFraction = 0.4f
        const val minBurnDistFraction = 0.33f
        const val maxFluxLevel = backoffUpperThreshold * 0.7f
    }

    override fun advance(dt: Float) {
        updateBurnVector()

        updateInterval.advance(dt)
        if (updateInterval.intervalElapsed()) {
            updateMaxBurnDist()
            updateShouldInitBurn()
            triggerSystem()
        }

        prevAngleToDestination = angleToDestination()
    }

    override fun overrideFacing(): Direction? {
        return if (shouldInitBurn) burnVector.facing
        else null
    }

    override fun holdTargets(): Boolean {
        return false
    }

    private fun updateMaxBurnDist() {
        val effectiveBurnDuration = system.chargeActiveDur + (system.chargeUpDur + system.chargeDownDur) / 2f
        maxBurnDist = (ship.baseMaxSpeed + burnDriveFlatBonus) * effectiveBurnDuration
    }

    /** The burn drive aligns with the ship’s heading, assuming it was
     * calculated specifically for an assault-type ship. In other words,
     * it drives the ship straight at the target without strafing. */
    private fun updateBurnVector() {
        burnVector = ai.movement.headingPoint - ship.location
    }

    /** Should the ship position itself to begin burn? */
    private fun updateShouldInitBurn() {
        shouldInitBurn = when {
            system.state != IDLE -> false

            !ship.canUseSystemThisFrame() -> false

            burnVector.isZero -> false

            ai.ventModule.isBackingOff -> false

            // Don't burn if not facing the burn destination, as this may lead
            // to interrupting an attack. Frigates may be ignored.
            angleToDestination().length > maxAngleToTarget && (ai.attackTarget as? ShipAPI)?.root?.isFrigate != true -> false

            // Don't burn to destination if it's too close.
            burnVector.length < maxBurnDist * minBurnDistFraction -> false

            // Don't burn to maneuver target when high on flux.
            ai.assignment.navigateTo == null && ship.FluxLevel > maxFluxLevel -> false

            !isOnCourse() -> false

            !isRouteClear() -> false

            else -> true
        }
    }

    private fun triggerSystem() {
        val isIdle = system.state == IDLE
        val isOn = system.state == IN || system.state == ACTIVE

        val shouldTrigger: Boolean = when {
            isIdle && shouldInitBurn && isFacingBurnVector() -> true

            isOn && !isOnCourse() -> true

            isOn && isCollisionImminent() -> true

            else -> false
        }

        if (shouldTrigger) ship.command(ShipCommand.USE_SYSTEM)
    }

    private fun isFacingBurnVector(): Boolean {
        val angle = angleToDestination()
        val minRecentAngle = min(angle.length, prevAngleToDestination.length)

        return when {
            angle.length < 0.1f -> true

            // Ship crossed the burn vector.
            minRecentAngle < 1f && angle.sign != prevAngleToDestination.sign -> true

            else -> false
        }
    }

    /** Verify if the ship is heading towards the intended target.
     * Note that the intended target may change during the burn. */
    private fun isOnCourse(): Boolean {
        // Heading to assignment location takes priority.
        val assignment: Vector2f? = ai.assignment.navigateTo
        if (assignment != null) {
            return (ship.facing.direction - (assignment - ship.location).facing).length <= maxAngleToTarget
        }

        // Assume the maneuver target is the closest relevant enemy ship.
        val maneuverTarget: Vector2f? = ai.maneuverTarget?.location
        if (maneuverTarget != null) {
            val toTarget = maneuverTarget - ship.location
            val expectedRange = ai.attackRange

            // Too close to nearest enemy.
            if (toTarget.length < expectedRange) {
                return false
            }

            // Ship is approaching the nearest enemy. Continue,
            // even if the ship is not following the burn vector.
            if ((ship.facing.direction - toTarget.facing).length <= maxAngleToTarget) {
                return true
            }
        }

        // Burn as long as the ship is following the burn vector.
        if (burnVector.isNonZero) {
            return (ship.facing.direction - burnVector.facing).length <= maxAngleToTarget
        }

        // Keep burning if there's no target, as long as the battle isn't over.
        // Burning after the last enemy ship is defeated looks unnatural.
        return Global.getCombatEngine().ships.any { it.isHostile(ship) && !it.isFighter }
    }

    private fun angleToDestination(): Direction {
        return if (burnVector.isZero) 180f.direction
        else (ship.facing.direction - burnVector.facing)
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

                // Heavy obstacles. Hitting heavy hulls will deflect the ships path.
                it.mass >= ship.mass * ignoreMassFraction -> true

                else -> false
            }
        }
    }

    private fun isRouteClear(): Boolean {
        val dist = burnVector.length.coerceAtMost(maxBurnDist)
        val position = ship.location + burnVector.resized(dist / 2)
        val obstacles = findObstacles(position, dist / 2)

        val maxBurnDuration = system.chargeActiveDur + system.chargeUpDur + system.chargeDownDur
        val timeToTarget = maxBurnDuration * (dist / maxBurnDist)
        val effectiveSpeed = maxBurnDist / maxBurnDuration

        return timeToTarget < timeToCollision(obstacles, burnVector.resized(effectiveSpeed))
    }

    private fun isCollisionImminent(): Boolean {
        val radius = maxBurnDist / 2
        val position = ship.location + ship.facing.direction.unitVector * radius
        val obstacles = findObstacles(position, radius)

        return timeToCollision(obstacles, ship.velocity) <= stopBeforeCollision
    }

    private fun timeToCollision(obstacles: Sequence<ShipAPI>, shipVelocity: Vector2f): Float {
        return obstacles.mapNotNull { obstacle ->
            val p = obstacle.location - ship.location
            val v = obstacle.velocity - shipVelocity
            val r = ship.totalCollisionRadius + obstacle.totalCollisionRadius

            // Calculate time to collision.
            if (p.lengthSquared <= r * r) 0f
            else solve(p, v, r)?.smallerNonNegative
        }.minOrNull() ?: Float.MAX_VALUE
    }
}
