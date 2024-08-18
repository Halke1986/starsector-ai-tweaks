package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.core.features.shipai.EngineController
import com.genir.aitweaks.core.features.shipai.command
import com.genir.aitweaks.core.features.shipai.strafeAcceleration
import com.genir.aitweaks.core.utils.Rotation
import com.genir.aitweaks.core.utils.boundsCollision
import com.genir.aitweaks.core.utils.extensions.facing
import com.genir.aitweaks.core.utils.extensions.hasCustomShipAI
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.resized
import com.genir.aitweaks.core.utils.times
import com.genir.aitweaks.core.utils.unitVector
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color.*
import kotlin.math.abs
import kotlin.math.ceil

internal fun debug(dt: Float) {
//    followMouse2(dt)


//    val ships = Global.getCombatEngine().ships
//    val custom = ships.mapNotNull { it.customAI }

//    val ship = Global.getCombatEngine().playerShip ?: return

//    drawLine(ship.location, ship.location + unitVector(ship.facing) * 600f, Color.GREEN)
//    drawLine(ship.location, ship.attackTarget?.location ?: ship.location, Color.RED)

//    ship.allWeapons.forEach{it.autofirePlugin?.forceOff()}

//    custom.forEach {
//        drawLine(it.ship.location, it.movement.headingPoint ?: it.ship.location, YELLOW)
//        drawLine(it.ship.location, it.ship.location + it.ship.velocity * 2f, GREEN)
//    }

//    followRotation(dt)
//    makeDroneFormation()

//    showBoundsCollision()
}

//private fun brakeDist(dt: Float, velocity: Float, deceleration: Float): Float {
//    val v = velocity * dt
//    val a = deceleration * dt * dt
//
//    val t = ceil(v / a) - 1f
//
//    debugPrint["t"] = "t $t $v $a"
//    debugPrint["k"] = "$velocity $deceleration $dt"
//
//    debugPrint["z"] = "${v * (t + 1f)} ${(-a * (t + (t * t)) / 2f)}"
//
//    return (v * (t + 1f) - a * (t + t * t) / 2f)
//}

private fun brakeDist(dt: Float, velocity: Float, deceleration: Float): Float {
    val v = velocity * dt
    val a = deceleration * dt * dt

    val t = ceil(v / a)
    return (v + a) * t * 0.5f
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

class DebugAI(val ship: ShipAPI) : ShipAIPlugin {
    private val engineController: EngineController = EngineController(ship)

    override fun advance(dt: Float) {
        drawTurnLines(ship)
        val expectedFacing = ship.location + unitVector(facing) * 600f

        drawLine(ship.location, expectedFacing, GREEN)
        drawLine(ship.location, ship.location + unitVector(ship.facing) * 600f, BLUE)

        engineController.facing(facing)
    }

    override fun setDoNotFireDelay(amount: Float) = Unit

    override fun forceCircumstanceEvaluation() = Unit

    override fun needsRefit(): Boolean = false

    override fun getAIFlags(): ShipwideAIFlags = ShipwideAIFlags()

    override fun cancelCurrentManeuver() = Unit

    override fun getConfig(): ShipAIConfig = ShipAIConfig()
}

private fun makeDroneFormation() {
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

        c.heading(position)
        c.facing(offset.facing)

        drawEngineLines(drone)
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

private fun followMouse2(dt: Float) {
    val ship = Global.getCombatEngine().playerShip ?: return

    val position = Vector2f(
        Global.getCombatEngine().viewport.convertScreenXToWorldX(Global.getSettings().mouseX.toFloat()),
        Global.getCombatEngine().viewport.convertScreenYToWorldY(Global.getSettings().mouseY.toFloat()),
    )

    val d = abs(ship.location.x - position.x) - ship.velocity.length * dt
    val a = ship.strafeAcceleration
    val b = brakeDist(dt, ship.velocity.length, a)

//    debugPrint["d"] = "d $d"
//    debugPrint["b"] = "b $b"

    when {
        ship.location.x < position.x -> {
            when {
                ship.velocity.x < 0 -> ship.command(ShipCommand.STRAFE_RIGHT)
                b > d -> ship.command(ShipCommand.STRAFE_LEFT)
                b < d -> ship.command(ShipCommand.STRAFE_RIGHT)
            }
        }

        ship.location.x > position.x -> {
            when {
                ship.velocity.x > 0 -> ship.command(ShipCommand.STRAFE_LEFT)
                b < d -> ship.command(ShipCommand.STRAFE_LEFT)
                b > d -> ship.command(ShipCommand.STRAFE_RIGHT)
            }
        }
    }

    drawEngineLines(ship)
}

//private fun followMouse() {
//    val ship = Global.getCombatEngine().playerShip ?: return
//
//    val position = Vector2f(
//        Global.getCombatEngine().viewport.convertScreenXToWorldX(Global.getSettings().mouseX.toFloat()),
//        Global.getCombatEngine().viewport.convertScreenYToWorldY(Global.getSettings().mouseY.toFloat()),
//    )
//
//    val c = EngineController(ship)
//    c.heading(position, Vector2f())
//    c.facing(position, Vector2f())
//
//    drawEngineLines(ship)
//}

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
