package com.genir.aitweaks.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.CustomClassLoader
import com.genir.aitweaks.utils.times
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URLClassLoader
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

    fun xx() {
        var cl: ClassLoader = DebugPlugin::class.java.getClassLoader()
        while (cl !is URLClassLoader) cl = cl.parent
        val urls = cl.urLs

        val cls: Class<*> = CustomClassLoader(urls, ClassLoader.getSystemClassLoader())
            .loadClass("com.genir.aitweaks.asm.BasicShipAI")

        val ctor = MethodHandles.lookup().findConstructor(cls, MethodType.methodType(Void.TYPE))
    }


//
//            while (cl != null && !(cl instanceof URLClassLoader)) cl = cl.getParent();
//            if (cl == null) throw new RuntimeException("Unable to find URLClassLoader");
//            URL[] urls = ((URLClassLoader) cl).getURLs();
//
//            @SuppressWarnings("resource")
//            Class<?> cls = new CustomClassLoader(urls, ClassLoader.getSystemClassLoader())
//                .loadClass(PluginMain.class.getPackage().getName() + ".CoreUIWatchScript");
//
//            ctor = MethodHandles.lookup().findConstructor(cls, MethodType.methodType(void.class));
//        } catch (RuntimeException | Error ex) {
//            throw ex;
//        } catch (Throwable t) {
//            throw new ExceptionInInitializerError(t);
//        }
//        scriptCtor = ctor;
//    }

    fun clear() {
        logs.clear()
    }

    private fun debug(dt: Float) {
//        Global.getCombatEngine().ships.forEach{
//        xx()
            val klas = Class.forName("com.genir.aitweaks.asm.BasicShipAI")

        debugPlugin[0] = "OK"

//            com.fs.starfarer.combat.ai.N.`String.super`(ship as Ship)

//            val ai = (it.shipAI as? Ship.ShipAIWrapper)?.ai ?: it.shipAI

//            debugPlugin[it] = ai.javaClass.canonicalName

//        }
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
