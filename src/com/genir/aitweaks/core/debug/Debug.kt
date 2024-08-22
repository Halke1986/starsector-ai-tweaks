package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.features.shipai.EngineController
import com.genir.aitweaks.core.utils.boundsCollision
import com.genir.aitweaks.core.utils.extensions.hasBasicShipAI
import com.genir.aitweaks.core.utils.extensions.hasCustomShipAI
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.resized
import com.genir.aitweaks.core.utils.mousePosition
import com.genir.aitweaks.core.utils.times
import com.genir.aitweaks.core.utils.unitVector
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color.*

internal fun debug(dt: Float) {
    highlightCustomAI()
}

private fun showBoundsCollision() {
    val ship = Global.getCombatEngine().playerShip ?: return

    val position = Vector2f(
        Global.getCombatEngine().viewport.convertScreenXToWorldX(Global.getSettings().mouseX.toFloat()),
        Global.getCombatEngine().viewport.convertScreenYToWorldY(Global.getSettings().mouseY.toFloat()),
    )

    drawBounds(ship)

    val dir = ship.location - position

    val dist = boundsCollision(position - ship.location, dir, ship) ?: return

    debugPrint["dist"] = dist

    drawLine(position, dir.resized(dir.length * dist) + position, YELLOW)
}

internal fun highlightCustomAI() {
    Global.getCombatEngine().ships.filter { it.hasCustomShipAI }.forEach {
        drawCircle(it.location, it.collisionRadius / 2f, RED)
    }
}

var facing = 0f
const val df = -0.3f * 60f

private fun followRotation(dt: Float) {
    val ship = Global.getCombatEngine().playerShip ?: return

    facing += df * dt

    if (ship.ai == null) {
        drawLine(ship.location, ship.location + unitVector(facing) * 600f, GREEN)
    }
}

//private fun makeDroneFormation() {
//    val ship = Global.getCombatEngine().playerShip ?: return
//    val drones = Global.getCombatEngine().ships.filter { it.isFighter }
//
//    val angle = 360f / drones.size
//
//    for (i in drones.indices) {
//        val drone = drones[i]
//
//        if (drone.customData["controller"] == null)
//            drone.setCustomData("controller", EngineController(drone))
//        val c = drone.customData["controller"] as EngineController
//
//        drone.shipAI = null
//
//        val offset = Rotation(angle * i + ship.facing).rotate(Vector2f(0f, 300f))
//        val position = ship.location + offset
//
//        c.heading(position)
//        c.facing(offset.facing)
//
//        drawEngineLines(drone)
//    }
//
//    drones.forEach { it.shipAI = null }
//}

private fun removeAsteroids() {
    val engine = Global.getCombatEngine()
    engine.asteroids.forEach {
        engine.removeEntity(it)
    }
}

private fun speedupAsteroids() {
    val asteroids = Global.getCombatEngine().asteroids
    for (i in asteroids.indices) {
        val a = asteroids[i]
        a.mass = 0f
        a.velocity.set(VectorUtils.getDirectionalVector(Vector2f(), a.velocity) * 1200f)
    }
}

private fun followMouse() {
//    val ship = Global.getCombatEngine().playerShip ?: return
    val engine = Global.getCombatEngine()
    val ship = engine.ships.firstOrNull { it != engine.playerShip } ?: return

    if (ship.hasBasicShipAI) ship.shipAI = EngineControllerAI(ship)
//
//    val c = ((ship.shipAI as Ship.ShipAIWrapper).ai as EngineControllerAI).controller
//
//    c.heading(mousePosition())
//
//    drawEngineLines(ship)
}

class EngineControllerAI(val ship: ShipAPI) : ShipAIPlugin {
    private val controller: EngineController = EngineController(ship)

    override fun advance(dt: Float) {
        ship.useSystem()

        controller.heading(dt * ship.mutableStats.timeMult.modifiedValue, mousePosition())
    }

    override fun setDoNotFireDelay(amount: Float) = Unit

    override fun forceCircumstanceEvaluation() = Unit

    override fun needsRefit(): Boolean = false

    override fun getAIFlags(): ShipwideAIFlags = ShipwideAIFlags()

    override fun cancelCurrentManeuver() = Unit

    override fun getConfig(): ShipAIConfig = ShipAIConfig()
}

//private fun followPlayerShip() {
//    val target = Global.getCombatEngine().playerShip ?: return
//    val ship = Global.getCombatEngine().ships.firstOrNull { it != target } ?: return
//
//    ship.shipAI = null
//
//    val c = EngineController(ship)
//    c.heading(target.location + Vector2f(250f, 250f), target.velocity)
//    c.facing(target.location, target.velocity)
//
//    drawEngineLines(ship)
//}
