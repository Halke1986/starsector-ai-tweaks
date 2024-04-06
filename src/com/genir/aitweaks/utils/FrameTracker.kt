package com.genir.aitweaks.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI

var frameTracker = 0

class FrameTracker : BaseEveryFrameCombatPlugin() {
    private var frame = 0
    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if (!Global.getCombatEngine().isPaused)
            frameTracker = frame++
    }
}