package com.genir.aitweaks.features.lidar

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.debug.debugPlugin
import com.genir.aitweaks.utils.Controller
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.resize

/** ShipAI replaces vanilla BasicShipAI only when the lidar array is active. */
class ShipAI(private val ship: ShipAPI, private val target: ShipAPI, private val range: Float) : ShipAIPlugin {
    private val flags = ship.aiFlags
    private val offset = (ship.location - target.location).resize(range * 0.92f)
    private val con = Controller()

    override fun advance(dt: Float) {
        con.facing(ship, target.location, dt)
        con.heading(ship, target.location + offset, target.velocity, dt)

        debugPlugin[0] = offset.length()
        debugPlugin[1] = (ship.location - target.location).length()
    }

    override fun setDoNotFireDelay(amount: Float) = Unit

    override fun forceCircumstanceEvaluation() = Unit

    override fun needsRefit(): Boolean = false

    override fun getAIFlags(): ShipwideAIFlags = flags

    override fun cancelCurrentManeuver() = Unit

    override fun getConfig(): ShipAIConfig = ShipAIConfig()
}