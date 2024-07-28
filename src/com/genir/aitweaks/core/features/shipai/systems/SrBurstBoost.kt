package com.genir.aitweaks.core.features.shipai.systems

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.genir.aitweaks.core.debug.drawLine
import com.genir.aitweaks.core.features.shipai.AI
import com.genir.aitweaks.core.features.shipai.Preset
import com.genir.aitweaks.core.features.shipai.command
import com.genir.aitweaks.core.features.shipai.strafeAcceleration
import com.genir.aitweaks.core.utils.extensions.facing
import com.genir.aitweaks.core.utils.extensions.isInFiringSequence
import com.genir.aitweaks.core.utils.extensions.isInWarmup
import com.genir.aitweaks.core.utils.times
import com.genir.aitweaks.core.utils.unitVector
import org.lazywizard.lazylib.MathUtils
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
    private var trigger: Boolean = true

    private var attackRange: Float = 0f

    override fun advance(dt: Float) {
        if (trigger) {
            ship.command(USE_SYSTEM)
            trigger = false
        }

        attackRange = ai.broadside.minRange * Preset.BurnDrive.approachToMinRangeFraction

        updateHeadingPoint()
        updateShouldBurn()
        updatedSystemTrigger()

        drawLine(ship.location, target?.location ?: ship.location, Color.BLUE)
    }

    override fun holdManeuverTarget(): Boolean {
        return ship.system.isOn
    }

    override fun overrideHeading(): Pair<Vector2f, Vector2f>? {
        return when {
            // Prevent engine controller from overriding system commands.
//            trigger != null -> Pair(ship.location, Vector2f())

            shouldBurn -> Pair(headingPoint, Vector2f())

            else -> null
        }
    }

    override fun overrideFacing(): Pair<Vector2f, Vector2f>? {
        when {
            !shouldBurn -> return null

            // Don't interrupt hardpoint bursts.
            ai.stats.significantWeapons.any { it.slot.isHardpoint && it.isInFiringSequence } -> return null
        }

        val toTarget = (headingPoint - ship.location).facing - ship.facing

        val vectors = burstVectors()
        val bestVector = vectors.minWithOrNull(compareBy { abs(MathUtils.getShortestRotation(it.key, toTarget)) })!!

        val rotation = MathUtils.getShortestRotation(bestVector.key, toTarget)
        return Pair(ship.location + unitVector(ship.facing + rotation) * 1000f, Vector2f())
    }

    private fun updateHeadingPoint() {
        target = ai.attackTarget

        // Head straight to the target.
        headingPoint = ai.attackTarget?.location ?: Vector2f()
    }

    private fun updateShouldBurn() {
        shouldBurn = when {
            // System is already scheduled to trigger.
            trigger -> false

            system.state != ShipSystemAPI.SystemState.IDLE -> false

            !ship.canUseSystemThisFrame() -> false

            target == null -> false

            headingPoint.isZeroVector() -> false

            else -> true
        }
    }

    private fun updatedSystemTrigger() {
        when {
            !shouldBurn -> return

            // Don't interrupt hardpoint warmup. It's possible to burn when weapon is already in burst.
            ai.stats.significantWeapons.any { it.slot.isHardpoint && it.isInWarmup } -> return
        }

        // Find all possible burn vectors.
        val vectors = burstVectors().toMutableMap()
        if (!ship.velocity.isZeroVector()) vectors += Pair(ship.velocity.facing - ship.facing, setOf())

        // Find burn vector best aligned with direction to target.
        val toTarget = (headingPoint - ship.location).facing - ship.facing
        val bestVector = vectors.minWithOrNull(compareBy { abs(MathUtils.getShortestRotation(it.key, toTarget)) })!!

        // Best burn vector is not aligned well with direction to target.
        if (abs(MathUtils.getShortestRotation(bestVector.key, toTarget)) > 2f) return

        // Issue commands that will trigger burn in the right direction.
        val commands: Set<ShipCommand> = bestVector.value
        commands.forEach { ship.command(it) }

        // Block commands that could skew the burn direction.
        val blockedCommands = setOf(ACCELERATE, ACCELERATE_BACKWARDS, STRAFE_RIGHT, STRAFE_LEFT, DECELERATE) - commands
        blockedCommands.forEach { ship.blockCommandForOneFrame(it) }

        // Use system the next frame, when issued commands are in effect.
        trigger = true
    }

    private fun burstVectors(): Map<Float, Set<ShipCommand>> {
        val a = Vector2f(ship.acceleration, 0f)
        val d = Vector2f(-ship.deceleration, 0f)
        val l = Vector2f(0f, ship.strafeAcceleration)
        val r = Vector2f(0f, -ship.strafeAcceleration)

        return mapOf(
            0f to setOf(ACCELERATE),
            180f to setOf(ACCELERATE_BACKWARDS),
            90f to setOf(STRAFE_LEFT),
            270f to setOf(STRAFE_RIGHT),

            (a + l).facing to setOf(ACCELERATE, STRAFE_LEFT),
            (a + r).facing to setOf(ACCELERATE, STRAFE_RIGHT),
            (d + l).facing to setOf(ACCELERATE_BACKWARDS, STRAFE_LEFT),
            (d + r).facing to setOf(ACCELERATE_BACKWARDS, STRAFE_RIGHT),
        )
    }
}
