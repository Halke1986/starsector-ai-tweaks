package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle
import com.genir.aitweaks.core.shipai.autofire.SimulateMissile
import com.genir.aitweaks.core.shipai.movement.BasicEngineController
import com.genir.aitweaks.core.shipai.movement.Kinematics.Companion.kinematics
import com.genir.aitweaks.core.utils.angularVelocity
import com.genir.aitweaks.core.utils.mousePosition
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
import com.genir.aitweaks.core.utils.types.RotationMatrix
import com.genir.aitweaks.core.utils.types.RotationMatrix.Companion.rotated
import com.genir.starfarer.combat.entities.Ship
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

var expectedFacing = 90f.direction
const val df = -1f * 60f

class OrbitTargetAI(val ship: ShipAPI, val target: ShipAPI, val r: Float) : BaseEngineControllerAI() {
    private val controller = BasicEngineController(ship.kinematics)

    override fun advance(dt: Float) {
        val toTarget = target.location - ship.location

        val v = toTarget.rotated(RotationMatrix(90f)).resized(ship.baseMaxSpeed)


        controller.heading(dt, toTarget.resized(-r) + target.location, v + target.velocity)
        controller.facing(dt, toTarget.facing, false)
    }
}

class RotateEngineControllerAI(val ship: ShipAPI) : BaseEngineControllerAI() {
    private val controller = BasicEngineController(ship.kinematics)

    override fun advance(dt: Float) {
        expectedFacing += df * dt

        Debug.drawLine(ship.location, ship.location + expectedFacing.unitVector * 400f, Color.GREEN)
        Debug.drawLine(ship.location, ship.location + ship.facing.direction.unitVector * 400f, Color.BLUE)
        Debug.print["f"] = (expectedFacing - ship.facing.direction).length

        controller.facing(dt, expectedFacing, false)
    }
}

class FollowMouseAI(val ship: ShipAPI) : BaseEngineControllerAI() {
    private val controller = BasicEngineController(ship.kinematics)
    private val prevP: Vector2f = Vector2f()

    override fun advance(dt: Float) {
        val p: Vector2f = mousePosition()
        val v: Vector2f = (p - prevP) / dt

        val toP = (p - ship.location)
        if (toP.length < ship.collisionRadius / 2f) {
            controller.facing(dt, ship.facing.direction, 0f)
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
