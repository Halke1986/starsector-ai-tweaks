package com.genir.aitweaks.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.asm.combat.ai.AssemblyShipAI
import com.genir.aitweaks.utils.times
import org.lazywizard.lazylib.VectorUtils
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
        val ships = Global.getCombatEngine().ships

//        ships.forEach { ship ->
//            ship.AITFlags.maneuverTarget?.let { debugVertices.add(Line(ship.location, it.location, Color.BLUE)) }
//            ship.AITFlags.attackTarget?.let { debugVertices.add(Line(ship.location, it.location, Color.RED)) }
//
//            if (ship.hasAIType<AssemblyShipAI>()) {
//                debugVertices.add(Line(ship.location, ship.location + Vector2f(ship.velocity).resized(400f), Color.RED))
////                debugVertices.add(Line(ship.location, ship.location + ship.ai as AssemblyShipAI)  Vector2f(ship.velocity).resized(400f), Color.RED))
//                debugPlugin["speed"] = ship.velocity.length()
//            }
//        }

        val ship = ships.firstOrNull { it.owner == 0 } ?: return

        debugPlugin[0] = (ship.ai as? AssemblyShipAI)?.currentManeuver?.javaClass?.canonicalName
//        debugPlugin["avoiding collision"] = if ((ship.ai as AssemblyShipAI).flockingAI.String()) "avoiding collision" else ""
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
