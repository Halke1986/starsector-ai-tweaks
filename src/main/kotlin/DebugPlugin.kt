package com.genir.aitweaks

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

var debugPlugin: DebugPlugin = DebugPlugin()

// DebugPlugin is used to render debug information during combat.
class DebugPlugin : BaseEveryFrameCombatPlugin() {
    private var font: LazyFont? = null
    private var drawable: MutableMap<Int, LazyFont.DrawableString> = mutableMapOf()

    operator fun set(index: Int, value: Any) {
        if (font == null) return

        drawable[index] = font!!.createText("$index: $value", baseColor = Color.ORANGE)
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)
        if (Global.getCurrentState() != GameState.COMBAT) return

        if (font == null) {
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
            debugPlugin = this
        }

        debug()
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)

        var i = 0
        for (v in drawable) {
            v.value.draw(500f, 500f + (drawable.count() / 2 - i) * 16f)
            i++
        }
    }

    private fun debug() {
//        val weapons = Global.getCombatEngine().ships.filter { it.owner == 0 }.map { it.allWeapons }.flatten()
//        for (i in weapons.indices) {
//            debugPlugin[i] = "${weapons[i].spec.weaponId} ${weapons[i].totalRange / weapons[i].range}"
//        }
    }
}
