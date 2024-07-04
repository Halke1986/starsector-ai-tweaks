package com.genir.aitweaks.core.features.shipai.ai

import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.ACTIVE
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.IDLE
import com.genir.aitweaks.core.debug.drawLine
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

class BurnDrive(val ship: ShipAPI, val ai: Maneuver) {
    var headingPoint: Vector2f? = Vector2f()
    var shouldBurn = false

    private var vectorToTarget: Vector2f = Vector2f()
    private var distToTarget: Float = 0f
    private var angleToTarget: Float = 0f

    private var maxSpeed = Float.MAX_VALUE
    private var maxBurnDist: Float = 0f

    fun advance() {
        updateMaxBurnDist()
        updateHeadingPoint()
        updateShouldBurn()

        if (headingPoint == null) {
            drawLine(ship.location, ship.location + unitVector(ship.facing) * 200f, Color.RED)
        } else {
            drawLine(ship.location, headingPoint!!, Color.GREEN)
        }
    }

    private fun updateMaxBurnDist() {
        // Try to get the unmodified max speed, without burn drive bonus.
        maxSpeed = min(maxSpeed, ship.engineController.maxSpeedWithoutBoost)

        val burnDriveFlatBonus = 200f
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

    fun shouldTrigger(dt: Float): Boolean {
//        debugPlugin["pos"] = headingPoint

        return when {
            // Launch.
            shouldBurn && angleToTarget < 0.1f -> {
//                debugPlugin[ship] = "${ship.name} ${ship.system.state} burn"
                true
            }

            ship.system.state != ACTIVE -> false

            // Stop to not overshoot target.
            vMax(dt, distToTarget, 600f) < ship.velocity.length() -> {
//                debugPlugin[ship] = "${ship.name} ${ship.system.state} dist"
                true
            }

            // Veered off course, stop.
            angleToTarget > Preset.BurnDrive.maxAngleToTarget -> {
//                debugPlugin[ship] = "${ship.name} ${ship.system.state} angle $angleToTarget"
                true
            }

            else -> false
        }
    }

    private fun isRouteClear(): Boolean {
        val p = ship.location + vectorToTarget / 2f
        val r = distToTarget.coerceAtMost(maxBurnDist)

        val obstacles = shipSequence(p, r).filter {
            when {
                it == ship -> false
                it.collisionClass != CollisionClass.SHIP -> false

                // Ram enemy frigates.
                it.owner != ship.owner && it.isFrigate && !it.isModule -> false
                else -> true
            }
        }.toList()

        return obstacles.none { obstacle ->
            val t = timeToOrigin(ship.location - obstacle.location, vectorToTarget)

            if (t < 0f || t > 1f) false
            else {
                val closestPoint = ship.location + vectorToTarget * t
                val dist = (closestPoint - obstacle.location).length()

                dist < ship.totalCollisionRadius + obstacle.totalCollisionRadius + Preset.collisionBuffer
            }
        }
    }
}
