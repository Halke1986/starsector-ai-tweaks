package com.genir.aitweaks.core.features.shipai.ai

import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.ACTIVE
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.IDLE
import com.genir.aitweaks.core.debug.drawLine
import com.genir.aitweaks.core.features.shipai.CustomAIManager
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.addLength
import com.genir.aitweaks.core.utils.extensions.resized
import com.genir.aitweaks.core.utils.extensions.rootModule
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.abs
import kotlin.math.min

/** Burn Drive AI. It replaces the vanilla implementation in ships with custom AI. */
class BurnDrive(val ship: ShipAPI, val ai: Maneuver) {
    var headingPoint: Vector2f? = Vector2f()
    var shouldBurn = false

    private val system: ShipSystemAPI = ship.system
    private val burnDriveFlatBonus: Float = 200f

    private var vectorToTarget: Vector2f = Vector2f()
    private var distToTarget: Float = 0f
    private var angleToTarget: Float = 0f

    // Stats
    private var maxSpeed: Float = Float.MAX_VALUE
    private var maxBurnDist: Float = 0f

    init {
        // Remove vanilla burn drive AI, so that it doesn't interfere.
        val c = CustomAIManager().getCustomAIClass()!!
        val f = c.getDeclaredField("systemAI")
        f.setAccessible(true);
        f.set(ship.shipAI, null);
    }

    fun advance(dt: Float) {
        updateMaxBurnDist()
        updateHeadingPoint()
        updateShouldBurn()
        triggerSystem(dt)

        if (headingPoint == null) {
            drawLine(ship.location, ship.location + unitVector(ship.facing) * 200f, Color.RED)
        } else {
            drawLine(ship.location, headingPoint!!, Color.GREEN)
        }
    }

    private fun updateMaxBurnDist() {
        // Try to get the unmodified max speed, without burn drive bonus.
        maxSpeed = min(maxSpeed, ship.engineController.maxSpeedWithoutBoost)

        val effectiveBurnDuration = system.chargeActiveDur + (system.chargeUpDur + system.chargeDownDur) / 2f
        maxBurnDist = (maxSpeed + burnDriveFlatBonus) * effectiveBurnDuration
    }

    private fun updateHeadingPoint() {
        val newHeadingPoint = when {
            ai.moveOrderLocation != null -> ai.moveOrderLocation

            // Charge straight at the maneuver target, disregard fleet coordination.
            ai.maneuverTarget != null -> {
                val vectorToTarget = ai.maneuverTarget.location - ship.location
                vectorToTarget.addLength(-ai.minRange * Preset.BurnDrive.approachToMinRangeFraction) + ship.location
            }

            else -> null
        }

        if (newHeadingPoint != null) {
            vectorToTarget = newHeadingPoint - ship.location
            headingPoint = vectorToTarget + ship.location
            distToTarget = vectorToTarget.length()
            angleToTarget = abs(MathUtils.getShortestRotation(vectorToTarget.getFacing(), ship.facing))
        } else {
            headingPoint = null
            vectorToTarget = Vector2f()
            distToTarget = 0f
            angleToTarget = 0f
        }
    }

    private fun updateShouldBurn() {
        shouldBurn = when {
            system.state != IDLE -> false

            !ship.canUseSystemThisFrame() -> false

            headingPoint == null -> false

            ai.isBackingOff -> false

            angleToTarget > Preset.BurnDrive.maxAngleToTarget -> false

            distToTarget < maxBurnDist / 2f -> false

            !isRouteClear() -> false

            else -> true
        }
    }

    private fun triggerSystem(dt: Float) {
        val shouldTrigger = when {
            // Launch.
            shouldBurn && angleToTarget < 0.1f -> true

            // Not burning, no need to abort.
            system.state != ACTIVE -> false

            // Stop to not overshoot target.
            vMax(dt, distToTarget, 600f) < ship.velocity.length() -> true

            // Veered off course, stop.
            angleToTarget > Preset.BurnDrive.maxAngleToTarget -> true

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
        val dist = distToTarget.coerceAtMost(maxBurnDist)
        val position = ship.location + vectorToTarget.resized(dist) / 2f
        val obstacles = findObstacles(position, dist / 2f)

        val maxBurnDuration = system.chargeActiveDur + system.chargeUpDur + system.chargeDownDur
        val timeToTarget = maxBurnDuration * (dist / maxBurnDist)
        val effectiveSpeed = maxBurnDist / maxBurnDuration

        return timeToTarget < timeToCollision(obstacles, vectorToTarget.resized(effectiveSpeed))
    }

    private fun isCollisionImminent(): Boolean {
        val radius = maxBurnDist / 2f
        val position = ship.location + unitVector(ship.facing) * radius
        val obstacles = findObstacles(position, radius)

        return timeToCollision(obstacles, ship.velocity) <= Preset.BurnDrive.stopBeforeCollision
    }

    private fun timeToCollision(obstacles: Sequence<ShipAPI>, shipVelocity: Vector2f): Float {
        return obstacles.mapNotNull { obstacle ->
            val p = obstacle.location - ship.location
            val v = obstacle.velocity - shipVelocity
            val r = ship.totalCollisionRadius + obstacle.totalCollisionRadius + Preset.collisionBuffer

            // Calculate time to collision.
            if (p.lengthSquared() <= r * r) 0f
            else solve(Pair(p, v), r)
        }.minOrNull() ?: Float.MAX_VALUE
    }
}
