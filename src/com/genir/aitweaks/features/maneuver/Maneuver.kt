package com.genir.aitweaks.features.maneuver

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.FLAG_DURATION
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.MISSILE
import com.genir.aitweaks.asm.combat.ai.AssemblyShipAI
import com.genir.aitweaks.debug.debugPlugin
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.ShipSystemAiType.BURN_DRIVE
import com.genir.aitweaks.utils.ShipSystemAiType.MANEUVERING_JETS
import com.genir.aitweaks.utils.extensions.*
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.max

const val threatEvalRadius = 2500f
const val aimOffsetSamples = 45

const val backoffUpperThreshold = 0.75f
const val backoffLowerThreshold = 0.2f

const val shieldDownVentTime = 2.0f
const val shieldFlickerThreshold = 0.5f

const val arrivedAtLocationRadius = 2000f

@Suppress("MemberVisibilityCanBePrivate")
class Maneuver(val ship: ShipAPI, val maneuverTarget: ShipAPI?, private val targetLocation: Vector2f?) {
    private val engineController = EngineController()
    private val shipAI = ship.ai as AssemblyShipAI

    val isDirectControl: Boolean = true
    var desiredHeading: Float = ship.facing
    var desiredFacing: Float = ship.facing

    var attackTarget: ShipAPI? = maneuverTarget
    var isBackingOff: Boolean = false
    var aimPoint: Vector2f? = null

    private var range: Float = 0f
    private var averageOffset = RollingAverageFloat(aimOffsetSamples)
    private var idleTime = 0f

    // Make strafe rotation direction random, but consistent for a given ship.
    private val strafeRotation = Rotation(if (ship.id.hashCode() % 2 == 0) 10f else -10f)
    private val systemAIType = ship.system?.specAPI?.AIType

    private var threatVector = calculateThreatDirection(ship.location)

    fun advance(dt: Float) {
        ship.AITStash.maneuverAI = this

        if (shouldEndManeuver()) {
            shipAI.cancelCurrentManeuver()
            ship.AITStash.maneuverAI = null
        }

        // Update state.
        val weapons = primaryWeapons()
        range = range(weapons)

        threatVector = calculateThreatDirection(ship.location)

        updateAttackTarget()
        updateBackoffStatus()
        updateIdleTime(dt)

        ventIfNeeded()
        manageMobilitySystems()

        debugPlugin["backoff"] = if (isBackingOff) "is backing off" else ""

        ship.aiFlags.setFlag(MANEUVER_RANGE_FROM_TARGET, range)
        ship.aiFlags.setFlag(MANEUVER_TARGET, FLAG_DURATION, maneuverTarget)

        // Facing is controlled in advance() instead of doManeuver()
        // because doManeuver() is not called when ShipAI decides
        // to perform collision avoidance. ShipAI collision avoidance
        // overrides only heading control, so we still need to
        // perform facing control.
        setFacing()

        debugPlugin["collision"] = ""
        val engineAI = shipAI.engineAI as OverrideEngineAI
        if (engineAI.aiIsAvoidingCollision) {
            if (isBackingOff) {
                debugPlugin["collision"] = "override collision ai"
                setHeading()
            } else {
                debugPlugin["collision"] = "avoiding collision"
            }
        }
    }

    fun doManeuver() = setHeading()

    private fun shouldEndManeuver(): Boolean {
        // Target ship was destroyed.
        if (maneuverTarget != null && (maneuverTarget.isExpired || !maneuverTarget.isAlive)) {
            return true
        }

        // Arrived at location.
        if (targetLocation != null && (ship.location - targetLocation).length() <= arrivedAtLocationRadius) {
            return true
        }

        return false
    }

    /** Select which enemy ship to attack. This may be different
     * from the maneuver target provided by the ShipAI. */
    private fun updateAttackTarget() {
        // Attack target is stored in a flag, so it carries over between Maneuver instances.
        var currentTarget: ShipAPI? = ship.AITStash.attackTarget

        val updateTarget = when {
            currentTarget == null -> true
            !currentTarget.isValidTarget -> true
            // Do not interrupt bursts.
            primaryWeapons().firstOrNull { it.trueIsInBurst } != null -> false
            isOutOfRange(currentTarget.location, range + currentTarget.collisionRadius) -> true
            else -> false
        }

        if (updateTarget) currentTarget = findNewTarget() ?: maneuverTarget

        ship.AITStash.attackTarget = currentTarget
        attackTarget = currentTarget
    }

    /** Decide if ships needs to back off due to high flux level */
    // TODO shieldless ships
    private fun updateBackoffStatus() {
        isBackingOff = ship.aiFlags.hasFlag(BACKING_OFF)
        val fluxLevel = ship.fluxTracker.fluxLevel

        if ((isBackingOff && fluxLevel > backoffLowerThreshold) || fluxLevel > backoffUpperThreshold) {
            ship.aiFlags.setFlag(BACKING_OFF)
        } else if (isBackingOff) {
            ship.aiFlags.unsetFlag(BACKING_OFF)
            isBackingOff = false
        }
    }

    private fun updateIdleTime(dt: Float) {
        val shieldIsUp = ship.shield?.isOn == true && shieldUptime(ship.shield) > shieldFlickerThreshold
        val isFiring = ship.allWeapons.firstOrNull { it.isFiring } != null

        idleTime = if (shieldIsUp || isFiring) 0f
        else idleTime + dt
    }

