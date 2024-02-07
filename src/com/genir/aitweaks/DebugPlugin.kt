package com.genir.aitweaks

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color
import java.util.*

var debugPlugin: DebugPlugin = DebugPlugin()

// DebugPlugin is used to render debug information during combat.
class DebugPlugin : BaseEveryFrameCombatPlugin() {
    private var font: LazyFont? = null
    private var drawable: MutableMap<Int, LazyFont.DrawableString> = TreeMap()

    operator fun set(index: Int, value: Any) {
        if (font == null) return

        drawable[index] = font!!.createText("$index: $value", baseColor = Color.ORANGE)
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)

        if (font == null) {
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
            debugPlugin = this
        }

        debug()
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)

        for ((i, v) in drawable.entries.withIndex()) {
            v.value.draw(500f, 500f + (drawable.count() / 2 - i) * 16f)
        }
    }

    private fun debug() {
//        val asteroids = Global.getCombatEngine().asteroids
//        val viewport = Global.getCombatEngine().viewport
////        val plugins = ship.weaponGroupsCopy.filter { it.isAutofiring }.map { it.aiPlugins }.flatten()
////
//        for (i in asteroids.indices) {
//        val a = asteroids[i]
//            val x = viewport.convertWorldXtoScreenX(a.location.x)
//            val y = viewport.convertWorldXtoScreenX(a.location.y)
//
//
//            debugPlugin[i] = "$x $y"
//        }
    }
}
