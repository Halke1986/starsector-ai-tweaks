package com.genir.aitweaks.core.features.shipai.systems

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.genir.aitweaks.core.debug.drawLine
import com.genir.aitweaks.core.features.shipai.AI
import com.genir.aitweaks.core.features.shipai.command
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.*
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color.BLUE
import kotlin.math.abs

class SrBurstBoost(private val ai: AI) : SystemAI {
    private val ship: ShipAPI = ai.ship
    private val system: ShipSystemAPI = ship.system
    private var burstVectors: List<BurstVector> = listOf()

    private var burstPlan: BurstPlan? = null
    private var useSystem: Boolean = false
    private var ventAfterBurst: Boolean = false

    @Suppress("ConstPropertyName")
    companion object Preset {
        const val maxBurnAngle = 2f
        const val maxRammingDistance = 400f
        const val minChaseDistance = 800f
        const val burstSpeed = 600f
    }

    override fun advance(dt: Float) {
        // Vent if burst was used to back off.
//        if (!useSystem && ventAfterBurst && system.state != ShipSystemAPI.SystemState.ACTIVE) {
        if (!useSystem && ventAfterBurst) {
            ship.command(VENT_FLUX)
            ventAfterBurst = !ship.fluxTracker.isVenting
        }

//        debugPrint["vent"] = "vent $useSystem $ventAfterBurst ${ship.fluxTracker.isVenting} ${system.state}"
//        debugPrint["backoff"] = "backoff ${ai.isBackingOff}"

        if (useSystem) {
            ship.command(USE_SYSTEM)

            val controller = ship.engineController

            if (controller.isAccelerating) log("isAccelerating")
            if (controller.isDecelerating) log("isDecelerating")
            if (controller.isAcceleratingBackwards) log("isAcceleratingBackwards")
            if (controller.isStrafingLeft) log("isStrafingLeft")
            if (controller.isStrafingRight) log("isStrafingRight")

            useSystem = false
        }

        if (canUseBurst()) {
            burstVectors = calculateBurstVectors()
            burstPlan = makeBurstPlan()

            val toExecute = shouldExecutePlannedBurst() ?: burstAtOpportunity()
            if (toExecute != null) {
                executeBurst(toExecute)
            }
        } else {
            burstVectors = listOf()
            burstPlan = null
            useSystem = false
        }

//        if (burstPlan != null) {
//            drawLine(ship.location, ship.location + burstPlan!!.burst.vector, Color.RED)
//        }

//        ai.threats.mapNotNull { estimateCollision(it) }.forEach {
//            drawLine(ship.location, ship.location + unitVector(it.facing).resized(it.distance), YELLOW)
//        }

        burstVectors.forEach {
            drawLine(ship.location + it.vector.resized(it.boundsOffset), ship.location + it.vector.resized(it.boundsOffset + burstSpeed), BLUE)
        }
    }

    override fun holdManeuverTarget(): Boolean {
        return ship.system.isOn
    }

    override fun overrideHeading(): Vector2f? {
        return null
    }

    override fun overrideFacing(): Float? {
        val plan = burstPlan ?: return null

        // Don't interrupt hardpoint bursts.
        if (ai.stats.significantWeapons.any { it.slot.isHardpoint && it.isInFiringSequence }) return null

        return ship.facing + plan.angleToTarget()
    }

    private fun canUseBurst(): Boolean {
        return when {
            // System is already scheduled to trigger.
            useSystem -> false

            system.state != ShipSystemAPI.SystemState.IDLE -> false

            !ship.canUseSystemThisFrame() -> false

            else -> true
        }
    }

    private fun makeBurstPlan(): BurstPlan? {
        // Use burst to run.
        if (ai.isBackingOff) {
            return BurstPlan(2048f, (-ai.threatVector).facing, null)
        }

        val shouldUseBurstToAttack = when {
            ai.attackTarget == null -> false

            // Save charges for better opportunities.
            system.ammo < system.maxAmmo / 2f -> false

            else -> true
        }

        if (!shouldUseBurstToAttack) return null

        val attackTarget = ai.attackTarget!!
        val (targetDistance, targetFacing) = estimateCollision(attackTarget) ?: return null
        val plan = BurstPlan(targetDistance, targetFacing, attackTarget)

//        debugPrint["distance"] = "distance ${plan.distanceToTarget()}"

        // Validate plan.
        return when {
            plan.distanceToTarget() <= maxRammingDistance -> plan
            plan.distanceToTarget() >= minChaseDistance -> plan
            else -> null
        }
    }

