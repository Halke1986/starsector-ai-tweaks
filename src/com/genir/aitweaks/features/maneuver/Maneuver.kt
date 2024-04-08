package com.genir.aitweaks.features.maneuver

import com.fs.starfarer.api.combat.ShipAPI
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
import org.lazywizard.lazylib.ext.getFacing
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

class Maneuver(val ship: ShipAPI, val target: ShipAPI) {
    private val controller = Controller()
    private val shipAI = ship.ai as AssemblyShipAI

    val isDirectControl: Boolean = true
    var desiredHeading: Float = ship.facing
    var desiredFacing: Float = ship.facing

    private var dt: Float = 0f
    private var range: Float = 0f
    private var averageOffset = RollingAverage(aimOffsetSamples)
    private var attackTarget: ShipAPI = target
    private var isBackingOff: Boolean = false

    fun advance(dt: Float) {
        this.dt = dt

        if (target.isExpired || !target.isAlive) {
            shipAI.cancelCurrentManeuver()
        }

        val weapons = primaryWeapons()
        range = range(weapons)
        attackTarget = updateAttackTarget()

        // Update backoff status.
        isBackingOff = shipAI.aiFlags.getCustom(BACKING_OFF) == true
        val fluxLevel = ship.fluxTracker.fluxLevel// ship.fluxTracker.hardFlux / ship.fluxTracker.maxFlux
        if ((isBackingOff && fluxLevel > backoffLowerThreshold) || fluxLevel > backoffUpperThreshold) {
            shipAI.aiFlags.setFlag(BACKING_OFF, 0.1f, true)
        }

        debugPlugin[0] = fluxLevel
        debugPlugin[1] = isBackingOff
        debugPlugin[2] = shipAI.aiFlags.getCustom(BACKING_OFF)

        shipAI.aiFlags.setFlag(MANEUVER_RANGE_FROM_TARGET, range)
        shipAI.aiFlags.setFlag(MANEUVER_TARGET, FLAG_DURATION, target)

        // Facing is controlled in advance() instead of doManeuver()
        // because doManeuver() is not called when ShipAI decides
        // to perform avoidance collision. ShipAI collision avoidance
        // overrides only heading control, so we still need to
        // perform facing control.
        setFacing()
    }

    private fun setFacing() {
        // Average aim offset to avoid ship wobbling.
        val offset = averageOffset.update(aimOffset())
        val aimPoint = attackTarget.location + offset

        controller.facing(ship, aimPoint, dt)
        desiredFacing = (aimPoint - ship.location).getFacing()
        ship.setAITFlag(FlagID.AIM_POINT, aimPoint)

//        debugVertices.add(Line(ship.location, aimPoint, Color.RED))
        drawEngineLines(ship)
//        debugVertices.add(Line(ship.location, ship.location + threatDirection(ship.location), Color.RED))
    }

    fun doManeuver() {
        val threat = threatDirection(ship.location)

        val p = if (isBackingOff) ship.location - threat.resize(1000f)
        else target.location - threat.resize(range)

        debugVertices.add(Line(ship.location, p, Color.YELLOW))

        controller.heading(ship, p, target.velocity, dt)
        desiredHeading = (p - ship.location).getFacing()
    }

    private fun updateAttackTarget(): ShipAPI {
        var attackTarget: ShipAPI? = ship.getAITFlag(FlagID.ATTACK_TARGET)

        if (attackTarget == null || !attackTarget.isValidTarget || isOutOfRange(attackTarget.location, range + target.collisionRadius)) {
            attackTarget = findNewTarget() ?: target
        }

        ship.setAITFlag(FlagID.ATTACK_TARGET, attackTarget)
        return attackTarget
    }

    private fun isOutOfRange(target: ShipAPI, range: Float) = isOutOfRange(target.location, range)

    private fun isOutOfRange(p: Vector2f, range: Float): Boolean {
        return (p - ship.location).length() > range
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
    private fun aimOffset(): Vector2f {
        // Assume autofire weapons are tracking this.attackTarget.
        val weapons = ship.allWeapons.filter { it.slot.isHardpoint }
        val interceptPoints = weapons.mapNotNull { it.autofireAI?.intercept }

        if (interceptPoints.isEmpty()) return Vector2f()

        val interceptSum = interceptPoints.fold(Vector2f()) { sum, intercept -> sum + intercept }
        val aimPoint = interceptSum / interceptPoints.size.toFloat()

        return aimPoint - attackTarget.location
    }

    private fun threatDirection(location: Vector2f): Vector2f {
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

val ShipAPI.isFullAhead: Boolean
    get() = this.angleFromFacing(this.velocity) <= 1f && this.velocity.length() >= this.maxSpeed * 0.9f
