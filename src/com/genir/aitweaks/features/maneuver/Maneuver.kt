package com.genir.aitweaks.features.maneuver

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.FLAG_DURATION
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.MISSILE
import com.genir.aitweaks.asm.combat.ai.AssemblyShipAI
import com.genir.aitweaks.debug.Line
import com.genir.aitweaks.debug.debugPlugin
import com.genir.aitweaks.debug.debugVertices
import com.genir.aitweaks.debug.drawEngineLines
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.ai.FlagID
import com.genir.aitweaks.utils.ai.getAITFlag
import com.genir.aitweaks.utils.ai.setAITFlag
import com.genir.aitweaks.utils.extensions.*
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.resize
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.abs
import kotlin.math.max

const val threatEvalRadius = 2500f
const val aimOffsetSamples = 45

const val backoffUpperThreshold = 0.8f
const val backoffLowerThreshold = 0.2f

const val shieldDownVentTime = 2.0f
const val shieldFlickerThreshold = 0.5f

class Maneuver(val ship: ShipAPI, val target: ShipAPI) {
    private val engineController = EngineController()
    private val shipAI = ship.ai as AssemblyShipAI

    val isDirectControl: Boolean = true
    var desiredHeading: Float = ship.facing
    var desiredFacing: Float = ship.facing

    private var dt: Float = 0f
    private var range: Float = 0f
    private var averageOffset = RollingAverage(aimOffsetSamples)
    private var shieldDownTime = 0f

    private var attackTarget: ShipAPI = target
    private var isBackingOff: Boolean = false

    fun advance(dt: Float) {
        this.dt = dt

        if (target.isExpired || !target.isAlive) {
            shipAI.cancelCurrentManeuver()
        }

        // Update state.
        val weapons = primaryWeapons()
        range = range(weapons)
        updateAttackTarget()
        updateBackoffStatus()
        updateShieldDownTime(dt)

        ventIfNeeded()

        debugPlugin["backoff"] = if (isBackingOff) "is backing off" else ""
        debugPlugin["assignment"] = if (shouldMoveToAssignment()) "move to assignment" else ""


        ship.aiFlags.setFlag(MANEUVER_RANGE_FROM_TARGET, range)
        ship.aiFlags.setFlag(MANEUVER_TARGET, FLAG_DURATION, target)

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

    /** Select which enemy ship to attack. This may be different
     * from the maneuver target provided by the ShipAI. */
    private fun updateAttackTarget() {
        // Attack target is stored in a flag, so it carries over between Maneuver instances.
        var currentTarget: ShipAPI? = ship.getAITFlag(FlagID.ATTACK_TARGET)

        val currentTargetIsValid = when {
            currentTarget == null -> false
            !currentTarget.isValidTarget -> false
            isOutOfRange(currentTarget.location, range + currentTarget.collisionRadius) -> false
            else -> true
        }

        if (!currentTargetIsValid) currentTarget = findNewTarget() ?: target

        ship.setAITFlag(FlagID.ATTACK_TARGET, currentTarget)
        attackTarget = currentTarget!!
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

    private fun updateShieldDownTime(dt: Float) {
        val shieldIsUp = ship.shield?.isOn == true && shieldUptime(ship.shield) > shieldFlickerThreshold
        shieldDownTime = if (shieldIsUp) 0f
        else shieldDownTime + dt
    }

    /** Force vent when the ship is backing off,
     * not shooting and with shields down. */
    private fun ventIfNeeded() {
        when {
            !isBackingOff -> return
            shieldDownTime < shieldDownVentTime -> return
            ship.fluxTracker.isVenting -> return
            ship.allWeapons.firstOrNull { it.autofireAI?.shouldFire() == true } != null -> return
        }

        ship.giveCommand(ShipCommand.VENT_FLUX, null, 0)
    }

    private fun setFacing() {
        // Average aim offset to avoid ship wobbling.
        val offset = averageOffset.update(calculateAimOffset())
        val aimPoint = attackTarget.location + offset

        desiredFacing = engineController.facing(ship, aimPoint, dt)
        ship.setAITFlag(FlagID.AIM_POINT, aimPoint)
    }

    private fun setHeading() {
        val threat = calculateThreatDirection(ship.location)

        val (p, v) = if (isBackingOff) {
            // Move opposite to threat direction.
            val backoffLocation = ship.location - threat.resize(1000f)
            Pair(backoffLocation, Vector2f())
        } else if (shouldMoveToAssignment()) {
            val assignment = ship.assignment!!.target
            Pair(assignment.location, assignment.velocity)
        } else {
            // Orbit target at max weapon range, while rotating away from threat.
            val strafeLocation = target.location - threat.resize(range)
            Pair(strafeLocation, attackTarget.velocity)
        }

//        debugVertices.add(Line(ship.location, p, Color.YELLOW))
        debugVertices.add(Line(ship.location, ship.location + unitVector(desiredHeading) * 300f, Color.GREEN))
        drawEngineLines(ship)

        desiredHeading = engineController.heading(ship, p, v, dt)
    }

    private fun shouldMoveToAssignment(): Boolean {
        val target = ship.assignment?.target ?: return false
        return (target.location - ship.location).length() > range
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

    private fun dps(weapons: List<WeaponAPI>): Float {
        return weapons.sumOf { it.derivedStats.dps.toDouble() }.toFloat()
    }

    /** Aim hardpoint weapons with entire ship, if possible. */
    private fun calculateAimOffset(): Vector2f {
        // Assume autofire weapons are tracking this.attackTarget.
        val weapons = ship.allWeapons.filter { it.slot.isHardpoint }
        val interceptPoints = weapons.mapNotNull { it.autofireAI?.intercept }

        if (interceptPoints.isEmpty()) return Vector2f()

        val interceptSum = interceptPoints.fold(Vector2f()) { sum, intercept -> sum + intercept }
        val aimPoint = interceptSum / interceptPoints.size.toFloat()

        return aimPoint - attackTarget.location
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
