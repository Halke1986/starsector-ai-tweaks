package com.genir.aitweaks.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.FLAG_DURATION
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.ShipSystemAiType.BURN_DRIVE
import com.genir.aitweaks.utils.ShipSystemAiType.MANEUVERING_JETS
import com.genir.aitweaks.utils.extensions.*
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*

const val threatEvalRadius = 2500f
const val aimOffsetSamples = 45

const val effectiveDpsThreshold = 0.80f

// Flux management
const val backoffUpperThreshold = 0.75f
const val backoffLowerThreshold = backoffUpperThreshold * 0.6f // will force vent
const val holdFireThreshold = 0.9f

// Idle time calculation
const val shieldDownVentTime = 2.0f
const val shieldFlickerThreshold = 0.5f

// Map movement calculation
const val arrivedAtLocationRadius = 2000f
const val borderCornerRadius = 4000f
const val borderNoGoZone = 3000f
const val borderHardNoGoZone = borderNoGoZone / 2f

// TODO retreat order during chase battle freezes the ship

@Suppress("MemberVisibilityCanBePrivate")
class Maneuver(val ship: ShipAPI, val maneuverTarget: ShipAPI?, private val targetLocation: Vector2f?) {
    private val engineController = EngineController(ship)
    private val systemAIType = ship.system?.specAPI?.AIType
    private val targetFinder = TargetFinder()

    // Make strafe rotation direction random, but consistent for a given ship.
    private val strafeRotation = Rotation(if (ship.id.hashCode() % 2 == 0) 10f else -10f)

    var desiredHeading: Float = ship.facing
    var desiredFacing: Float = ship.facing
    var headingPoint: Vector2f? = null
    var aimPoint: Vector2f? = null

    var attackTarget: ShipAPI? = maneuverTarget
    var effectiveRange: Float = 0f

    var isBackingOff: Boolean = false
    var isHoldingFire: Boolean = false
    var isAvoidingBorder: Boolean = false
    var is1v1: Boolean = false

    private var averageAimOffset = RollingAverageFloat(aimOffsetSamples)
    private var idleTime = 0f
    private var threats: List<ShipAPI> = listOf()
    private var threatVector = Vector2f()

