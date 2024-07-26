package com.genir.aitweaks.core.features.shipai.systems

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.genir.aitweaks.core.debug.drawLine
import com.genir.aitweaks.core.features.shipai.AI
import com.genir.aitweaks.core.features.shipai.Preset
import com.genir.aitweaks.core.features.shipai.SystemAI
import com.genir.aitweaks.core.features.shipai.command
import com.genir.aitweaks.core.utils.extensions.addLength
import com.genir.aitweaks.core.utils.getShortestRotation
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.abs

class SrBurstBoost(private val ai: AI) : SystemAI {
    private val ship: ShipAPI = ai.ship
    private val system: ShipSystemAPI = ship.system

    private var target: ShipAPI? = null
    private var headingPoint: Vector2f = Vector2f()
    private var shouldBurn: Boolean = false

    private var attackRange: Float = 0f

    override fun advance(dt: Float) {
        attackRange = ai.stats.minRange * Preset.BurnDrive.approachToMinRangeFraction

        updateHeadingPoint()
        updateShouldBurn()
        triggerSystem()

        drawLine(ship.location, if (headingPoint.isZeroVector()) ship.location else headingPoint, Color.BLUE)
    }

    override fun holdManeuverTarget(): Boolean {
        return ship.system.isOn
    }

    override fun overrideHeading(): Pair<Vector2f, Vector2f>? {
        return if (shouldBurn) Pair(headingPoint, Vector2f())
        else null
    }

    override fun overrideFacing(): Pair<Vector2f, Vector2f>? {
        return null
    }

    private fun updateHeadingPoint() {
        target = when {
            ai.maneuverTarget == null -> null

            ai.attackTarget == null -> null

            ai.maneuverTarget != ai.attackTarget -> null

            else -> ai.maneuverTarget
        }

        if (target == null) {
            headingPoint = Vector2f()
        } else {
            val vectorToTarget = target!!.location - ship.location
            val approachVector = vectorToTarget.addLength(-attackRange)

            headingPoint = approachVector + ship.location
        }
    }

    private fun updateShouldBurn() {
        shouldBurn = when {
            system.state != ShipSystemAPI.SystemState.IDLE -> false

            !ship.canUseSystemThisFrame() -> false

            target == null -> false

            headingPoint.isZeroVector() -> false

            // Already close to target.
            (target!!.location - ship.location).length() < attackRange -> false

            else -> true
        }
    }

    private fun triggerSystem() {
        val shouldTrigger = when {
            !shouldBurn -> false

            abs(getShortestRotation(ship.velocity, headingPoint - ship.location)) > 0.1f -> false

            else -> true
        }

        if (shouldTrigger) {
            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE)
            ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT)
            ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT)
            ship.blockCommandForOneFrame(ShipCommand.DECELERATE)

            ship.command(ShipCommand.USE_SYSTEM)
        }
    }
}