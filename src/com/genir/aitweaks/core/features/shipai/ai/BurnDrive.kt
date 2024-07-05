package com.genir.aitweaks.core.features.shipai.ai

import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.ACTIVE
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.IDLE
import com.genir.aitweaks.core.debug.drawLine
import com.genir.aitweaks.core.features.shipai.CustomAIManager
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.addLength
import com.genir.aitweaks.core.utils.extensions.isModule
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

    private var vectorToTarget: Vector2f = Vector2f()
    private var distToTarget: Float = 0f
    private var angleToTarget: Float = 0f

    private val burnDriveFlatBonus = 200f
    private var maxSpeed = Float.MAX_VALUE
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

        val duration = ship.system.chargeActiveDur + (ship.system.chargeUpDur + ship.system.chargeDownDur) / 2f
        maxBurnDist = (maxSpeed + burnDriveFlatBonus) * duration
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
            ship.system.state != IDLE -> false

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

            ship.system.state != ACTIVE -> false

            // Stop to not overshoot target.
            vMax(dt, distToTarget, 600f) < ship.velocity.length() -> true

            // Veered off course, stop.
            angleToTarget > Preset.BurnDrive.maxAngleToTarget -> true

            // Collision is imminent.
            timeToCollision() < Preset.BurnDrive.stopBeforeCollision -> true

            else -> false
        }

        if (shouldTrigger) ship.command(ShipCommand.USE_SYSTEM)
    }

    private fun findObstacles(center: Vector2f, radius: Float): Sequence<ShipAPI> {
        return shipSequence(center, radius).filter {
            when {
                it == ship -> false
                it.collisionClass != CollisionClass.SHIP -> false

                // Ram enemy frigates.
                it.owner != ship.owner && it.isFrigate && !it.isModule -> false
                else -> true
            }
        }
    }

    private fun isRouteClear(): Boolean {
        val p = ship.location + vectorToTarget / 2f
        val r = distToTarget.coerceAtMost(maxBurnDist)

        return findObstacles(p, r).none { obstacle ->
            val t = timeToOrigin(ship.location - obstacle.location, vectorToTarget)

            if (t < 0f || t > 1f) false
            else {
                val closestPoint = ship.location + vectorToTarget * t
                val dist = (closestPoint - obstacle.location).length()

                dist < ship.totalCollisionRadius + obstacle.totalCollisionRadius + Preset.collisionBuffer
            }
        }
    }

    private fun timeToCollision(): Float {
        val position = ship.location + unitVector(ship.facing) * maxBurnDist / 2f
        val obstacles = findObstacles(position, maxBurnDist)

        return obstacles.mapNotNull { obstacle ->
            val p = obstacle.location - ship.location
            val v = obstacle.velocity - ship.velocity
            val r = ship.totalCollisionRadius + obstacle.totalCollisionRadius + Preset.collisionBuffer

            // Calculate time to collision.
            solve(Pair(p, v), r, 0f, 0f)
        }.minOrNull() ?: Float.MAX_VALUE
    }
}
