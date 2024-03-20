package com.genir.aitweaks.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.utils.Controller
import com.genir.aitweaks.utils.div
import com.genir.aitweaks.utils.extensions.isAutomated
import com.genir.aitweaks.utils.times
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
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

//        debug(amount)
//        speedupAsteroids()
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)

        for ((i, v) in logs.entries.withIndex()) {
            v.value.draw(500f, 500f + (logs.count() / 2 - i) * 16f)
        }
    }

    private var prevPositions: MutableList<Vector2f> = mutableListOf(Vector2f())

    private fun debug(dt: Float) {
//        val ships = Global.getCombatEngine().ships
//
//        for (i in ships.indices) {
//            val it = ships[i]
//            debugPlugin[i] = "${it.hullSpec.hullId} ${it.stationSlot} ${it.owner}"
//        }

        val ship = Global.getCombatEngine().ships.firstOrNull { it.isAutomated } ?: return


        (ship as Ship).ai = null


        val targetShip = Global.getCombatEngine().playerShip ?: return
        val position = Vector2f(Global.getCombatEngine().viewport.convertScreenXToWorldX(Global.getSettings().mouseX.toFloat()), Global.getCombatEngine().viewport.convertScreenYToWorldY(Global.getSettings().mouseY.toFloat()))
//        val position = targetShip.location + Vector2f(250f, 250f)

//        ship.giveCommand(ShipCommand.DECELERATE, null, 0)

//        pid.move(position, ship)
//        pid.rotate(VectorUtils.getFacing(position - ship.location), ship)


        val con = Controller()
//        con.facing(ship, targetShip.location, dt)
        con.facing(ship, position, dt)
        val v = (position - prevPositions.first()) / (dt * prevPositions.size.toFloat())

//        debugPlugin[0] = v
//        debugPlugin[1] = targetShip.velocity

        con.heading(ship, position, Vector2f(), dt)
//        con.heading(ship, position, v, dt)

        prevPositions.add(position)
        if (prevPositions.size > 4) {
            prevPositions.removeFirst()
        }

//        drawEngineLines(ship)
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