    private fun burstAtOpportunity(): BurstPlan? {
//        val plans: Sequence<BurstPlan> = ai.threats.asSequence().mapNotNull { target ->
//            estimateCollision(target)?.let { BurstPlan(it.first, it.second, target) }
//        }
        val plans: List<BurstPlan> = ai.threats.mapNotNull { target ->
            estimateCollision(target)?.let { BurstPlan(it.first, it.second, target) }
        }
        val validPlans = plans.filter { abs(it.angleToTarget()) <= maxBurnAngle && it.distanceToTarget() <= maxRammingDistance }

//        debugPrint.clear()
////
//        plans.forEach {
//            debugPrint[it] = "${it.angleToTarget()} ${it.distanceToTarget()} ${it.target?.hullSpec?.hullId}"
//            drawLine(ship.location, unitVector(it.targetFacing).resized(it.targetDistance) + ship.location, GREEN)
//        }

//        plans.forEach {
//        }

        val bestPlan = validPlans.minWithOrNull(compareBy { it.distanceToTarget() }) ?: return null

        return if (bestPlan.distanceToTarget() <= maxRammingDistance) bestPlan
        else null
    }

    private fun shouldExecutePlannedBurst(): BurstPlan? {
        val plan = burstPlan ?: return null

        return when {
            // Don't interrupt hardpoint warmup. It's possible to burn when weapon is already in burst.
            ai.stats.significantWeapons.any { it.slot.isHardpoint && it.isInWarmup } -> null

            abs(plan.angleToTarget()) > maxBurnAngle -> null

            else -> plan
        }
    }

    private fun executeBurst(plan: BurstPlan) {
        // Issue commands that will trigger burn in the right direction.
        val commands: Set<ShipCommand> = plan.burst.commands
        commands.forEach {
            ship.command(it)
            log(it)
        }

        // Block commands that could skew the burn direction.
        val blockedCommands = setOf(ACCELERATE, ACCELERATE_BACKWARDS, STRAFE_RIGHT, STRAFE_LEFT, DECELERATE) - commands
        blockedCommands.forEach { ship.blockCommandForOneFrame(it) }

        // Use system the next frame, when issued commands are in effect.
        useSystem = true

        ventAfterBurst = ventAfterBurst || ai.isBackingOff
    }

    inner class BurstVector(direction: Vector2f, toShipFacing: Rotation, val commands: Set<ShipCommand>) {
        val vector: Vector2f = direction.rotated(toShipFacing).resized(burstSpeed)
        val facing: Float = vector.facing
        val boundsOffset: Float = burstSpeed - boundsCollision(vector, -vector, ship)!!
    }

    /** Burst vector associated with a target. */
    inner class BurstPlan(val targetDistance: Float, val targetFacing: Float, val target: ShipAPI?) {
        val burst: BurstVector = burstVectors.minWithOrNull(compareBy { abs(MathUtils.getShortestRotation(it.facing, targetFacing)) })!!

        fun angleToTarget(): Float {
            return MathUtils.getShortestRotation(burst.facing, targetFacing)
        }

        fun distanceToTarget(): Float {
            return targetDistance - burst.boundsOffset
        }
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

        val toShipFacing = Rotation(ship.facing)
        return rawVectors.map { BurstVector(it.first, toShipFacing, it.second) }
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

    /** Estimate collision parameters with target, assuming the ship travels
     * directly at target (including target leading) with burstSpeed. */
    private fun estimateCollision(target: ShipAPI): Pair<Float, Float>? {
        val intercept = intercept(target) ?: return null
        val toIntercept = intercept - ship.location

        // Ship location and velocity in target frame of reference.
        val p = ship.location - target.location
        val v = toIntercept.resized(burstSpeed) - target.velocity

        val distance = shieldCollision(p, v, target) ?: boundsCollision(p, v, target) ?: return null
        return Pair(distance, toIntercept.facing)
    }

    private fun intercept(target: ShipAPI): Vector2f? {
        // Target location and velocity in ship frame of reference.
        val p = target.location - ship.location
        val v = target.velocity

        val time = solve(Pair(p, v), 0f, burstSpeed, 0f) ?: return null
        return target.location + v * time
    }

    /** Distance along burn vector at which collision with target shield occurs. */
    private fun shieldCollision(position: Vector2f, velocity: Vector2f, target: ShipAPI): Float? {
        if (target.shield?.isOn != true) return null
        val shield = target.shield

        val time = solve(Pair(position, velocity), shield.radius) ?: return null
        val hitPoint = position + velocity * time
        val willHitShield = vectorInArc(hitPoint, Arc(shield.activeArc, shield.facing))

        return if (willHitShield) (velocity * time).length else null
    }

    /** Distance along burn vector at which collision with target bounds occurs. */
    private fun boundsCollision(position: Vector2f, velocity: Vector2f, target: ShipAPI): Float? {
        val distanceRelative = com.genir.aitweaks.core.utils.boundsCollision(position, velocity, target)
        return distanceRelative?.let { it * velocity.length }
    }
}
