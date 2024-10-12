package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.features.shipai.BasicEngineController
import com.genir.aitweaks.core.utils.extensions.facing
import com.genir.aitweaks.core.utils.extensions.isUnderManualControl
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.log
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils.getFacingStrict
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.abs

fun targetTest(dt: Float) {
    removeAsteroids()

    val ship: ShipAPI = Global.getCombatEngine().playerShip ?: return
    val target: ShipAPI = Global.getCombatEngine().ships.firstOrNull { it.owner == 1 } ?: return

    if (!ship.isUnderManualControl) {
        installAI(ship) { ShipAI(ship, target) }
    }
    installAI(target) { TargetAI(target) }
}

class ShipAI(val ship: ShipAPI, val target: ShipAPI) : BaseEngineControllerAI() {
    private val controller: BasicEngineController = BasicEngineController(ship)

    val location = Vector2f(0f, -500f)

    override fun advance(dt: Float) {
        drawCircle(ship.location, ship.collisionRadius / 2f, Color.GREEN)
        ship.weaponGroupsCopy.forEach { it.toggleOff() }

        val err = MathUtils.getShortestRotation(ship.facing, getFacingStrict(target.location - ship.location))

        debugPrint["e"] = "e ${abs(err)}"

        log(err)

        controller.heading(dt, location)
        controller.facing(dt, (target.location - ship.location).facing)
    }
}

class TargetAI(val ship: ShipAPI) : BaseEngineControllerAI() {
    private val controller: BasicEngineController = BasicEngineController(ship)

    val x = 1500f
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