    fun advance(dt: Float) {
        ship.AITStash.maneuverAI = this

        if (shouldEndManeuver()) {
            ship.shipAI.cancelCurrentManeuver()
            ship.AITStash.maneuverAI = null
        }

        // Update state.
        updateThreats()
        effectiveRange = ship.effectiveRange(effectiveDpsThreshold)

        updateIdleTime(dt)
        updateBackoffStatus()
        updateAttackTarget()
        update1v1Status()

        ventIfNeeded()
        holdFireIfOverfluxed()
        manageMobilitySystems()

        setFacing()
        setHeading(dt)

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
            targetLocation != null && (ship.location - targetLocation).length() <= arrivedAtLocationRadius -> {
                true
            }

            else -> false
        }
    }

    /** Select which enemy ship to attack. This may be different
     * from the maneuver target provided by the ShipAI. */
    private fun updateAttackTarget() {
        // Attack target is stored in a flag, so it carries over between Maneuver instances.
        val currentTarget: ShipAPI? = ship.AITStash.attackTarget

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

        val updatedTarget = if (updateTarget) targetFinder.target ?: maneuverTarget
        else currentTarget

        ship.AITStash.attackTarget = updatedTarget
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
            fluxLevel > backoffUpperThreshold -> true

            // Stop backing off.
            backingOffFlag && fluxLevel <= 0.01f -> false

            // Continue backing off.
            else -> backingOffFlag
        }

        if (isBackingOff) ship.aiFlags.setFlag(BACKING_OFF)
        else if (backingOffFlag) ship.aiFlags.unsetFlag(BACKING_OFF)
    }

    private fun updateIdleTime(dt: Float) {
        val shieldIsUp = ship.shield?.isOn == true && shieldUptime(ship.shield) > shieldFlickerThreshold
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
        isHoldingFire = ship.fluxTracker.fluxLevel > holdFireThreshold

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
            ship.fluxLevel < backoffLowerThreshold -> true
            idleTime < shieldDownVentTime -> false
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

    private fun setFacing() {
        val (aimPoint, velocity) = when {
            // Face the attack target.
            attackTarget != null -> {
                val target = attackTarget!!

                // Average aim offset to avoid ship wobbling.
                val aimPointThisFrame = calculateOffsetAimPoint(target)
                val aimOffsetThisFrame = getShortestRotation(target.location, ship.location, aimPointThisFrame)
                val aimOffset = averageAimOffset.update(aimOffsetThisFrame)

                Pair(target.location.rotatedAroundPivot(Rotation(aimOffset), ship.location), target.velocity)
            }

            // Face threat direction when backing off and no target.
            isBackingOff && !threatVector.isZeroVector() -> {
                Pair(ship.location + threatVector, Vector2f())
            }

            // Move to location, if no attack target.
            targetLocation != null -> {
                Pair(targetLocation, Vector2f())
            }

            // Nothing to do. Stop rotation.
            else -> Pair(ship.location, Vector2f())
        }

        this.aimPoint = aimPoint
        desiredFacing = engineController.facing(aimPoint, velocity)
    }

    private fun setHeading(dt: Float) {
        val (headingPoint, velocity) = when {
            // Move opposite to threat direction when backing off.
            // If there's no threat, the ship will continue to coast.
            isBackingOff -> {
                if (threatVector.isZeroVector()) Pair(ship.location, ship.velocity.resized(ship.maxSpeed))
                else Pair(ship.location - threatVector.resized(1000f), Vector2f())
            }

            // Move directly to ordered location.
            targetLocation != null -> {
                Pair(targetLocation, Vector2f())
            }

            // Orbit target at effective weapon range.
            // Rotate away from threat if there are multiple enemy ships around.
            // Chase the target if there are no other enemy ships around.
            // Strafe target randomly if in range and no other threat.
            maneuverTarget != null -> {
                // TODO will need syncing when interval tracking is introduced.
                val vectorToTarget = maneuverTarget.location - ship.location
                val vectorToThreat = if (!threatVector.isZeroVector()) threatVector else vectorToTarget

                // Strafe the target randomly, when it's the only threat.
                val shouldStrafe = is1v1 && engagementRange(maneuverTarget) <= effectiveRange
                val attackPositionOffset = if (shouldStrafe) vectorToThreat.rotated(strafeRotation)
                else vectorToThreat

                val headingPoint = maneuverTarget.location - attackPositionOffset.resized(ship.minRange)
                val velocity = (headingPoint - (this.headingPoint ?: headingPoint)) / dt
                Pair(headingPoint, velocity)
            }

            // Nothing to do, stop the ship.
            else -> Pair(ship.location, Vector2f())
        }

        // Avoid border. When in border zone, do not attempt to lead
        // target, as it may lead to deeper intrusion into the border zone.
        val censoredHeadingPoint = avoidBorder(headingPoint)
        val censoredVelocity = if (censoredHeadingPoint == headingPoint) velocity else Vector2f()

        this.headingPoint = censoredHeadingPoint
        desiredHeading = engineController.heading(censoredHeadingPoint, censoredVelocity)
    }

    /** Make the ship avoid map border. The ship will attempt to move
     * inside a rectangle with rounded corners placed `borderNoGoZone`
     * units from map border.*/
    private fun avoidBorder(headingPoint: Vector2f): Vector2f {
        isAvoidingBorder = false

        val mapH = Global.getCombatEngine().mapHeight / 2f
        val mapW = Global.getCombatEngine().mapWidth / 2f
        val borderZone = borderNoGoZone + borderCornerRadius

        // Translate ship coordinates so that the ship appears to be
        // always near a map corner. That way we can use a circle
        // calculations to approximate a rectangle with rounded corners.
        val l = ship.location
        val dirToBorder = Vector2f()
        dirToBorder.x = if (l.x > 0) (l.x - mapW + borderZone).coerceAtLeast(0f)
        else (l.x + mapW - borderZone).coerceAtMost(0f)
        dirToBorder.y = if (l.y > 0) (l.y - mapH + borderZone).coerceAtLeast(0f)
        else (l.y + mapH - borderZone).coerceAtMost(0f)

        // Distance into the border zone.
        val d = (dirToBorder.length() - borderCornerRadius).coerceAtLeast(0f)

        // Ship is far from border, no avoidance required.
        if (d == 0f) return headingPoint

        val borderFacing = dirToBorder.getFacing()
        val travelFacing = (headingPoint - ship.location).getFacing()
        val intrusionAngle = MathUtils.getShortestRotation(borderFacing, travelFacing)

        // Ship attempts to move away from the border on its own.
        if (abs(intrusionAngle) > 90f) return headingPoint

        // Allow chasing targets into the border zone.
        val attackAngle = attackTarget?.let { (it.location - ship.location).getFacing() - borderFacing }
        val absAllowedAngle = if (attackAngle?.sign == intrusionAngle.sign) min(90f, abs(attackAngle))
        else 90f

        if (abs(intrusionAngle) + 1f >= absAllowedAngle) return headingPoint

        // The closer the ship is to map edge, the stronger
        // the heading transformation away from the border.
        val avoidForce = (d / (borderNoGoZone - borderHardNoGoZone))
        val correctionSign = if (intrusionAngle.sign > 0) 1f else -1f
        val correctionAngle = (absAllowedAngle - abs(intrusionAngle)) * correctionSign * avoidForce

        isAvoidingBorder = true
        return headingPoint.rotatedAroundPivot(Rotation(correctionAngle), ship.location)
    }

    private fun engagementRange(target: ShipAPI): Float {
        return (target.location - ship.location).length()
    }

    /** Aim hardpoint weapons with entire ship, if possible. */
    private fun calculateOffsetAimPoint(attackTarget: ShipAPI): Vector2f {
        // Find intercept points of all hardpoints attacking the current target.
        val hardpoints = ship.allWeapons.filter { it.slot.isHardpoint }.mapNotNull { it.autofireAI }
        val aimedHardpoints = hardpoints.filter { it.targetShip != null && it.targetShip == attackTarget }
        val interceptPoints = aimedHardpoints.mapNotNull { it.intercept }

        if (interceptPoints.isEmpty()) return attackTarget.location

        // Average the intercept points. This may cause poor aim if different hardpoints
        // have weapons with significantly different projectile velocities.
        val interceptSum = interceptPoints.fold(Vector2f()) { sum, intercept -> sum + intercept }
        val aimPoint = interceptSum / interceptPoints.size.toFloat()

        return aimPoint
    }

    // TODO ensure stations are counted
    private fun updateThreats() {
        val rangeEnvelope = 500f
        val r = 2f * max(threatEvalRadius, ship.maxRange + rangeEnvelope)
        val allShips = shipGrid().getCheckIterator(ship.location, r, r).asSequence().filterIsInstance<ShipAPI>()

        threats = allShips.filter { it.owner != ship.owner && it.isValidTarget && it.isShip }.toList()

        threatVector = threats.fold(Vector2f()) { sum, it ->
            val dp = it.deploymentPoints
            val dir = (it.location - ship.location).resized(1f)
            sum + dir * dp * dp
        }
    }

    inner class TargetFinder {
        val target: ShipAPI?
            get() {
                val opportunities = threats.filter { engagementRange(it) < ship.maxRange }
                return opportunities.minWithOrNull { o1, o2 -> (evaluateTarget(o1) - evaluateTarget(o2)).sign.toInt() }
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
}
