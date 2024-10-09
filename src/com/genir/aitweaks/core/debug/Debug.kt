package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.features.shipai.EngineController
import com.genir.aitweaks.core.features.shipai.autofire.SimulateMissile
import com.genir.aitweaks.core.state.combatState
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.attackTarget
import com.genir.aitweaks.core.utils.extensions.facing
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.resized
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color.*

internal fun debug(dt: Float) {
    val ships = Global.getCombatEngine().ships

    ships.forEach {
        debugPrint[it] = "${it.hullSpec.hullId} ${it.attackTarget}"
    }
}

var facingGoal = 90f
const val df = -1f * 60f

class Interpolate(private val ship: ShipAPI) {
    private var prevValue = Vector2f()
    private var value = Vector2f()

    private var timestamp: Int = 0
    private var dtSum: Float = 0f

    fun advance(dt: Float, nextValue: () -> Vector2f): Vector2f {
        val timeMult: Float = ship.mutableStats.timeMult.modifiedValue

        // No need to interpolate for ships in normal time flow.
        if (timeMult == 1f) return nextValue()

        if (combatState.frameCount > timestamp) {
            timestamp = combatState.frameCount
            dtSum = 0f

            prevValue = value
            value = nextValue()
        }

        dtSum += dt

        val delta = (value - prevValue) / timeMult
        return prevValue + delta * (dtSum / Global.getCombatEngine().elapsedInLastFrame)
    }
}

class EngineControllerAI(val ship: ShipAPI) : ShipAIPlugin {
    private val controller: EngineController = EngineController(ship)

    private val interpolateHeading = Interpolate(ship)
    private val interpolateFacing = Interpolate(ship)

    private var heading = Vector2f()
    private var facing = 0f

    override fun advance(dt: Float) {
//        ship.useSystem()

        facing = interpolateFacing.advance(dt) {
//            drawLine(ship.location, ship.location + unitVector(facing) * 600f, GREEN)
//            drawLine(ship.location, ship.location + unitVector(ship.facing) * 600f, YELLOW)
//            drawEngineLines(ship)

//            facingGoal += df * Global.getCombatEngine().elapsedInLastFrame

            val facing = if ((mousePosition() - ship.location).length > 30f) {
                (mousePosition() - ship.location).facing
            } else this.facing

            Vector2f(facing, 0f)
        }.x

        heading = interpolateHeading.advance(dt) {
            mousePosition()
        }

        if (!ship.velocity.isZeroVector())
            controller.heading(dt, controller.allStop)

        controller.facing(dt, facing)
//        controller.heading(dt, heading)
    }

    override fun setDoNotFireDelay(amount: Float) = Unit

    override fun forceCircumstanceEvaluation() = Unit

    override fun needsRefit(): Boolean = false

    override fun getAIFlags(): ShipwideAIFlags = ShipwideAIFlags()

    override fun cancelCurrentManeuver() = Unit

    override fun getConfig(): ShipAIConfig = ShipAIConfig()
}

var trail: Sequence<SimulateMissile.Frame>? = null

fun debugMissilePath(dt: Float) {
    val ship = Global.getCombatEngine().playerShip ?: return
    val weapon = ship.allWeapons.firstOrNull() ?: return

    if (trail != null) {
        var prev = trail!!.firstOrNull()!!.location
        trail?.forEach { frame ->
            drawLine(prev, frame.location, BLUE)
            prev = frame.location
        }
    }

    if (Global.getCombatEngine().missiles.isNotEmpty())
        return

    trail = SimulateMissile.missilePath(weapon)
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

    drawLine(position, dir.resized(dir.length * dist) + position, YELLOW)
}

private fun makeDroneFormation(dt: Float) {
    val ship = Global.getCombatEngine().playerShip ?: return
    val drones = Global.getCombatEngine().ships.filter { it.isFighter }

    val angle = 360f / drones.size

    for (i in drones.indices) {
        val drone = drones[i]

        if (drone.customData["controller"] == null)
            drone.setCustomData("controller", EngineController(drone))
        val c = drone.customData["controller"] as EngineController

        drone.shipAI = null

        val offset = Rotation(angle * i + ship.facing).rotate(Vector2f(0f, 300f))
        val position = ship.location + offset

        c.heading(dt, position)
        c.facing(dt, offset.facing)

//        drawEngineLines(drone)
    }

    drones.forEach { it.shipAI = null }
}

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

private fun followMouse(dt: Float) {
    val engine = Global.getCombatEngine()
    val ship = engine.ships.firstOrNull { it == engine.playerShip } ?: return

    if (ship.customData["controller"] == null)
        ship.setCustomData("controller", EngineController(ship))
    val c = ship.customData["controller"] as EngineController

    val facing = (mousePosition() - ship.location).facing
    c.facing(dt, facing)

    drawLine(ship.location, ship.location + unitVector(facing) * 600f, GREEN)
    drawLine(ship.location, ship.location + unitVector(ship.facing) * 600f, YELLOW)

//    debugPrint["facing"] = facing
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
