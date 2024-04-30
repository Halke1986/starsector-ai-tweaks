package com.genir.aitweaks.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.features.maneuver.ShipAIPluginExtended
import com.genir.aitweaks.utils.*
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
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

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
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
        makeDroneFormation(dt)
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)

        for ((i, v) in logs.entries.withIndex()) {
            v.value.draw(500f, 100f + (logs.count() / 2 - i) * 16f)
        }
    }

    fun clear() {
        logs.clear()
    }

    private fun debug(dt: Float) {
        val ship = Global.getCombatEngine().ships.firstOrNull { it.owner == 0 } ?: return

//        debugPlugin["ai name"] = ship.ai?.javaClass?.canonicalName

        val m = ship.AITStash.maneuverAI ?: return

//        debugVertices.add(Line(ship.location, ship.location + unitVector(m.desiredHeading).resized(400f), Color.GREEN))
//        debugVertices.add(Line(ship.location + ship.velocity * 5f, ship.location, Color.GREEN))
//        debugVertices.add(Line(ship.location + ship.velocity * 5f, ship.location + ship.velocity * 5f + (m.attackTarget?.velocity
//            ?: Vector2f()) * 5f, Color.RED))
        debugVertices.add(Line(ship.location, m.headingPoint ?: ship.location, Color.YELLOW))
        debugVertices.add(Line(ship.location, m.attackTarget?.location ?: ship.location, Color.RED))
        debugVertices.add(Line(ship.location, m.maneuverTarget?.location ?: ship.location, Color.BLUE))
//        drawEngineLines(ship)

//        debugPlugin["speed"] = "speed ${ship.velocity.length()}"
//        debugPlugin["dist"] = "dist  ${(ship.location - (m.travelPoint ?: ship.location)).length()}"

        debugPlugin["isBackingOff"] = if (m.isBackingOff) "is backing off" else null
        debugPlugin["isHoldingFire"] = if (m.isHoldingFire) "hold fire" else null
        debugPlugin["isAvoidingBorder"] = if (m.isAvoidingBorder) "avoid border" else null

//        debugPlugin["1"] = "100 ${ship.maxRange}"
//        debugPlugin["66"] = "66  ${m.range66}"
//        debugPlugin["33"] = "33  ${m.range33}"


        debugPlugin["maneuver"] = (ship.ai as? ShipAIPluginExtended)?.getCurrentManeuver()?.javaClass?.canonicalName
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

    private fun speedupAsteroids() {
        val asteroids = Global.getCombatEngine().asteroids
        for (i in asteroids.indices) {
            val a = asteroids[i]
            a.mass = 0f
            a.velocity.set(VectorUtils.getDirectionalVector(Vector2f(), a.velocity) * 1200f)
        }
    }
}

