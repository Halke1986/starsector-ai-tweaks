package com.genir.aitweaks.core.features.shipai.systems

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.genir.aitweaks.core.features.shipai.AI
import com.genir.aitweaks.core.features.shipai.command
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.*
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

class SrBurstBoost(private val ai: AI) : SystemAI {
    private val ship: ShipAPI = ai.ship
    private val system: ShipSystemAPI = ship.system

    private var target: ShipAPI? = null
    private var headingPoint: Vector2f = Vector2f()
    private var shouldBurnAtTarget: Boolean = false
    private var trigger: Boolean = false

    private var attackRange: Float = 0f
    private var burstVectors: List<BurstVector> = calculateBurstVectors()

    @Suppress("ConstPropertyName")
    companion object Preset {
        const val maxBurnAngle = 2f
        const val approachToMinRangeFraction = 0.75f
        const val maxRammingDistance = 400f
        const val minChaseDistance = 650f
        const val burstSpeed = 600f
    }

    data class BurstVector(val vector: Vector2f, val facing: Float, val offset: Float, val commands: Set<ShipCommand>)

    override fun advance(dt: Float) {
        burstVectors = calculateBurstVectors()

        if (trigger) {
            ship.command(USE_SYSTEM)
            trigger = false
        }

//        burstVectors.forEach {
//            drawLine(ship.location + it.vector.resized(it.offset), ship.location + it.vector.resized(it.offset + burstSpeed), BLUE)
//        }
//
//
//        drawBounds(ship)
//
//        ai.threats.forEach {
//            rammingDistance(it)
//        }

        attackRange = ai.broadside.minRange * approachToMinRangeFraction

        updateHeadingPoint()
        updateShouldBurn()
        updateTrigger()
    }

    override fun holdManeuverTarget(): Boolean {
        return ship.system.isOn
    }

    override fun overrideHeading(): Vector2f? {
        return null
//        return if (shouldBurn) headingPoint
//        else null
    }

    override fun overrideFacing(): Float? {
        when {
            !shouldBurnAtTarget -> return null

            // Don't interrupt hardpoint bursts.
            ai.stats.significantWeapons.any { it.slot.isHardpoint && it.isInFiringSequence } -> return null
        }

        val bestVector = bestBurstVector(headingPoint, 360f)!!
        val toTarget = (headingPoint - ship.location).facing
        val rotation = MathUtils.getShortestRotation(bestVector.facing, toTarget)
        return ship.facing + rotation
    }

    private fun updateHeadingPoint() {
        target = ai.attackTarget

        // Head straight to the target.
        headingPoint = ai.attackTarget?.location ?: Vector2f()
    }

    private fun canBurn(): Boolean {
        return when {
            // System is already scheduled to trigger.
            trigger -> false

            system.state != ShipSystemAPI.SystemState.IDLE -> false

            !ship.canUseSystemThisFrame() -> false

            else -> true
        }
    }

    private fun updateShouldBurn() {
        shouldBurnAtTarget = when {
            !canBurn() -> false

            target == null -> false

            headingPoint.isZeroVector() -> false

            // Save charges for better opportunities.
            system.ammo < system.maxAmmo / 2f -> false

            else -> true
        }
    }

    private fun updateTrigger() {
        when {
            !canBurn() -> return

            // Don't interrupt hardpoint warmup. It's possible to burn when weapon is already in burst.
            ai.stats.significantWeapons.any { it.slot.isHardpoint && it.isInWarmup } -> return
        }

        // Use system the next frame, when issued commands are in effect.
        val burst: BurstVector = burnAtTarget() ?: burnAtOpportunity() ?: return

        // Issue commands that will trigger burn in the right direction.
        val commands: Set<ShipCommand> = burst.commands
        commands.forEach { ship.command(it) }

        // Block commands that could skew the burn direction.
        val blockedCommands = setOf(ACCELERATE, ACCELERATE_BACKWARDS, STRAFE_RIGHT, STRAFE_LEFT, DECELERATE) - commands
        blockedCommands.forEach { ship.blockCommandForOneFrame(it) }

        // Use system the next frame, when issued commands are in effect.
        trigger = true
    }

