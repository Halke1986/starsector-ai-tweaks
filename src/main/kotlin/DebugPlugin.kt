package com.genir.aitweaks

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

// DebugPlugin is used to render debug information during combat.
class DebugPlugin : BaseEveryFrameCombatPlugin() {
    private var engine: CombatEngineAPI? = null
    private var font: LazyFont? = null
    private var drawable: MutableMap<Int, LazyFont.DrawableString> = mutableMapOf()

    operator fun set(index: Int, value: Any) {
        if (font == null) return
        drawable[index] = font!!.createText(value.toString(), baseColor = Color.ORANGE)
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)
        if (Global.getCurrentState() != GameState.COMBAT) return

        if (engine == null) {
            engine = Global.getCombatEngine()
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
        }

        debug()
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)

        for (i in drawable) {
            i.value.draw(500f, 500f - i.key * 16f)
        }
    }

    private fun debug() {
    }
}
