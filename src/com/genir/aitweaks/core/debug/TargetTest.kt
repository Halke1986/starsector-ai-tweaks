package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.genir.aitweaks.core.features.shipai.BasicEngineController
import com.genir.aitweaks.core.utils.distanceToOrigin
import com.genir.aitweaks.core.utils.extensions.*
import com.genir.aitweaks.core.utils.log
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

var projectiles: MutableSet<DamagingProjectileAPI> = mutableSetOf()

var targetV = Vector2f()

fun targetTest(dt: Float) {
    removeAsteroids()

    val engine = Global.getCombatEngine()
    val ship: ShipAPI = engine.playerShip ?: return
    val target: ShipAPI = engine.ships.firstOrNull { it.owner == 1 } ?: return

    if (!ship.isUnderManualControl) {
        installAI(ship) { ShipAI(ship, target) }
    }

    installAI(target) { TargetAI(target) }

    engine.projectiles.forEach { proj ->
        if (!projectiles.contains(proj)) {
            projectiles.add(proj)

            val intercept = ship.allGroupedWeapons.first().customAI!!.plotIntercept(target)

            val vt = proj.velocity - targetV
            val pt = proj.location - target.location
            val errt = distanceToOrigin(pt, vt)!!

            val vi = proj.velocity
            val pi = proj.location - intercept
            val erri = distanceToOrigin(pi, vi)!!

//            debugPrint["intercept"] = "i $erri"
            debugPrint["target"] = "t $errt"

//            debugPrint["spec"] = "spec ${ship.allGroupedWeapons.first().projectileSpeed}"
//            debugPrint["vel"] = "vel ${proj.velocity.length}"
//            debugPrint["loc"] = "loc ${ship.allGroupedWeapons.first().location}"

//            log("$erri $errt ${target.location} ${targetV} ${proj.location} ${proj.velocity} $intercept")
            log("$erri")

//            drawCircle(target.location, erri * 2f, Color.GREEN)
        }
    }
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

        val err = MathUtils.getShortestRotation(ship.facing, (target.location - ship.location).facing)
        debugPrint["e"] = "e ${abs(err)}"
        log(err)

        controller.heading(dt, location)
        controller.facing(dt, target.location)
//        ship.command(ShipCommand.TURN_RIGHT)
//        ship.command(ShipCommand.TURN_LEFT)

        drawEngineLines(ship)
    }
}

class TargetAI(val ship: ShipAPI) : BaseEngineControllerAI() {
    private val controller: BasicEngineController = BasicEngineController(ship)

    val x = 3000f
    private val y = 0f

    val location = Vector2f(x, y)

    override fun advance(dt: Float) {
        ship.weaponGroupsCopy.forEach { it.toggleOff() }

        if ((ship.location - location).length < 10f) {
            log("--------------------")
            location.x = -location.x
        }

        controller.heading(dt, location)
        controller.facing(dt, location)
    }
}
