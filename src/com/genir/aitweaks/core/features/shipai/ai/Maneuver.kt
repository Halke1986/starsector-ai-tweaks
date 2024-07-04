package com.genir.aitweaks.core.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.FLAG_DURATION
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize.SMALL
import com.genir.aitweaks.core.GlobalState
import com.genir.aitweaks.core.utils.extensions.*
import com.genir.aitweaks.core.utils.shieldUptime
import com.genir.aitweaks.core.utils.shipSequence
import com.genir.aitweaks.core.utils.times
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max

// TODO retreat order during chase battle freezes the ship

@Suppress("MemberVisibilityCanBePrivate")
class Maneuver(val ship: ShipAPI, vanillaManeuverTarget: ShipAPI?, vanillaMoveOrderLocation: Vector2f?) {
    private val movement = Movement(this)

    val moveOrderLocation: Vector2f?
    val maneuverTarget: ShipAPI? //= selectManeuverTarget(vanillaManeuverTarget)
    var attackTarget: ShipAPI? //= stash.attackTarget

    var effectiveRange: Float = 0f
    var minRange: Float = 0f
    var maxRange: Float = 0f
    var totalCollisionRadius: Float = 0f

    // Required by vanilla logic.
    var desiredHeading: Float = ship.facing
    var desiredFacing: Float = ship.facing

    // Used for communication with attack coordinator.
    var proposedHeadingPoint: Vector2f? = null
    var reviewedHeadingPoint: Vector2f? = null

    var headingPoint: Vector2f? = null
    var aimPoint: Vector2f? = null

    // State
    var isBackingOff: Boolean = false
    var isHoldingFire: Boolean = false
    var isAvoidingBorder: Boolean = false
    var is1v1: Boolean = false

    private var idleTime = 0f
    private var threats: List<ShipAPI> = listOf()
    internal var threatVector = Vector2f()

    /** Attempt to stay on tar*/
    init {
        val oldManeuver = ship.customAI

        // Continue attacking the same target as previous custom AI instance.
        attackTarget = oldManeuver?.attackTarget

        // When burn drive is activated, vanilla AI passes no orders to Maneuver instance.
        // In that case, try to follow previous Maneuver instance orders.
        val noOrders = vanillaManeuverTarget == null && vanillaMoveOrderLocation == null

        moveOrderLocation = when {
            noOrders -> oldManeuver?.moveOrderLocation
            vanillaMoveOrderLocation != null -> vanillaMoveOrderLocation
            else -> null
        }

        maneuverTarget = when {
            noOrders -> oldManeuver?.maneuverTarget
            vanillaManeuverTarget != null -> selectManeuverTarget(vanillaManeuverTarget)
            else -> null
        }
    }

    fun advance(dt: Float) {
        ship.storeCustomAI(this)

        if (shouldEndManeuver()) {
            ship.shipAI.cancelCurrentManeuver()
        }

        // Update state.
        updateThreats()
        effectiveRange = ship.effectiveRange(Preset.effectiveDpsThreshold)
        minRange = ship.minRange
        maxRange = ship.maxRange
        totalCollisionRadius = ship.totalCollisionRadius

        updateIdleTime(dt)
        updateBackoffStatus()
        updateAttackTarget()
        update1v1Status()

        ventIfNeeded()
        holdFireIfOverfluxed()

        movement.advance(dt)

        ship.aiFlags.setFlag(MANEUVER_RANGE_FROM_TARGET, minRange)
        ship.aiFlags.setFlag(MANEUVER_TARGET, FLAG_DURATION, maneuverTarget)
    }

    /** Method called by ShipAI to set ship heading. It is not called when ShipAI
     * is avoiding collision. But since ShipAI collision avoidance is overriden,
     * setting heading by Maneuver needs to be done each frame, in advance method. */
    fun doManeuver() = Unit

    /** Custom ship AI uses fleet cohesion directly, instead of through orders. */
    private fun selectManeuverTarget(vanillaManeuverTarget: ShipAPI?): ShipAPI? {
        return GlobalState.fleetCohesion?.findValidTarget(ship, vanillaManeuverTarget) ?: vanillaManeuverTarget
    }

    private fun shouldEndManeuver(): Boolean {
        return when {
            // Target ship was destroyed.
            maneuverTarget != null && (maneuverTarget.isExpired || !maneuverTarget.isAlive) -> {
                true
            }

            // Arrived at location.
            moveOrderLocation != null && (ship.location - moveOrderLocation).length() <= Preset.arrivedAtLocationRadius -> {
                true
            }

            else -> false
        }
    }

    /** Select which enemy ship to attack. This may be different
     * from the maneuver target provided by the ShipAI. */
    private fun updateAttackTarget() {
        val currentTarget = attackTarget

        val updateTarget = when {
            currentTarget == null -> true
            !currentTarget.isValidTarget -> true

            // Do not interrupt weapon bursts.
            ship.primaryWeapons.find { it.size != SMALL && it.isInFiringSequence && it.autofireAI?.targetShip == attackTarget } != null -> false

            // Target is out of range.
            range(currentTarget) > maxRange -> true

            // Finish helpless target.
            currentTarget.fluxTracker.isOverloadedOrVenting -> false

            else -> true
        }

        val updatedTarget = if (updateTarget) findNewAttackTarget()
        else currentTarget

        ship.shipTarget = updatedTarget
        attackTarget = updatedTarget
    }

