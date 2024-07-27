package com.genir.aitweaks.core.features.shipai.systems

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.genir.aitweaks.core.debug.debugPrint
import com.genir.aitweaks.core.debug.drawLine
import com.genir.aitweaks.core.features.shipai.AI
import com.genir.aitweaks.core.features.shipai.Preset
import com.genir.aitweaks.core.features.shipai.command
import com.genir.aitweaks.core.features.shipai.strafeAcceleration
import com.genir.aitweaks.core.utils.extensions.addLength
import com.genir.aitweaks.core.utils.extensions.facing
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
    private var trigger: Set<ShipCommand>? = null

    private var attackRange: Float = 0f

    override fun advance(dt: Float) {
//        burstVectors().forEach {
//            drawLine(ship.location, ship.location + unitVector(it.key) * 200f, Color.GREEN)
//            debugPrint["x"] = it.key
//        }

//        if (trigger) {
////            ship.command(USE_SYSTEM)
//            trigger = false
//        }

        debugPrint.clear()

        attackRange = ai.broadside.minRange * Preset.BurnDrive.approachToMinRangeFraction

        updateHeadingPoint()
        updateShouldBurn()
        updatedSystemTrigger()
        triggerSystem()

//        drawLine(ship.location, if (headingPoint.isZeroVector()) ship.location else headingPoint, Color.BLUE)
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
        if (!shouldBurn) return null

        val toTarget = (headingPoint - ship.location).facing - ship.facing

        val vectors = burstVectors()
        val bestVector = vectors.minWithOrNull(compareBy { abs(MathUtils.getShortestRotation(it.key, toTarget)) })!!

        vectors.forEach {
            debugPrint[it.value] = "${abs(MathUtils.getShortestRotation(it.key, toTarget))} ${it.value}"
        }

        return Pair(unitVector(ship.facing - bestVector.key) * 1000f, Vector2f())
    }

    private fun updateHeadingPoint() {
        target = ai.attackTarget
//        target = when {
//            ai.maneuverTarget == null -> null
//
//            ai.attackTarget == null -> null
//
//            ai.maneuverTarget != ai.attackTarget -> null
//
//            else -> ai.maneuverTarget
//        }

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
            // System is already scheduled to trigger. // TODO expire
            trigger != null -> false

            system.state != ShipSystemAPI.SystemState.IDLE -> false

            !ship.canUseSystemThisFrame() -> false

            target == null -> false

            headingPoint.isZeroVector() -> false

            // Already close to target.
            (target!!.location - ship.location).length() < attackRange -> false

            else -> true
        }
    }

    private fun updatedSystemTrigger() {
        if (!shouldBurn) return

        val toTarget = (headingPoint - ship.location).facing - ship.facing

        val vectors = burstVectors().toMutableMap()
        if (!ship.velocity.isZeroVector()) {
            vectors += Pair(ship.velocity.facing - ship.facing, setOf())
        }
        val bestVector = vectors.minWithOrNull(compareBy { abs(MathUtils.getShortestRotation(it.key, toTarget)) })!!

//        vectors.forEach {
//            debugPrint[it.value] = "${abs(MathUtils.getShortestRotation(it.key, toTarget))} ${it.value}"
//        }

        if (abs(MathUtils.getShortestRotation(bestVector.key, toTarget)) > 2f) return

        trigger = bestVector.value

//        val commands: Set<ShipCommand> = bestVector.value
//        val blockedCommands = setOf(ACCELERATE, DECELERATE, STRAFE_RIGHT, STRAFE_LEFT) - commands

//        commands.forEach { ship.command(it) }
//        blockedCommands.forEach { ship.blockCommandForOneFrame(it) }
//
//        trigger = true
//        ship.command(USE_SYSTEM)
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

    private fun triggerSystem() {
        if (trigger == null) return

        val commands = trigger!!
        val blockedCommands = setOf(ACCELERATE, ACCELERATE_BACKWARDS, STRAFE_RIGHT, STRAFE_LEFT) - commands

        commands.forEach { ship.command(it) }
        blockedCommands.forEach { ship.blockCommandForOneFrame(it) }

        val shouldUse = when {
            ship.engineController.isAccelerating != commands.contains(ACCELERATE) -> false
            ship.engineController.isAcceleratingBackwards != commands.contains(ACCELERATE_BACKWARDS) -> false
            ship.engineController.isStrafingLeft != commands.contains(STRAFE_LEFT) -> false
            ship.engineController.isStrafingRight != commands.contains(STRAFE_RIGHT) -> false

            else -> true
        }

        if (shouldUse) {
            trigger = null
            ship.command(USE_SYSTEM)
        }
    }
}