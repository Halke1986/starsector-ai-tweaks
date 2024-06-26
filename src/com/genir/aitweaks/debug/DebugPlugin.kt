package com.genir.aitweaks.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import com.genir.aitweaks.features.shipai.CustomAIManager
import com.genir.aitweaks.features.shipai.ai.EngineController
import com.genir.aitweaks.utils.Rotation
import com.genir.aitweaks.utils.aitStash
import com.genir.aitweaks.utils.div
import com.genir.aitweaks.utils.extensions.hasAIType
import com.genir.aitweaks.utils.times
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.opengl.DrawUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.opengl.GL11
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
//            engine.addLayeredRenderingPlugin(RendererCollisionRadius())
            engine.customData[ID] = true
        }

        debug(dt)
//        followPlayerShip()
//        followMouse()
//        makeDroneFormation(dt)
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        for ((i, v) in logs.entries.withIndex()) {
            v.value.draw(500f, 200f + (logs.count() / 2 - i) * 16f)
        }
    }

    fun clear() {
        logs.clear()
    }

    private fun debug(dt: Float) {
//        Global.getCombatEngine().asteroids.forEach {
//            Global.getCombatEngine().removeEntity(it)
//        }

        Global.getCombatEngine().ships.forEach { ship ->
//            debugVertex(ship.location, ship.location + accelerationTracker[ship], Color.YELLOW)
        }

        val ships = Global.getCombatEngine().ships.filter { it.hasAIType(CustomAIManager.getCustomAIClass()) }

        ships.forEach {
//            drawEngineLines(it)

            val m = it.aitStash.maneuverAI
            if (m != null && it != Global.getCombatEngine().playerShip) {
//                debugPlugin["man"] = it.aitStash.maneuverAI?.let { m -> m::class.java }
//                debugVertex(it.location, it.location + m.desiredVelocity, Color.YELLOW)
            }

//            it.allWeapons.filter { weapon -> !weapon.isPD }.mapNotNull { weapon -> weapon.autofireAI }.forEachIndexed { idx, aai ->
//                debugPlugin[idx] = aai.shouldHoldFire
//            }
        }

//        val lookup = MethodHandles.lookup()
//        val methodType = MethodType.methodType(oO0O::class.java)
//        val handle = lookup.findVirtual(ship.ai::class.java, "getCurrentManeuver", methodType)
//        debugPlugin["class"] = handle.invoke(ship.ai)?.javaClass?.canonicalName
//
//
//        ship.allWeapons.firstOrNull { it.id == "multineedler" }?.let {
//            debugPlugin["needler"] = it.ammo
//        }
//
//        drawEngineLines(ship)
//
//        val m = ship.AITStash.maneuverAI ?: return
//        debugPlugin["isBackingOff"] = if (m.isBackingOff) "is backing off" else null
//        debugPlugin["isHoldingFire"] = if (m.isHoldingFire) "hold fire" else null
//        debugPlugin["isAvoidingBorder"] = if (m.isAvoidingBorder) "avoid border" else null
//        debugPlugin["1v1"] = if (m.is1v1) "1v1" else null
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

    inner class RendererCollisionRadius : BaseCombatLayeredRenderingPlugin() {
        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)

            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            Misc.setColor(Color.CYAN, 100)
            GL11.glLineWidth(2f / Global.getCombatEngine().viewport.viewMult)

            Global.getCombatEngine().ships.forEach { ship ->

                DrawUtils.drawArc(
                    ship.location.x,
                    ship.location.y,
                    ship.collisionRadius,
                    0f,
                    360f,
                    64,
                    false,
                )
            }

            GL11.glPopAttrib()
        }

        override fun getRenderRadius(): Float = 1e6f

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> = EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS)
    }
}