    /** Decide if ships needs to back off due to high flux level */
    private fun updateBackoffStatus() {
        val backingOffFlag = ship.aiFlags.hasFlag(BACKING_OFF)
        val fluxLevel = ship.fluxTracker.fluxLevel

        isBackingOff = when {
            Global.getCombatEngine().isEnemyInFullRetreat -> false

            // Start backing off.
            fluxLevel > Preset.backoffUpperThreshold -> true

            // Stop backing off.
            backingOffFlag && fluxLevel <= 0.01f -> false

            // Continue backing off.
            else -> backingOffFlag
        }

        if (isBackingOff) ship.aiFlags.setFlag(BACKING_OFF)
        else if (backingOffFlag) ship.aiFlags.unsetFlag(BACKING_OFF)
    }

    private fun updateIdleTime(dt: Float) {
        val shieldIsUp = ship.shield?.isOn == true && shieldUptime(ship.shield) > Preset.shieldFlickerThreshold
        val pdIsFiring = ship.allWeapons.firstOrNull { it.isPD && it.isFiring } != null

        idleTime = if (shieldIsUp || pdIsFiring) 0f
        else idleTime + dt
    }

    /** Is ship engaged in 1v1 duel with the target. */
    private fun update1v1Status() {
        is1v1 = when {
            isAvoidingBorder -> false
            isBackingOff -> false
            attackTarget == null -> false
            attackTarget != maneuverTarget -> false
            attackTarget!!.isFrigate != ship.isFrigate -> false
            threats.size > 1 -> false
            else -> true
        }
    }

    private fun holdFireIfOverfluxed() {
        isHoldingFire = ship.fluxTracker.fluxLevel > Preset.holdFireThreshold

        if (isHoldingFire) {
            ship.allWeapons.filter { !it.isPD && it.fluxCostToFire != 0f }.mapNotNull { it.autofirePlugin }.forEach { it.forceOff() }
        }
    }

    /** Force vent when the ship is backing off,
     * not shooting and with shields down. */
    private fun ventIfNeeded() {
        val shouldVent = when {
            !isBackingOff -> false
            ship.fluxTracker.isVenting -> false
            ship.fluxLevel < 0.01f -> false
            ship.fluxLevel < Preset.backoffLowerThreshold -> true
            idleTime < Preset.shieldDownVentTime -> false
            ship.allWeapons.firstOrNull { it.autofireAI?.shouldFire() == true } != null -> false
            else -> true
        }

        if (shouldVent) ship.giveCommand(ShipCommand.VENT_FLUX, null, 0)
    }

    internal fun range(target: ShipAPI): Float {
        return (target.location - ship.location).length() - target.collisionRadius / 2f
    }

    private fun updateThreats() {
        val rangeEnvelope = 500f
        val r = max(Preset.threatEvalRadius, maxRange + rangeEnvelope)
        threats = shipSequence(ship.location, r).filter { isThreat(it) }.toList()

        threatVector = threats.fold(Vector2f()) { sum, it ->
            val dp = it.deploymentPoints
            val dir = (it.location - ship.location).resized(1f)
            sum + dir * dp * dp
        }
    }

    private fun findNewAttackTarget(): ShipAPI? {
        val opportunities = threats.filter { range(it) < maxRange }

        val foldInit: Pair<ShipAPI?, Float> = Pair(null, Float.MAX_VALUE)
        val bestOpportunity = opportunities.fold(foldInit) { best, it ->
            val eval = evaluateTarget(it)
            if (eval < best.second) Pair(it, eval)
            else best
        }.first

        return when {
            bestOpportunity != null -> bestOpportunity

            maneuverTarget != null -> maneuverTarget

            // Try to find a target near move location.
            moveOrderLocation != null -> {
                shipSequence(moveOrderLocation, 200f).firstOrNull { isThreat(it) }
            }

            else -> null
        }
    }

    /** Evaluate if target is worth attacking. The lower the score, the better the target. */
    private fun evaluateTarget(target: ShipAPI): Float {
        // Prioritize targets closer to ship facing.
        val angle = ship.shortestRotationToTarget(target.location) * PI.toFloat() / 180.0f
        val angleWeight = 0.75f
        val evalAngle = abs(angle) * angleWeight

        // Prioritize closer targets. Avoid attacking targets out of effective weapons range.
        val dist = range(target)
        val distWeight = 1f / ship.dpsFractionAtRange(dist)
        val evalDist = (dist / maxRange) * distWeight

        // Prioritize targets high on flux. Avoid hunting low flux phase ships.
        val fluxLeft = (1f - target.fluxLevel)
        val fluxFactor = if (target.phaseCloak?.specAPI?.isPhaseCloak == true) 2f else 0.5f
        val evalFlux = fluxLeft * fluxFactor

        // Avoid attacking bricks, especially Monitors.
        val evalDamper = if (target.system?.id == "damper" && !target.isFrigate) 1f else 0f
        val evalShunt = if (target.variant.hasHullMod("fluxshunt") && target.isFrigate) 256f else 0f

        // Assign lower priority to frigates.
        val evalType = if (target.isFrigate && !target.isModule) 1f else 0f

        // TODO avoid wrecks

        return evalAngle + evalDist + evalFlux + evalDamper + evalShunt + evalType
    }

    private fun isThreat(target: ShipAPI): Boolean {
        return target.owner != ship.owner && target.isAlive && !target.isExpired && target.isShip
    }
}
