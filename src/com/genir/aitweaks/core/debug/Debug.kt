package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.combat.entities.Ship.ShipAIWrapper
import com.genir.aitweaks.core.features.shipai.BasicEngineController
import com.genir.aitweaks.core.features.shipai.autofire.SimulateMissile
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.facing
import com.genir.aitweaks.core.utils.extensions.isUnderManualControl
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.rotated
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color.BLUE
import java.awt.Color.GREEN
import kotlin.math.abs

/**
 *
 * FRAME UPDATE ORDER
 *
 * ship movement
 * AI
 * ship advance:
 *   engine controller process commands
 *   weapons:
 *      fire projectile
 *      update aim
 * (ship movement, AI, ship advance LOOP for fast time ships)
 *
 * EFSs
 *
 */

internal fun debug(dt: Float) {
//    targetTest(dt)
    return

//    val ships = Global.getCombatEngine().ships
    val ship = Global.getCombatEngine().playerShip ?: return

//    FollowMouseAI.install(ship)


//    makeDroneFormation()

    if (!ship.isUnderManualControl && ((ship.ai as? ShipAIWrapper)?.ai !is RotateEngineControllerAI)) {
        ship.shipAI = RotateEngineControllerAI(ship)
    }
    if (ship.isUnderManualControl) {
        expectedFacing += df * dt
        drawLine(ship.location, ship.location + unitVector(expectedFacing) * 400f, GREEN)
    }



    log("EFS ${ship.facing} ${expectedFacing} ${abs(MathUtils.getShortestRotation(ship.facing, expectedFacing))}")
    log("------------")
//    drawLine(ship.location, ship.location + unitVector(ship.facing) * 400f, RED)
//    debugPrint["err"] = abs(err)
//    drawEngineLines(ship)
}

var expectedFacing = 90f
const val df = -1f * 60f

class RotateEngineControllerAI(val ship: ShipAPI) : BaseEngineControllerAI() {
    private val controller: BasicEngineController = BasicEngineController(ship)

    override fun advance(dt: Float) {
        expectedFacing += df * dt

        drawLine(ship.location, ship.location + unitVector(expectedFacing) * 400f, GREEN)
        drawLine(ship.location, ship.location + unitVector(ship.facing) * 400f, BLUE)
        debugPrint["f"] = abs(MathUtils.getShortestRotation(ship.facing, expectedFacing))

        controller.facing(dt, expectedFacing)
    }
}

class FollowMouseAI(val ship: ShipAPI) : BaseEngineControllerAI() {
    private val controller: BasicEngineController = BasicEngineController(ship)

    override fun advance(dt: Float) {
        val toMouse = mousePosition() - ship.location

        val facing = if (toMouse.length > ship.collisionRadius / 2f) toMouse.facing
        else controller.rotationStop

        controller.facing(dt, facing)
        controller.heading(dt, mousePosition())

        drawEngineLines(ship)
    }

    companion object {
        fun install(ship: ShipAPI) {
            if (((ship.ai as? ShipAIWrapper)?.ai !is FollowMouseAI)) {
                ship.shipAI = FollowMouseAI(ship)
            }
        }
    }
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

class DroneFormationAI(private val drone: ShipAPI, val ship: ShipAPI, private val offset: Vector2f) : BaseEngineControllerAI() {
    private val controller: BasicEngineController = BasicEngineController(drone)

    override fun advance(dt: Float) {
        val currentOffset = offset.rotated(Rotation(ship.facing))

        controller.heading(dt, ship.location + currentOffset)
        controller.facing(dt, currentOffset.facing)
    }
}

private fun makeDroneFormation() {
    if (Global.getCombatEngine().getTotalElapsedTime(false) < 8f) return

    val ship = Global.getCombatEngine().playerShip ?: return
    val drones = Global.getCombatEngine().ships.filter { it.isFighter }

    val angle = 360f / drones.size

    for (i in drones.indices) {
        val drone = drones[i]

        if (((drone.ai as? ShipAIWrapper)?.ai !is DroneFormationAI)) {
            val offset = Rotation(angle * i).rotate(Vector2f(0f, 300f))
            drone.shipAI = DroneFormationAI(drone, ship, offset)
        }

        drawEngineLines(drone)
    }
}

fun removeAsteroids() {
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

abstract class BaseEngineControllerAI : ShipAIPlugin {
    override fun setDoNotFireDelay(amount: Float) = Unit

    override fun forceCircumstanceEvaluation() = Unit

    override fun needsRefit(): Boolean = false

    override fun getAIFlags(): ShipwideAIFlags = ShipwideAIFlags()

    override fun cancelCurrentManeuver() = Unit

    override fun getConfig(): ShipAIConfig = ShipAIConfig()
}

inline fun <reified T : ShipAIPlugin> installAI(ship: ShipAPI, aiFactory: () -> T) {
    if (((ship.ai as? ShipAIWrapper)?.ai !is T)) {
        ship.shipAI = aiFactory()
    }
}