    private fun burnAtTarget(): BurstVector? {
        return if (shouldBurnAtTarget) bestBurstVector(headingPoint, maxBurnAngle)
        else null
    }

    private fun burnAtOpportunity(): BurstVector? {
        val vectors: List<Pair<BurstVector, Float>> = ai.threats.mapNotNull { rammingVector(it) }
        val bestVector = vectors.minWithOrNull(compareBy { it.second }) ?: return null

        return if (bestVector.second <= maxRammingDistance) bestVector.first
        else null
    }

    private fun calculateBurstVectors(): List<BurstVector> {
        val a = Vector2f(ship.acceleration, 0f)
        val d = Vector2f(-ship.deceleration, 0f)
        val l = Vector2f(0f, burstStrafeAcceleration())
        val r = Vector2f(0f, -burstStrafeAcceleration())

        val rawVectors = listOf(
            Pair(a, setOf(ACCELERATE)),
            Pair(d, setOf(ACCELERATE_BACKWARDS)),
            Pair(l, setOf(STRAFE_LEFT)),
            Pair(r, setOf(STRAFE_RIGHT)),

            Pair((a + l), setOf(ACCELERATE, STRAFE_LEFT)),
            Pair((a + r), setOf(ACCELERATE, STRAFE_RIGHT)),
            Pair((d + l), setOf(ACCELERATE_BACKWARDS, STRAFE_LEFT)),
            Pair((d + r), setOf(ACCELERATE_BACKWARDS, STRAFE_RIGHT)),
        )

        val rot = Rotation(ship.facing)
        return rawVectors.map {
            val burstVector = it.first.rotated(rot).resized(burstSpeed)
            val offsetRelative = boundsCollision(burstVector, -burstVector, ship)!!
            val offset = burstSpeed * (1f - offsetRelative)

            BurstVector(burstVector, it.first.facing + ship.facing, offset, it.second)
        }
    }

    /** Find burn vector best aligned with direction to target. */
    private fun bestBurstVector(target: Vector2f, maxAngle: Float): BurstVector? {
        val toTarget = (target - ship.location).facing
        val bestVector = burstVectors.minWithOrNull(compareBy { abs(MathUtils.getShortestRotation(it.facing, toTarget)) })!!

        // Best burn vector is not aligned well with direction to target.
        return if (abs(MathUtils.getShortestRotation(bestVector.facing, toTarget)) <= maxAngle) bestVector
        else null
    }

    private fun rammingVector(target: ShipAPI): Pair<BurstVector, Float>? {
        val bestVector = bestBurstVector(target.location, 360f) ?: return null
        val collision = shieldCollision(bestVector, target) ?: boundsCollision(bestVector, target) ?: return null

        return Pair(bestVector, collision - bestVector.offset)
    }

    /** Distance along burn vector at which collision with target shield occurs. */
    private fun shieldCollision(burst: BurstVector, target: ShipAPI): Float? {
        if (target.shield?.isOn != true) return null
        val shield = target.shield

        val position = ship.location - shield.location
        val velocity = burst.vector

        val time = solve(Pair(position, velocity), shield.radius) ?: return null
        val hitPoint = position + velocity * time

        return if (vectorInArc(hitPoint, Arc(shield.activeArc, shield.facing))) (velocity * time).length else null
    }

    /** Distance along burn vector at which collision with target bounds occurs. */
    private fun boundsCollision(burst: BurstVector, target: ShipAPI): Float? {
        val position = ship.location - target.location

        val distanceRelative = boundsCollision(position, burst.vector, target) ?: return null
        return burst.vector.length * distanceRelative
    }

    /** Burst boost strafe acceleration differs
     * from vanilla strafe acceleration. */
    private fun burstStrafeAcceleration(): Float {
        return ship.acceleration * when (ship.hullSize) {
            ShipAPI.HullSize.FIGHTER -> 1f
            ShipAPI.HullSize.FRIGATE -> 1.0f
            ShipAPI.HullSize.DESTROYER -> 0.75f
            ShipAPI.HullSize.CRUISER -> 0.5f
            ShipAPI.HullSize.CAPITAL_SHIP -> 0.35f
            else -> 1.0f
        }
    }
}
