package com.genir.aitweaks.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.CombatFleetManager.O0
import com.fs.starfarer.combat.tasks.CombatTaskManager
import com.genir.aitweaks.utils.Controller2
import com.genir.aitweaks.utils.Rotation
import com.genir.aitweaks.utils.times
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

const val ID = "com.genir.aitweaks.debug.DebugPlugin"

var debugPlugin: DebugPlugin = DebugPlugin()

// DebugPlugin is used to render debug information during combat.
class DebugPlugin : BaseEveryFrameCombatPlugin() {
    private var font: LazyFont? = null
    private var logs: MutableMap<String, LazyFont.DrawableString> = TreeMap()

    operator fun set(index: Any, value: Any?) {
        if (font == null) return

        if (value == null) logs.remove("$index")
        else logs["$index"] = font!!.createText("$value", baseColor = Color.ORANGE)
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if (font == null) {
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
            debugPlugin = this
        }

        // Initialize debug renderer.
        val engine = Global.getCombatEngine()
        if (!engine.customData.containsKey(ID)) {
            engine.addLayeredRenderingPlugin(RenderLines())
            engine.customData[ID] = true
        }

        debug(amount)
//        speedupAsteroids()
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)

        for ((i, v) in logs.entries.withIndex()) {
            v.value.draw(500f, 500f + (logs.count() / 2 - i) * 16f)
        }
    }

    fun clear() {
        logs.clear()
    }

    private fun debug(dt: Float) {
//        val ship = Global.getCombatEngine().playerShip ?: return
//
//        val position = Vector2f(
//            Global.getCombatEngine().viewport.convertScreenXToWorldX(Global.getSettings().mouseX.toFloat()),
//            Global.getCombatEngine().viewport.convertScreenYToWorldY(Global.getSettings().mouseY.toFloat()),
//        )
//
//        val c = Controller2(ship)
//        c.heading(position, dt)
//
//        drawEngineLines(ship)
        makeDroneFormation(dt)
    }

    private var controllers: MutableMap<ShipAPI, Controller2> = mutableMapOf()

    private fun makeDroneFormation(dt: Float) {
        val ship = Global.getCombatEngine().playerShip ?: return
        val drones = Global.getCombatEngine().ships.filter { it.isFighter }

        val angle = 360f / drones.size

        for (i in drones.indices) {
            val drone = drones[i]

            if (!controllers.containsKey(drone)) {
                controllers[drone] = Controller2(drone)
            }

            drone.shipAI = null

//            val offset = Rotation(angle * i + ship.facing).rotate(Vector2f(0f, 230f))
            val offset = Rotation(angle * i).rotate(Vector2f(0f, 230f))

//            debugVertices.add(Line(ship.location, ship.location + offset, Color.YELLOW))

            controllers[drone]!!.heading(ship.location + offset, ship.velocity, dt)
//            controllers[drone]!!.facing(ship.location + offset * 2f)
        }

        drones.forEach { it.shipAI = null }
    }

    private fun speedupAsteroids() {
        val asteroids = Global.getCombatEngine().asteroids
        for (i in asteroids.indices) {
            val a = asteroids[i]
            a.mass = 0f
            a.velocity.set(VectorUtils.getDirectionalVector(Vector2f(), a.velocity) * 1200f)
        }
    }
}


val ShipAPI.hasDirectOrder: Boolean
    get() {
        val fleetManager = Global.getCombatEngine().getFleetManager(this.owner)
        val taskManager = fleetManager.getTaskManager(this.isAlly) as CombatTaskManager
        val deployedFleetMember = fleetManager.getDeployedFleetMember(this) as? O0 ?: return false
        return taskManager.hasDirectOrders(deployedFleetMember)
    }
