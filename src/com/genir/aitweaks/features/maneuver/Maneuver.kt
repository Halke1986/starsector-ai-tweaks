package com.genir.aitweaks.features.maneuver

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags.MANEUVER_RANGE_FROM_TARGET
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags.MANEUVER_TARGET
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.MISSILE
import com.genir.aitweaks.asm.combat.ai.AssemblyShipAI
import com.genir.aitweaks.debug.Line
import com.genir.aitweaks.debug.debugVertices
import com.genir.aitweaks.debug.drawEngineLines
import com.genir.aitweaks.features.autofire.AutofireAI
import com.genir.aitweaks.utils.Controller
import com.genir.aitweaks.utils.ai.FlagID
import com.genir.aitweaks.utils.ai.getAITFlag
import com.genir.aitweaks.utils.ai.setAITFlag
import com.genir.aitweaks.utils.div
import com.genir.aitweaks.utils.extensions.*
import com.genir.aitweaks.utils.shipGrid
import com.genir.aitweaks.utils.times
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.resize
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.max

class Maneuver(val ship: ShipAPI, val target: ShipAPI) {
    private val controller = Controller()
    private val shipAI = ship.ai as AssemblyShipAI

    val isDirectControl: Boolean = true
    var desiredHeading: Float = ship.facing
    var desiredFacing: Float = ship.facing

    private var dt: Float = 0f
    private var range: Float = 0f

    fun advance(dt: Float) {
        this.dt = dt

        if (target.isExpired || !target.isAlive) {
            shipAI.cancelCurrentManeuver()
        }

//        debugPlugin[0] = ship.maneuverTarget
//        debugPlugin[1] = targetTracker[ship]

        val weapons = primaryWeapons()
        range = range(weapons)

//        findNewTarget()?.let {
//            debugVertices.add(Line(ship.location, it.location, Color.RED))
//        }
        debugVertices.add(Line(ship.location, ship.location + threatDirection(ship.location), Color.RED))

        val attackTarget = selectTarget()

        shipAI.aiFlags.setFlag(MANEUVER_RANGE_FROM_TARGET, 1f, range)
        shipAI.aiFlags.setFlag(MANEUVER_TARGET, 1f, target)

        // Facing is controlled in advance() instead of doManeuver()
        // because doManeuver() is not called when ShipAI decides
        // to perform avoidance collision. ShipAI collision avoidance
        // overrides only heading control, so we still need to
        // perform facing control.
        val p = aimPoint() ?: attackTarget.location
        controller.facing(ship, p, dt)
        desiredFacing = (p - ship.location).getFacing()
        ship.setAITFlag(FlagID.AIM_POINT, p)

        drawEngineLines(ship)
    }

    fun doManeuver() {
        val threat = threatDirection(ship.location).resize(range)
        val p = target.location - threat

        debugVertices.add(Line(ship.location, p, Color.YELLOW))

        controller.heading(ship, p, target.velocity, dt)
        desiredHeading = (p - ship.location).getFacing()
    }

    private fun selectTarget(): ShipAPI {
        var attackTarget: ShipAPI? = ship.getAITFlag(FlagID.ATTACK_TARGET)

        if (attackTarget == null || !attackTarget.isValidTarget || outOfRange(attackTarget.location, range + target.collisionRadius)) {
            attackTarget = findNewTarget() ?: target
        }

        ship.setAITFlag(FlagID.ATTACK_TARGET, attackTarget)
        return attackTarget
    }

    private fun outOfRange(target: ShipAPI, range: Float) = outOfRange(target.location, range)

    private fun outOfRange(p: Vector2f, range: Float): Boolean {
        return (p - ship.location).length() > range
    }

    private fun findNewTarget(): ShipAPI? {
        val radius = range * 2f
        val ships = shipGrid().getCheckIterator(ship.location, radius, radius).asSequence().filterIsInstance<ShipAPI>()
        val threats = ships.filter { it.owner != ship.owner && it.isValidTarget && it.isShip && !outOfRange(it, range) }

        return threats.maxByOrNull { -ship.absAngleFromFacing(it.location) }
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
    private fun aimPoint(): Vector2f? {
        // Clamp aim point when in pursuit to avoid ship wobbling.
        if (ship.isFullAhead) return null

        val weapons = ship.allWeapons.filter { it.slot.isHardpoint }
        val interceptPoints = weapons.mapNotNull { it.autofireAI?.intercept }

        if (interceptPoints.isEmpty()) return null

        val interceptSum = interceptPoints.fold(Vector2f()) { sum, intercept -> sum + intercept }
        return interceptSum / interceptPoints.size.toFloat()
    }

    private fun threatDirection(location: Vector2f): Vector2f {
        val radius = max(2500f, this.range)
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

val WeaponAPI.autofireAI: AutofireAI?
    get() = this.ship.getWeaponGroupFor(this).getAutofirePlugin(this) as? AutofireAI

val ShipAPI.isFullAhead: Boolean
    get() = this.absAngleFromFacing(this.velocity) <= 1f && this.velocity.length() >= this.maxSpeed * 0.9f