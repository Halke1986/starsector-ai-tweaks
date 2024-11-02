package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.features.shipai.BasicEngineController
import com.genir.aitweaks.core.features.shipai.WrapperShipAI
import com.genir.aitweaks.core.features.shipai.command
import com.genir.aitweaks.core.utils.div
import com.genir.aitweaks.core.utils.extensions.*
import com.genir.aitweaks.core.utils.log
import com.genir.aitweaks.core.utils.quad
import com.genir.aitweaks.core.utils.times
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max
import kotlin.math.min

var targetV = Vector2f()

fun targetTest(dt: Float) {
    removeAsteroids()

    val engine = Global.getCombatEngine()
    val ship: ShipAPI = engine.playerShip ?: return
    val target: ShipAPI = engine.ships.firstOrNull { it.owner == 1 } ?: return

    if (!ship.isUnderManualControl) {
        installAI(ship) { ShipAI(ship, target) }
    }

    val weapon = ship.allGroupedWeapons.firstOrNull() ?: return

//    val err = shortestRotation(weapon.currAngle, expectedRelativeFacing)
//    debugPrint["err"] = ("${abs(err)}")
//    log("$err")

    installAI(target) { TargetAI(target) }
}

class ShipAI(val ship: ShipAPI, val target: ShipAPI) : BaseEngineControllerAI() {
    private val controller: BasicEngineController = BasicEngineController(ship)
    private val flags = ShipwideAIFlags()

    val location = Vector2f(0f, -1000f)

    override fun getAIFlags(): ShipwideAIFlags = flags

    override fun advance(dt: Float) {
//        drawCircle(ship.location, ship.collisionRadius / 2f, Color.GREEN)
        ship.weaponGroupsCopy.forEach { it.toggleOn() }
        flags.advance(dt)
//        ship.shipTarget = target
        aiFlags.setFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET, 1f, target)

        targetV = target.velocity.copy


        controller.heading(dt, location)
//        controller.facing(dt, 90f)

//        controller.facing(dt, aimShip(ship, target))

        val weapon = ship.allGroupedWeapons.firstOrNull() ?: return
//        val intercept = weapon.customAI!!.plotIntercept(target)

//        val err = shortestRotation(weapon.currAngle, (intercept - weapon.location).facing)
//        debugPrint["e"] = "e ${abs(err)}"
//        log(err)

        ship.command(ShipCommand.TURN_RIGHT)
//        ship.command(ShipCommand.TURN_LEFT)

        drawEngineLines(ship)
    }
}

class TargetAI(val ship: ShipAPI) : BaseEngineControllerAI() {
    private val controller: BasicEngineController = BasicEngineController(ship)

    val x = 2000f
    private val y = 0f

    val location = Vector2f(x, y)

    override fun advance(dt: Float) {
        ship.weaponGroupsCopy.forEach { it.toggleOff() }

        if ((ship.location - location).length < 10f) {
            log("--------------------")
            location.x = -location.x
        }

        controller.heading(dt, location)
        controller.facing(dt, (location - ship.location).facing)
    }
}

private fun aimShip(ship: ShipAPI, target: CombatEntityAPI): Float {
    val weapon = ship.allGroupedWeapons.first { WrapperShipAI.shouldAim(it) }

    val p = (target.location - weapon.ship.location)
    val v = (target.velocity - weapon.ship.velocity) / (weapon.projectileSpeed)
    val h = weapon.slot.location.x + weapon.barrelOffset

    // (px + t vx)² + (py + t vy)² = (h + t)²
    // (px² + 2 t px vx + t² vx²) + (py² + 2 t py vy + t² vy²) = (h² + 2 t h + t²)
    // 0 = t² (vx² + vy² - 1) + t 2 (px vx + py vy - h) + (px² + py² - h²)

    val a = (v.x * v.x) + (v.y * v.y) - 1f
    val b = (p.x * v.x) + (p.y * v.y) - h
    val c = (p.x * p.x) + (p.y * p.y) - h * h

    val t = quad(a, b + b, c)?.smallerPositive ?: 0f
    val intercept = target.location + v * t

    return (intercept - ship.location).facing
}

val Pair<Float, Float>.smallerPositive: Float?
    get() = when {
        first >= 0 && second >= 0 -> min(first, second)
        first <= 0 != second <= 0 -> max(first, second)
        else -> null
    }

