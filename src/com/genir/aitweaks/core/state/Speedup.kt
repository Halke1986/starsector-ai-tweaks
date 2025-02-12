package com.genir.aitweaks.core.state

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import org.lwjgl.input.Keyboard

/** Plugin enabling the vanilla devmode combat speedup. */
class Speedup : BaseEveryFrameCombatPlugin() {
    private var vanillaDevMode = false
    private var prevSpeedup = false

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val speedup = Keyboard.isKeyDown(Keyboard.KEY_RMENU) && Keyboard.isKeyDown(Keyboard.KEY_K)

        if (!prevSpeedup && speedup) {
            vanillaDevMode = Global.getSettings().isDevMode
        }

        if (prevSpeedup && !speedup) {
            Global.getSettings().isDevMode = vanillaDevMode
        }

        if (speedup) {
            Global.getSettings().isDevMode = true
        }

        prevSpeedup = speedup
    }
}
