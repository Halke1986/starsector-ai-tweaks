package com.genir.aitweaks.core.features.lidar

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.features.autofire.AutofireAI
import com.genir.aitweaks.core.features.shipai.EngineController
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.deploymentPoints
import com.genir.aitweaks.core.utils.extensions.isShip
import com.genir.aitweaks.core.utils.extensions.resized
import com.genir.aitweaks.core.utils.extensions.rotated
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

/** LidarShipAI replaces vanilla AI only when the lidar array is active. */
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

            offset = unitVector(angle).resized(range * 0.92f)
        }

        val c = EngineController(ship)
        c.facing(getAimPoint(), target.velocity)
        c.heading(target.location + offset, target.velocity)
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
        val pos = ((enemy as ShipAPI).location.rotated(r) - ship.location)
        val dp = enemy.deploymentPoints
        sum + (dp * dp * pos.length() * pos.y) / 1e6f
    }
}