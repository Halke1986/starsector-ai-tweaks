package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.features.shipai.ai.EngineController
import com.genir.aitweaks.core.utils.Rotation
import com.genir.aitweaks.core.utils.div
import com.genir.aitweaks.core.utils.extensions.hasCustomAI
import com.genir.aitweaks.core.utils.times
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

internal fun debug(dt: Float) {
    val ships = Global.getCombatEngine().ships
    val custom = ships.filter { it.hasCustomAI }

//    val p = Global.getCombatEngine().playerShip ?: return

    val c = custom.firstOrNull() ?: return

    c.weaponGroupsCopy.forEach {
        debugPrint[it] = "${it.weaponsCopy.size} ${it.isAutofiring}"
    }

//        print[it] = "tgt ${it.customAI} ${it.customAI?.attackTarget?.name}"
//        debug[it] = it.hullSpec.hullId

//        drawLine(it.location, it.customAI?.headingPoint ?: it.location, YELLOW)
//        drawLine(it.location, it.customAI?.attackTarget?.location ?: it.location, RED)
//        drawLine(it.location, it.location + it.velocity * 2f, Color.GREEN)
//        drawCircle(it.location, it.collisionRadius / 2f, RED)
}

private var history: MutableMap<ShipAPI, Pair<Vector2f, Vector2f>> = mutableMapOf()

private fun makeDroneFormation(dt: Float) {
    val ship = Global.getCombatEngine().playerShip ?: return
    val drones = Global.getCombatEngine().ships.filter { it.isFighter }

    val angle = 360f / drones.size

    for (i in drones.indices) {
        val drone = drones[i]

        drone.shipAI = null

        val offset = Rotation(angle * i + ship.facing).rotate(Vector2f(0f, 300f))
        val position = ship.location + offset
        val facing = ship.location + offset * 2f

        val prev = history[drone] ?: Pair(Vector2f(), Vector2f())

//            debugVertices.add(Line(ship.location, ship.location + offset, Color.YELLOW))

        val c = EngineController(drone)
        c.heading(position, (position - prev.first) / dt)
        c.facing(facing, (facing - prev.second) / dt)

        history[drone] = Pair(position, facing)

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

private fun followMouse() {
    val ship = Global.getCombatEngine().playerShip ?: return

    val position = Vector2f(
        Global.getCombatEngine().viewport.convertScreenXToWorldX(Global.getSettings().mouseX.toFloat()),
        Global.getCombatEngine().viewport.convertScreenYToWorldY(Global.getSettings().mouseY.toFloat()),
    )

    val c = EngineController(ship)
    c.heading(position, Vector2f())
    c.facing(position, Vector2f())

    drawEngineLines(ship)
}

private fun followPlayerShip() {
    val target = Global.getCombatEngine().playerShip ?: return
    val ship = Global.getCombatEngine().ships.firstOrNull { it != target } ?: return

    ship.shipAI = null

    val c = EngineController(ship)
    c.heading(target.location + Vector2f(250f, 250f), target.velocity)
    c.facing(target.location, target.velocity)

    drawEngineLines(ship)
}

//fun printCustomManeuverName(ship: ShipAPI) {
//    val c = CustomAIManager().getCustomAIClass()
//    val t = MethodType.methodType(oO0O::class.java)
//    val m = MethodHandles.lookup().findVirtual(c, "getCurrentManeuver", t)
//    val maneuver = m.invoke(ship.ai)
//    debugPlugin[ship] = maneuver?.javaClass?.canonicalName
//}