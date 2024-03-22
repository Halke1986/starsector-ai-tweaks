package com.genir.aitweaks.features.lidar

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.features.autofire.AutofireAI
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.extensions.isShip
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.resize
import org.lwjgl.util.vector.Vector2f

/** LidarShipAI replaces vanilla BasicShipAI only when the lidar array is active. */
class LidarShipAI(private val ship: ShipAPI, private val target: ShipAPI, private val range: Float) : ShipAIPlugin {
    private val flags = ship.aiFlags
    private var advanceInterval = defaultAIInterval()
    private var offset = ship.location - target.location

    override fun advance(dt: Float) {
        advanceInterval.advance(dt)
        if (advanceInterval.intervalElapsed()) {

            // Try to maintain constant position in relation to the attack target,
            // at 92% weapon range. Orbit around target to avoid threats.
            var angle = (ship.location - target.location).getFacing()
            val gradient = dangerGradientInDirection(ship, ship.facing + 90f)

            when {
                gradient > 30f -> angle += 5f
                gradient < -30f -> angle -= 5f
            }

            offset = unitVector(angle).resize(range * 0.92f)
        }

        val con = Controller()
        con.facing(ship, getAimPoint(), dt)
        con.heading(ship, target.location + offset, target.velocity, dt)
    }

    /** Get average aim point of lidar weapons. */
    private fun getAimPoint(): Vector2f {
        val allAIs = ship.weaponGroupsCopy.flatMap { it.aiPlugins }.filterIsInstance<AutofireAI>()
        val lidarAutofireAIs = allAIs.filter { it.weapon.isLidarWeapon && it.shouldFire() && it.intercept != null }

        return if (lidarAutofireAIs.isEmpty()) target.location
        else lidarAutofireAIs.fold(Vector2f()) { sum, ai -> sum + ai.intercept!! } / lidarAutofireAIs.size.toFloat()
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
    val threats = ships.filter { (it as ShipAPI).owner != ship.owner && it.isAlive && it.isShip }

    val r = Rotation(90f - facing)
    return threats.fold(0f) { sum, enemy ->
        val pos = r.rotate((enemy as ShipAPI).location - ship.location);
        val member = enemy.fleetMember
        val dp = member.unmodifiedDeploymentPointsCost
        sum + (dp * dp * pos.length() * pos.y) / 1e6f;
    }
}