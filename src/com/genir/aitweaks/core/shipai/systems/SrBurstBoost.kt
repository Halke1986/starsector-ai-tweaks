package com.genir.aitweaks.core.shipai.systems

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.*
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.RotationMatrix.Companion.rotated
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lwjgl.util.vector.Vector2f

class SrBurstBoost(ai: CustomShipAI) : SystemAI(ai) {
    private var burstVectors: List<BurstVector> = listOf()
    private var hardpoints: List<WeaponAPI> = listOf()

    private var burstPlan: BurstPlan? = null
    private var useSystem: Boolean = false
    private var ventAfterBurst: Boolean = false

    companion object Preset {
        const val burstTriggerAngle = 1f
        const val maxOpportunityBurstAngle = 7f
        const val maxRammingDistance = 400f
        const val minChaseDistance = 700f
        const val burstSpeed = 600f
    }

    override fun advance(dt: Float) {
        // Vent if burst was used to back off.
        if (!useSystem && ventAfterBurst) {
            ship.command(VENT_FLUX)
            ventAfterBurst = !ship.fluxTracker.isVenting
        }

        // Execute scheduled system use.
        if (useSystem) {
            ship.command(USE_SYSTEM)
            useSystem = false
        }

        if (canUseBurst()) {
            hardpoints = ai.stats.significantWeapons.filter { it.slot.isHardpoint }
            burstVectors = calculateBurstVectors()
            burstPlan = updatePlannedBurst()
            if (shouldExecuteBurstPlan()) executeBurstPlan(burstPlan!!)
        } else {
            hardpoints = listOf()
            burstVectors = listOf()
            burstPlan = null
            useSystem = false
        }
    }

