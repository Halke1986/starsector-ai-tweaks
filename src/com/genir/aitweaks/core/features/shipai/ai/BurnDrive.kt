package com.genir.aitweaks.core.features.shipai.ai

import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.debug.drawLine
import com.genir.aitweaks.core.features.shipai.ai.Preset.Companion.collisionBuffer
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.addLength
import com.genir.aitweaks.core.utils.extensions.isModule
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.clampLength
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.abs
import kotlin.math.min

class BurnDrive(val ship: ShipAPI, val ai: Maneuver) {
    var headingPoint: Vector2f? = ai.stash.burnDriveHeading

    //    private var vectorToTarget: Vector2f = Vector2f()
    private var angleToTarget: Float = 0f

    var shouldBurn = false
    private var maxSpeed = Float.MAX_VALUE

    fun advance(dt: Float) {
        // Try to get the unmodified max speed, without burn drive bonus.
        maxSpeed = min(maxSpeed, ship.engineController.maxSpeedWithoutBoost)

        updateHeadingPoint()
        updateShouldBurn()

        if (headingPoint == null) {
            drawLine(ship.location, ship.location + unitVector(ship.facing) * 200f, Color.RED)
        } else {
            drawLine(ship.location, headingPoint!!, Color.GREEN)
        }
    }

    private fun updateHeadingPoint() {
        val headingPoint = when {
            ship.system.isOn -> headingPoint

            ai.moveOrderLocation != null -> ai.moveOrderLocation

            // Charge straight at the maneuver target, disregard fleet coordination.
            ai.maneuverTarget != null -> {
                val vectorToTarget = ai.maneuverTarget.location - ship.location
                vectorToTarget.addLength(-ai.minRange) + ship.location
            }

            else -> null
        }

        if (headingPoint != null) {
            val burnDriveFlatBonus = 200f
            val duration = ship.system.chargeActiveDur + (ship.system.chargeUpDur + ship.system.chargeDownDur) / 2f
            val travelDist = (maxSpeed + burnDriveFlatBonus) * duration
            val vectorToTarget = (headingPoint - ship.location).clampLength(travelDist)

            this.headingPoint = vectorToTarget + ship.location
            this.angleToTarget = abs(MathUtils.getShortestRotation(vectorToTarget.getFacing(), ship.facing))

//            debugPlugin[ship] = vectorToTarget.length()
        } else {
            this.headingPoint = null
//            this.vectorToTarget = Vector2f()
            this.angleToTarget = 0f
        }

        // Heading point target is stored in stash, so it carries over between BurnDrive instances.
        ai.stash.burnDriveHeading = this.headingPoint
    }

    private fun updateShouldBurn() {
        val vectorToTarget = headingPoint?.let { it - ship.location } ?: Vector2f()
        shouldBurn = when {
            headingPoint == null -> false

            ai.isBackingOff -> false

            ship.system.isOn -> false

            !ship.canUseSystemThisFrame() -> false

            angleToTarget > 15f -> false

            vectorToTarget.length() < 800f -> false

            !routeIsClear(vectorToTarget) -> false

            else -> true
        }
    }

    fun go(): Boolean {
        return when {
            !shouldBurn -> false

            angleToTarget < 0.1f -> true

            else -> false
        }
    }

    private fun routeIsClear(vectorToTarget: Vector2f): Boolean {
        val p = ship.location + vectorToTarget / 2f
        val r = vectorToTarget.length()

        val obstacles = shipSequence(p, r).filter {
            when {
                it == ship -> false
                it.collisionClass != CollisionClass.SHIP -> false

                // Ram enemy frigates.
                it.owner != ship.owner && it.isFrigate && !it.isModule -> false
                else -> true
            }
        }.toList()

//        if (ship.name.contains("Averna")) {
//            obstacles.forEach {
//                drawLine(ship.location, it.location, Color.RED)
//            }
//
//            drawCircle(p, r)
//        }

        return obstacles.none { obstacle ->
            val t = timeToOrigin(ship.location - obstacle.location, vectorToTarget)

            if (t < 0f || t > 1f) false
            else {
                val closestPoint = ship.location + vectorToTarget * t
                val dist = (closestPoint - obstacle.location).length()

                dist < ship.totalCollisionRadius + obstacle.totalCollisionRadius + collisionBuffer
            }
        }
    }
}