    /** Force vent when the ship is backing off,
     * not shooting and with shields down. */
    private fun ventIfNeeded() {
        when {
            !isBackingOff -> return
            idleTime < shieldDownVentTime -> return
            ship.fluxTracker.isVenting -> return
            ship.allWeapons.firstOrNull { it.autofireAI?.shouldFire() == true } != null -> return
        }

        ship.giveCommand(ShipCommand.VENT_FLUX, null, 0)
    }

    private fun manageMobilitySystems() {
        when (systemAIType) {
            MANEUVERING_JETS -> {
                // Use MANEUVERING_JETS to back off, if possible. Vanilla AI
                // does this already, but is not determined enough.
                if (isBackingOff && ship.canUseSystemThisFrame()) {
                    ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0)
                }
            }

            BURN_DRIVE -> {
                // Prevent vanilla AI from jumping closer to target with
                // BURN_DRIVE, if the target is already within weapons range.
                if (attackTarget != null) {
                    val range = range + attackTarget!!.collisionRadius * 0.5f
                    if (!isOutOfRange(attackTarget!!.location, range)) {
                        ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM)
                    }
                }
            }

            else -> Unit
        }
    }

    private fun setFacing() {
        this.aimPoint = null

        val (aimPoint, aimPointVelocity) = when {
            attackTarget != null -> {
                val target = attackTarget!!
                val farOutOfRange = isOutOfRange(target.location, range + target.collisionRadius + 1000f)

                when {
                    farOutOfRange && isBackingOff -> {
                        // Face threat direction when backing off.
                        Pair(ship.location + threatVector, Vector2f())
                    }

                    farOutOfRange -> {
                        // Face heading direction when chasing target.
                        Pair(ship.location + unitVector(desiredHeading) * 1000f, Vector2f())
                    }

                    else -> {
                        // Average aim offset to avoid ship wobbling.
                        val aimPointThisFrame = calculateOffsetAimPoint(target)
                        val aimOffsetThisFrame = getShortestRotation(target.location, ship.location, aimPointThisFrame)
                        val aimOffset = averageOffset.update(aimOffsetThisFrame)
                        val aimPoint = target.location.rotatedAroundPivot(Rotation(aimOffset), ship.location)

                        Pair(aimPoint, target.velocity)
                    }
                }
            }

            targetLocation != null -> {
                // Move to location, if no attack target.
                Pair(targetLocation, Vector2f())
            }

            // Nothing to do.
            else -> return
        }

        this.aimPoint = aimPoint
        desiredFacing = engineController.facing(ship, aimPoint, aimPointVelocity)
    }

    private fun setHeading() {
        val (p, v) = when {
            isBackingOff -> {
                // Move opposite to threat direction.
                val backoffLocation = ship.location - threatVector.resized(1000f)
                Pair(backoffLocation, Vector2f())
            }

            targetLocation != null -> {
                // Move directly to ordered location.
                Pair(targetLocation, Vector2f())
            }

            maneuverTarget != null -> {
                // TODO will need syncing when interval tracking is introduced.
                val angleFromTargetToThreat = abs(getShortestRotation(maneuverTarget.location - ship.location, threatVector))
                val offset = if (angleFromTargetToThreat > 1f) threatVector
                else threatVector.rotated(strafeRotation)

                // Orbit target at max weapon range. Rotate away from threat,
                // or just strafe randomly if no threat.
                val strafeLocation = maneuverTarget.location - offset.resized(range)
                Pair(strafeLocation, attackTarget?.velocity ?: maneuverTarget.velocity)
            }

            // Nothing to do, let the ship coast.
            else -> return
        }

        desiredHeading = engineController.heading(ship, p, v)
    }

    private fun isOutOfRange(target: ShipAPI, range: Float) = isOutOfRange(target.location, range)

    private fun isOutOfRange(p: Vector2f, range: Float): Boolean {
        return (p - ship.location).lengthSquared() > range * range
    }

    private fun findNewTarget(): ShipAPI? {
        val radius = range * 2f
        val ships = shipGrid().getCheckIterator(ship.location, radius, radius).asSequence().filterIsInstance<ShipAPI>()
        val threats = ships.filter { it.owner != ship.owner && it.isValidTarget && it.isShip && !isOutOfRange(it, range) }

        return threats.maxByOrNull { -abs(ship.angleFromFacing(it.location)) }
    }

    private fun primaryWeapons(): List<WeaponAPI> {
        return ship.allWeapons.filter {
            when {
                it.type == MISSILE -> false
                !it.frontFacing -> false
                it.isPD -> false
                it.isPermanentlyDisabled -> false
                else -> true
            }
        }
    }

    /** Range at which all front facing non-PD weapons can hit. */
    private fun range(weapons: List<WeaponAPI>): Float {
        return weapons.maxOfOrNull { -it.range - it.slot.location.x }?.let { -it } ?: 0f
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

    private fun calculateThreatDirection(location: Vector2f): Vector2f {
        val radius = max(threatEvalRadius, this.range)
        val ships = shipsInRadius(location, radius)
        val threats = ships.filter { it.owner != ship.owner && it.isValidTarget && it.isShip }

        val threatSum = threats.fold(Vector2f()) { sum, it ->
            val dp = it.deploymentPoints
            val dir = (it.location - ship.location).resized(1f)
            sum + dir * dp * dp
        }

        return threatSum
    }

    private fun shipsInRadius(location: Vector2f, radius: Float): Sequence<ShipAPI> {
        val r = radius * 2f
        return shipGrid().getCheckIterator(location, r, r).asSequence().filterIsInstance<ShipAPI>()
    }
}
