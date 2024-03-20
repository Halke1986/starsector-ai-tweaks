package com.genir.aitweaks.features.lidar

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.debug.debugPlugin
import com.genir.aitweaks.utils.Controller
import com.genir.aitweaks.utils.Rotation
import com.genir.aitweaks.utils.shipGrid
import com.genir.aitweaks.utils.unitVector
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.resize

/** LidarShipAI replaces vanilla BasicShipAI only when the lidar array is active. */
class LidarShipAI(private val ship: ShipAPI, private val target: ShipAPI, private val range: Float) : ShipAIPlugin {
    private val flags = ship.aiFlags
    private val con = Controller()

    override fun advance(dt: Float) {
        con.facing(ship, target.location, dt)

        var angle = (ship.location - target.location).getFacing()
        val gradient = dangerGradientInDirection(ship, ship.facing + 90f)

        when {
            gradient > 30f -> angle += 5f
            gradient < -30f -> angle -= 5f
        }

        val offset = unitVector(angle).resize(range * 0.92f)

        con.heading(ship, target.location + offset, target.velocity, dt)

        debugPlugin[0] = gradient
//        debugPlugin[1] = angle
        debugPlugin[1] = (ship.location - target.location).length()
        debugPlugin[2] = range
    }

    override fun setDoNotFireDelay(amount: Float) = Unit

    override fun forceCircumstanceEvaluation() = Unit

    override fun needsRefit(): Boolean = false

    override fun getAIFlags(): ShipwideAIFlags = flags

    override fun cancelCurrentManeuver() = Unit

    override fun getConfig(): ShipAIConfig = ShipAIConfig()
}

fun dangerGradientInDirection(ship: ShipAPI, facing: Float): Float {
    val ships = shipGrid().getCheckIterator(ship.location, 4000f, 4000f).asSequence()
    val threats = ships.filter { (it as ShipAPI).owner != ship.owner && it.isAlive }

    val r = Rotation(90f - facing)
    return threats.fold(0f) { sum, enemy ->
        val pos = r.rotate((enemy as ShipAPI).location - ship.location);
//        debugPlugin[enemy] = "${enemy.name} ${pos.length()} ${pos.y}"
        val dp = enemy.fleetMember.unmodifiedDeploymentPointsCost
        sum + (dp * dp * pos.length() * pos.y) / 1e6f;
    }
}