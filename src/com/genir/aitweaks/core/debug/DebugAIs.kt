package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle
import com.genir.aitweaks.core.shipai.autofire.ballistics.SimulateMissile
import com.genir.aitweaks.core.shipai.movement.BasicEngineController
import com.genir.aitweaks.core.shipai.movement.Movement.Companion.movement
import com.genir.aitweaks.core.utils.angularVelocity
import com.genir.aitweaks.core.utils.mousePosition
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection
import com.genir.aitweaks.core.utils.types.RotationMatrix
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotated
import com.genir.starfarer.combat.entities.Ship
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

var expectedFacing = 90f.toDirection
const val df = -1f * 60f

class ControllerAI(val ship: ShipAPI) : BaseEngineControllerAI() {
    private val controller = BasicEngineController(ship.movement)
    private val RAD: Float = 300f;

    override fun advance(dt: Float) {
        val dir: Vector2f = mousePosition() - ship.location

        Debug.drawCircle(ship.location, RAD, Color.cyan)
        Debug.drawVector(ship.location, dir, Color.green)
        Debug.drawEngineLines(ship)

        val throttle: Float = (dir.length / RAD)//.coerceIn(0f, 1f)
        val vel: Vector2f = dir.facing.unitVector * throttle * ship.movement.maxSpeed

        controller.clearCommands()

        controller.facing(dt, dir.facing, 0f)
        controller.heading(dt, ship.location, vel)

        controller.executeCommands()
    }
}

class MirrorTargetAI(val ship: ShipAPI, val target: ShipAPI) : BaseEngineControllerAI() {
    private val controller = BasicEngineController(ship.movement)
    private val offset = Vector2f(200f, 200f)

    override fun advance(dt: Float) {
        controller.clearCommands()

        controller.heading(dt, target.location + offset, target.velocity)
        controller.facing(dt, target.facing.toDirection, target.angularVelocity)

        controller.executeCommands()
    }
}

class OrbitTargetAI(val ship: ShipAPI, val target: ShipAPI, val r: Float) : BaseEngineControllerAI() {
    private val controller = BasicEngineController(ship.movement)

    override fun advance(dt: Float) {
        val toTarget = target.location - ship.location

        val v = toTarget.rotated(RotationMatrix(90f)).resized(ship.baseMaxSpeed)

        controller.heading(dt, toTarget.resized(-r) + target.location, v + target.velocity)
        controller.facing(dt, toTarget.facing, false)
    }
}

class RotateEngineControllerAI(val ship: ShipAPI) : BaseEngineControllerAI() {
    private val controller = BasicEngineController(ship.movement)

    override fun advance(dt: Float) {
        expectedFacing += df.toDirection * dt

        Debug.drawLine(ship.location, ship.location + expectedFacing.unitVector * 400f, Color.GREEN)
        Debug.drawLine(ship.location, ship.location + ship.facing.toDirection.unitVector * 400f, Color.BLUE)
        Debug.print["f"] = (expectedFacing - ship.facing.toDirection).length

        controller.facing(dt, expectedFacing, false)
    }
}

class FollowMouseAI(val ship: ShipAPI) : BaseEngineControllerAI() {
    private val controller = BasicEngineController(ship.movement)
    private val prevP: Vector2f = Vector2f()

    override fun advance(dt: Float) {
        val p: Vector2f = mousePosition()
        val v: Vector2f = (p - prevP) / dt

        val toP = (p - ship.location)
        if (toP.length < ship.collisionRadius / 2f) {
            controller.facing(dt, ship.facing.toDirection, 0f)
        } else {
            val w: Float = angularVelocity(toP, v - ship.velocity)
            controller.facing(dt, toP.facing, w)
        }

        controller.heading(dt, p, v)

        Debug.drawEngineLines(ship)

        prevP.set(p)
    }
}

var trail: Sequence<SimulateMissile.Frame>? = null

fun debugMissilePath(dt: Float) {
    val ship = Global.getCombatEngine().playerShip ?: return
    val weapon = ship.allWeapons.firstOrNull()?.handle ?: return

    if (trail != null) {
        var prev = trail!!.firstOrNull()!!.location
        trail?.forEach { frame ->
            Debug.drawLine(prev, frame.location, Color.BLUE)
            prev = frame.location
        }
    }

    if (Global.getCombatEngine().missiles.isNotEmpty()) return

    trail = SimulateMissile.missilePath(weapon)
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
        a.velocity.set(a.velocity.resized(1200f))
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
    if (((ship.ai as? Ship.ShipAIWrapper)?.ai !is T)) {
        ship.shipAI = aiFactory()
    }
}
