package com.genir.aitweaks.core.features.shipai.ai

import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.debug.drawCircle
import com.genir.aitweaks.core.debug.drawLine
import com.genir.aitweaks.core.features.shipai.ai.Preset.Companion.collisionBuffer
import com.genir.aitweaks.core.utils.div
import com.genir.aitweaks.core.utils.extensions.addLength
import com.genir.aitweaks.core.utils.extensions.isModule
import com.genir.aitweaks.core.utils.shipSequence
import com.genir.aitweaks.core.utils.timeToOrigin
import com.genir.aitweaks.core.utils.times
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.abs

class BurnDrive(val ship: ShipAPI, val ai: Maneuver) {
    var headingPoint: Vector2f? = null
    var shouldBurn = false
    private var vectorToTarget: Vector2f? = null

    fun advance(dt: Float) {
        updateHeadingPoint()
        updateShouldBurn()

        headingPoint?.let {
            drawLine(ship.location, it, Color.GREEN)
        }
    }

    private fun updateHeadingPoint() {
        headingPoint = when {
            ship.system.isOn -> headingPoint

            ai.moveOrderLocation != null -> ai.moveOrderLocation

            // Charge straight at the maneuver target, disregard fleet coordination.
            ai.maneuverTarget != null -> {
                val vectorToTarget = ai.maneuverTarget.location - ship.location
                vectorToTarget.addLength(-ai.minRange) + ship.location
            }

            else -> null
        }

        vectorToTarget = headingPoint?.let { it - ship.location }
    }

    private fun updateShouldBurn() {
        shouldBurn = when {
            vectorToTarget == null -> false

            !routeIsClear(vectorToTarget!!) -> false

            ai.isBackingOff -> false

            ship.system.isOn -> false

            !ship.canUseSystemThisFrame() -> false

            vectorToTarget!!.length() < 800f -> false

//            !routeIsClear(vectorToTarget!!) -> false
//
            else -> true
        }
    }

    fun go(): Boolean {
        return when {
            !shouldBurn -> false

//            ship.system.isOn -> false

            abs(MathUtils.getShortestRotation(ship.facing, vectorToTarget!!.getFacing())) < 0.1f -> true

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

        if (ship.name.contains("Averna")) {
            obstacles.forEach {
                drawLine(ship.location, it.location, Color.RED)
            }

            drawCircle(p, r)
        }

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
