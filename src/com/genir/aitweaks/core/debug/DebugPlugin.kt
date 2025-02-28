package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

// DebugPlugin is used to render debug information during combat.
class DebugPlugin : BaseEveryFrameCombatPlugin() {
    private var font: LazyFont? = null
    private var logs: MutableMap<Any, Pair<String, LazyFont.DrawableString>> = mutableMapOf()
    val renderer = Renderer()

    operator fun set(key: Any, value: Any?) {
        val font = this.font ?: return
        val text: String = value?.toString() ?: ""

        when {
            value == null -> logs.remove(key)

            logs[key]?.first == text -> return

            else -> logs[key] = Pair(text, font.createText(text, baseColor = Color.ORANGE))
        }
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        if (font == null) {
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
        }

        // Initialize debug renderer.
        val id = "aitweaks_debug_plugin"
        val engine = Global.getCombatEngine()
        if (!engine.customData.containsKey(id)) {
            engine.addLayeredRenderingPlugin(renderer)
            engine.customData[id] = true
        }

        debug(dt)
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        for ((i, v) in logs.entries.withIndex()) {
            v.value.second.draw(500f, 400f + (logs.count() / 2 - i) * 16f)
        }
    }

    fun clear() {
        logs.clear()
    }
}
