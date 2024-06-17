package com.genir.aitweaks.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.FLAG_DURATION
import com.genir.aitweaks.utils.ShipSystemAiType.BURN_DRIVE
import com.genir.aitweaks.utils.ShipSystemAiType.MANEUVERING_JETS
import com.genir.aitweaks.utils.aitStash
import com.genir.aitweaks.utils.extensions.*
import com.genir.aitweaks.utils.shieldUptime
import com.genir.aitweaks.utils.shipGrid
import com.genir.aitweaks.utils.times
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

// TODO retreat order during chase battle freezes the ship

@Suppress("MemberVisibilityCanBePrivate")
class Maneuver(val ship: ShipAPI, val maneuverTarget: ShipAPI?, internal val targetLocation: Vector2f?) {
    private val systemAIType = ship.system?.specAPI?.AIType
    private val movement = Movement(this)

    var attackTarget: ShipAPI? = maneuverTarget
    var effectiveRange: Float = 0f

    var desiredHeading: Float = ship.facing
    var desiredFacing: Float = ship.facing
    var headingPoint: Vector2f? = null
    var aimPoint: Vector2f? = null

    var isBackingOff: Boolean = false
    var isHoldingFire: Boolean = false
    var isAvoidingBorder: Boolean = false
    var is1v1: Boolean = false

    private var idleTime = 0f
    private var threats: List<ShipAPI> = listOf()
    internal var threatVector = Vector2f()

    fun advance(dt: Float) {
        ship.aitStash.maneuverAI = this

        if (shouldEndManeuver()) {
            ship.shipAI.cancelCurrentManeuver()
            ship.aitStash.maneuverAI = null
        }

        // Update state.
        updateThreats()
        effectiveRange = ship.effectiveRange(Preset.effectiveDpsThreshold)

        updateIdleTime(dt)
        updateBackoffStatus()
        updateAttackTarget()
        update1v1Status()

        ventIfNeeded()
        holdFireIfOverfluxed()
        manageMobilitySystems()

        movement.advance(dt)

        ship.aiFlags.setFlag(MANEUVER_RANGE_FROM_TARGET, ship.minRange)
        ship.aiFlags.setFlag(MANEUVER_TARGET, FLAG_DURATION, maneuverTarget)
    }

    /** Method called by ShipAI to set ship heading. It is not called when ShipAI
     * is avoiding collision. But since ShipAI collision avoidance is overriden,
     * setting heading by Maneuver needs to be done each frame, in advance method. */
    fun doManeuver() = Unit

    private fun shouldEndManeuver(): Boolean {
        return when {
            // Target ship was destroyed.
            maneuverTarget != null && (maneuverTarget.isExpired || !maneuverTarget.isAlive) -> {
                true
            }

            // Arrived at location.
            targetLocation != null && (ship.location - targetLocation).length() <= Preset.arrivedAtLocationRadius -> {
                true
            }

            else -> false
        }
    }

    /** Select which enemy ship to attack. This may be different
     * from the maneuver target provided by the ShipAI. */
    private fun updateAttackTarget() {
        // Attack target is stored in a flag, so it carries over between Maneuver instances.
        val currentTarget: ShipAPI? = ship.aitStash.attackTarget

        val updateTarget = when {
            currentTarget == null -> true
            !currentTarget.isValidTarget -> true

            // Do not interrupt bursts.
            ship.primaryWeapons.find { it.isInFiringSequence && it.autofireAI?.targetShip == attackTarget } != null -> false

            // Target is out of range.
            engagementRange(currentTarget) < ship.maxRange -> true

            // Finish helpless target.
            currentTarget.fluxTracker.isOverloadedOrVenting -> false

            else -> true
        }

        val updatedTarget = if (updateTarget) {
            val opportunities = threats.filter { engagementRange(it) < ship.maxRange }
            val target = opportunities.minWithOrNull { o1, o2 -> (evaluateTarget(o1) - evaluateTarget(o2)).sign.toInt() }
            target ?: maneuverTarget
        } else currentTarget

        ship.aitStash.attackTarget = updatedTarget
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

    private fun manageMobilitySystems() {
        when (systemAIType) {

            MANEUVERING_JETS -> {
                val shouldUse = when {
                    !ship.canUseSystemThisFrame() -> false

                    // Use MANEUVERING_JETS to back off. Vanilla AI does
                    // this already, but is not determined enough.
                    isBackingOff -> true

                    // Use MANEUVERING_JETS to chase target during 1v1 duel.
                    is1v1 && engagementRange(attackTarget!!) > effectiveRange -> true

                    else -> false
                }

                if (shouldUse) ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0)
            }

            // Prevent vanilla AI from jumping closer to target with
            // BURN_DRIVE, if the target is already within weapons range.
            BURN_DRIVE -> {
                if (attackTarget != null && engagementRange(attackTarget!!) < effectiveRange) {
                    ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM)
                }
            }

            // TODO BURN_DRIVE_TOGGLE

            else -> Unit
        }
    }

    internal fun engagementRange(target: ShipAPI): Float {
        return (target.location - ship.location).length()
    }

    private fun updateThreats() {
        val rangeEnvelope = 500f
        val r = 2f * max(Preset.threatEvalRadius, ship.maxRange + rangeEnvelope)
        val allShips = shipGrid().getCheckIterator(ship.location, r, r).asSequence().filterIsInstance<ShipAPI>()

        threats = allShips.filter { it.owner != ship.owner && it.isAlive && !it.isExpired && it.isShip }.toList()

        threatVector = threats.fold(Vector2f()) { sum, it ->
            val dp = it.deploymentPoints
            val dir = (it.location - ship.location).resized(1f)
            sum + dir * dp * dp
        }
    }

    /** Evaluate if target is worth attacking. The lower the score, the better the target. */
    private fun evaluateTarget(target: ShipAPI): Float {
        // Prioritize targets closer to ship facing.
        val angle = ship.shortestRotationToTarget(target.location) * PI.toFloat() / 180.0f
        val angleWeight = 0.75f
        val evalAngle = abs(angle) * angleWeight

        // Prioritize closer targets. Avoid attacking targets out of effective weapons range.
        val dist = engagementRange(target)
        val distWeight = 1f / ship.dpsFractionAtRange(dist)
        val evalDist = (dist / ship.maxRange) * distWeight

        // Prioritize targets high on flux. Avoid hunting low flux phase ships.
        val fluxLeft = (1f - target.fluxLevel)
        val fluxFactor = if (target.phaseCloak?.specAPI?.isPhaseCloak == true) 2f else 0.5f
        val evalFlux = fluxLeft * fluxFactor

        // Avoid attacking bricks.
        val evalDamper = if (target.system?.id == "damper" && !ship.isFrigate) 1f else 0f
        val evalShunt = if (target.variant.hasHullMod("fluxshunt")) 4f else 0f

        // Assign lower priority to frigates.
        val evalType = if (target.isFrigate && !target.isModule) 1f else 0f

        // TODO avoid wrecks

        return evalAngle + evalDist + evalFlux + evalDamper + evalShunt + evalType
    }
}
