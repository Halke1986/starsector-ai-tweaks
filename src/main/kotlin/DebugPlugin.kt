package com.genir.aitweaks

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

const val debug = false
var debugStr = "debug"

// DebugPlugin is used to render debug information during combat.
// It's disabled by default.
class DebugPlugin : BaseEveryFrameCombatPlugin() {
    private var engine: CombatEngineAPI? = null
    private var font: LazyFont? = null
    private var drawable: LazyFont.DrawableString? = null
    private var oldDebugStr = ""

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)
        if (!debug || Global.getCurrentState() != GameState.COMBAT)
            return

        if (engine == null) {
            engine = Global.getCombatEngine()
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
        }

        debug()
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)
        if (!debug || font == null)
            return

        if (debugStr != oldDebugStr) {
            oldDebugStr = debugStr
            drawable = font!!.createText(debugStr, baseColor = Color.ORANGE)
        }

        drawable?.draw(500f, 500f)
    }

    private fun debug() {
    }
}