    /** Rotate the ship to point selected burst vector at target. */
    override fun overrideFacing(): Direction? {
        val plan = burstPlan ?: return null

        // Don't interrupt hardpoint bursts. When flux is high, the weapon
        // may be stuck in warmup loop, so execute burst when backing off.
        if (!ai.ventModule.isBackingOff && hardpoints.any { it.isInFiringSequence }) return null

        return ship.facing.direction + plan.angleToTarget()
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

    private fun updatePlannedBurst(): BurstPlan? {
        // Use burst to back off.
        if (ai.ventModule.isBackingOff) {
            return makeBurstPlan(ai.movement.headingPoint - ship.location, burstVectors, null)
        }

        // Use burst to ram opportunity target.
        val plans: Sequence<BurstPlan> = ai.threats.asSequence().mapNotNull { target ->
            estimateCollision(target)?.let { makeBurstPlan(it, burstVectors, target) }
        }
        val validAngle = plans.filter { it.angleToTarget().length <= maxOpportunityBurstAngle }
        val validPlans = validAngle.filter { it.distanceToTarget() <= maxRammingDistance }

        validPlans.minWithOrNull(compareBy { it.distanceToTarget() })?.let { return it }

        // Use burst to ram attack target.
        val attackTarget = ai.attackTarget as? ShipAPI ?: return null
        val collision = estimateCollision(attackTarget) ?: return null

        val frontVectors = burstVectors.filter { !it.commands.contains(ACCELERATE_BACKWARDS) }
        val allPlans = frontVectors.map { BurstPlan(it, collision, attackTarget) }
        val rammingPlans = allPlans.filter { it.distanceToTarget() <= maxRammingDistance }
        rammingPlans.minWithOrNull(compareBy { it.angleToTarget().length })?.let { return it }

        // Try the shortest rotation burst to approach target.
        // Save charges for better opportunities.
        if (system.ammo < system.maxAmmo / 2f) return null

        val shortestRotationPlan = makeBurstPlan(collision, frontVectors, attackTarget)
        return if (shortestRotationPlan.distanceToTarget() >= minChaseDistance) shortestRotationPlan
        else null
    }

    private fun shouldExecuteBurstPlan(): Boolean {
        return when {
            burstPlan == null -> false

            // Don't interrupt hardpoint warmup. It's possible to burn when
            // weapon is already in burst. When flux is high, the weapon may
            // be stuck in warmup loop, so execute burst when backing off.
            !ai.ventModule.isBackingOff && hardpoints.any { it.isInWarmup } -> false

            // Ship is not aligned for burst.
            burstPlan!!.angleToTarget().length > burstTriggerAngle -> false

            else -> true
        }
    }

    private fun executeBurstPlan(plan: BurstPlan) {
        // Issue commands that will trigger burn in the right direction.
        val commands: Set<ShipCommand> = plan.burst.commands
        commands.forEach { ship.command(it) }

        // Block commands that could skew the burn direction.
        val blockedCommands = setOf(ACCELERATE, ACCELERATE_BACKWARDS, STRAFE_RIGHT, STRAFE_LEFT, DECELERATE) - commands
        blockedCommands.forEach { ship.blockCommandForOneFrame(it) }

        // Use system the next frame, when issued commands are in effect.
        useSystem = true

        // Schedule vent after burst if ship is backing off.
        ventAfterBurst = ai.ventModule.isBackingOff
    }

    /** Description of one of the eight possible burst vectors around the ship. */
    inner class BurstVector(direction: Vector2f, toShipFacing: RotationMatrix, val commands: Set<ShipCommand>) {
        val vector: Vector2f = direction.rotated(toShipFacing).resized(burstSpeed)
        val facing: Direction = vector.facing
        val boundsOffset: Float = burstSpeed - boundsCollision(vector, -vector, ship)!!
    }

    /** Burst vector associated with heading towards a target. */
    data class BurstPlan(val burst: BurstVector, val toTarget: Vector2f, val target: ShipAPI?) {
        private val targetDistance: Float = toTarget.length
        private val targetFacing: Direction = toTarget.facing

        fun angleToTarget(): Direction {
            return shortestRotation(burst.facing, targetFacing)
        }

        fun distanceToTarget(): Float {
            return targetDistance - burst.boundsOffset
        }
    }

    private fun makeBurstPlan(toTarget: Vector2f, burstVectors: List<BurstVector>, target: ShipAPI?): BurstPlan {
        // Find burst vector best aligned with direction to target.
        val burst: BurstVector = burstVectors.minWithOrNull(compareBy { (toTarget.facing - it.facing).length })!!

        return BurstPlan(burst, toTarget, target)
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

        val toShipFacing = ship.facing.direction.rotationMatrix
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
    private fun estimateCollision(target: ShipAPI): Vector2f? {
        val intercept = intercept(target) ?: return null
        val toIntercept = intercept - ship.location

        // Ship location and velocity in target frame of reference.
        val p = ship.location - target.location
        val v = toIntercept.resized(burstSpeed) - target.velocity

        val distance = shieldCollision(p, v, target) ?: boundsCollision(p, v, target) ?: return null
        return toIntercept.resized(distance)
    }

    private fun intercept(target: ShipAPI): Vector2f? {
        // Target location and velocity in ship frame of reference.
        val p = target.location - ship.location
        val v = target.velocity

        val time = solve(Pair(p, v), 0f, burstSpeed, 0f, 0f) ?: return null
        return target.location + v * time
    }

    /** Distance along burn vector at which collision with target shield occurs. */
    private fun shieldCollision(position: Vector2f, velocity: Vector2f, target: ShipAPI): Float? {
        if (target.shield?.isOn != true) return null
        val shield = target.shield

        val time = solve(Pair(position, velocity), shield.radius) ?: return null
        val hitPoint = position + velocity * time
        val willHitShield = Arc(shield.activeArc, Direction(shield.facing)).contains(hitPoint)

        return if (willHitShield) (velocity * time).length else null
    }

    /** Distance along burn vector at which collision with target bounds occurs. */
    private fun boundsCollision(position: Vector2f, velocity: Vector2f, target: ShipAPI): Float? {
        val distanceRelative = state.bounds.collision(position, velocity, target)
        return distanceRelative?.let { it * velocity.length }
    }
}
