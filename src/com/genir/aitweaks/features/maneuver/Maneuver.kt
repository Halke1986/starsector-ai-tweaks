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
import com.genir.aitweaks.utils.ShipSystemAiType.MANEUVERING_JETS
import com.genir.aitweaks.utils.ai.AITFlags
import com.genir.aitweaks.utils.extensions.*
import org.lazywizard.lazylib.MathUtils.getShortestRotation
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.resize
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

class Maneuver(val ship: ShipAPI, val targetShip: ShipAPI?, private val targetLocation: Vector2f?) {
    private val engineController = EngineController()
    private val shipAI = ship.ai as AssemblyShipAI

    val isDirectControl: Boolean = true
    var desiredHeading: Float = ship.facing
    var desiredFacing: Float = ship.facing

    private var dt: Float = 0f
    private var range: Float = 0f
    private var averageOffset = RollingAverageFloat(aimOffsetSamples)
    private var idleTime = 0f

    private var attackTarget: ShipAPI? = targetShip
    private var isBackingOff: Boolean = false

    // Make strafe rotation direction random, but consistent for a given ship.
    private val strafeRotation = Rotation(if (ship.id.hashCode() % 2 == 0) 10f else -10f)
    private val systemAIType = ship.system?.specAPI?.AIType

    private var threatVector = calculateThreatDirection(ship.location)

    fun advance(dt: Float) {
        this.dt = dt

        if (shouldEndManeuver()) {
            shipAI.cancelCurrentManeuver()
        }

        // Update state.
        val weapons = primaryWeapons()
        range = range(weapons)
        updateAttackTarget()
        updateBackoffStatus()
        updateIdleTime(dt)

        ventIfNeeded()
        runIfNeeded()

        debugPlugin["backoff"] = if (isBackingOff) "is backing off" else ""

        ship.aiFlags.setFlag(MANEUVER_RANGE_FROM_TARGET, range)
        ship.aiFlags.setFlag(MANEUVER_TARGET, FLAG_DURATION, targetShip)

        // Facing is controlled in advance() instead of doManeuver()
        // because doManeuver() is not called when ShipAI decides
        // to perform collision avoidance. ShipAI collision avoidance
        // overrides only heading control, so we still need to
        // perform facing control.
        setFacing()

        debugPlugin["override collision"] = ""
        val engineAI = shipAI.engineAI as OverrideEngineAI
        if (isBackingOff && engineAI.AIIsAvoidingCollision) {
            debugPlugin["override collision"] = "override collision ai"
            setHeading()
        }
    }

    fun doManeuver() = setHeading()

    private fun shouldEndManeuver(): Boolean {
        // Target ship was destroyed.
        if (targetShip != null && (targetShip.isExpired || !targetShip.isAlive)) {
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
        var currentTarget: ShipAPI? = ship.AITFlags.attackTarget

        val updateTarget = when {
            currentTarget == null -> true
            !currentTarget.isValidTarget -> true
            // Do not interrupt bursts.
            primaryWeapons().firstOrNull { it.trueIsInBurst } != null -> false
            isOutOfRange(currentTarget.location, range + currentTarget.collisionRadius) -> true
            else -> false
        }

        if (updateTarget) currentTarget = findNewTarget() ?: targetShip

        ship.AITFlags.attackTarget = currentTarget
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

    /** Use maneuvering jets to back off, if possible. */
    private fun runIfNeeded() {
        if (isBackingOff && systemAIType == MANEUVERING_JETS && ship.canUseSystemThisFrame()) {
            ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0)
        }
    }

    private fun setFacing() {
        val (aimPoint, v, offset) = when {
            attackTarget != null -> {
                val target = attackTarget!!
                val farOutOfRange = isOutOfRange(target.location, range + target.collisionRadius + 1000f)

                when {
                    farOutOfRange && isBackingOff -> {
                        // Face threat direction when backing off.
                        Triple(ship.location + threatVector, Vector2f(), 0f)
                    }

                    farOutOfRange -> {
                        // Face heading direction when chasing target.
                        Triple(ship.location + unitVector(desiredHeading) * 1000f, Vector2f(), 0f)
                    }

                    else -> {
                        // Average aim offset to avoid ship wobbling.
                        val offset = averageOffset.update(calculateAimOffset(attackTarget!!))
                        Triple(target.location, target.velocity, offset)
                    }
                }
            }

            targetLocation != null -> {
                // Move to location, if no attack target.
                Triple(targetLocation, Vector2f(), 0f)
            }

            else -> {
                // Nothing to do.
                return
            }
        }

        desiredFacing = engineController.facing(ship, aimPoint, v, dt, offset)
        ship.AITFlags.aimPoint = aimPoint
    }

    private fun setHeading() {
        val (p, v) = when {
            isBackingOff -> {
                // Move opposite to threat direction.
                val backoffLocation = ship.location - threatVector.resize(1000f)
                Pair(backoffLocation, Vector2f())
            }

            targetLocation != null -> {
                // Move directly to ordered location.
                Pair(targetLocation, Vector2f())
            }

            targetShip != null -> {
                val angleToTarget = (targetShip.location - ship.location).getFacing()
                val angleFromTargetToThreat = getShortestRotation(threatVector.getFacing(), angleToTarget)
                val offset = if (angleFromTargetToThreat > 1f) threatVector
                else strafeRotation.rotate(threatVector)

                // Orbit target at max weapon range. Rotate away from threat,
                // or just strafe randomly if no threat.
                val strafeLocation = targetShip.location - offset.resize(range)
                Pair(strafeLocation, attackTarget?.velocity ?: targetShip.velocity)
            }

            else -> {
                // Nothing to do, let the ship coast.
                return
            }
        }

        desiredHeading = engineController.heading(ship, p, v, dt)
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
    private fun calculateAimOffset(attackTarget: ShipAPI): Float {
        // Find intercept points of all hardpoints attacking the current target.
        val hardpoints = ship.allWeapons.filter { it.slot.isHardpoint }.mapNotNull { it.autofireAI }
        val aimedHardpoints = hardpoints.filter { it.targetShip != null && it.targetShip == attackTarget }
        val interceptPoints = aimedHardpoints.mapNotNull { it.intercept }

        if (interceptPoints.isEmpty()) return 0f

        // Average the intercept points. This may cause poor aim if different hardpoints
        // have weapons with significantly different projectile velocities.
        val interceptSum = interceptPoints.fold(Vector2f()) { sum, intercept -> sum + intercept }
        val aimPoint = interceptSum / interceptPoints.size.toFloat()

        return getShortestRotation((attackTarget.location - ship.location).getFacing(), (aimPoint - ship.location).getFacing())
    }

    private fun calculateThreatDirection(location: Vector2f): Vector2f {
        val radius = max(threatEvalRadius, this.range)
        val ships = shipsInRadius(location, radius)
        val threats = ships.filter { it.owner != ship.owner && it.isValidTarget && it.isShip }

        val threatSum = threats.fold(Vector2f()) { sum, it ->
            val dp = it.deploymentPoints
            val dir = (it.location - ship.location).resize(1f)
            sum + dir * dp * dp
        }

        return threatSum
    }

    private fun shipsInRadius(location: Vector2f, radius: Float): Sequence<ShipAPI> {
        val r = radius * 2f
        return shipGrid().getCheckIterator(location, r, r).asSequence().filterIsInstance<ShipAPI>()
    }
}
