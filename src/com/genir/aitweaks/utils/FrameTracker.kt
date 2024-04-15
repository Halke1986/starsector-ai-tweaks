package com.genir.aitweaks.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI

var frameTracker = 0f

class FrameTracker : BaseEveryFrameCombatPlugin() {
    private var tSum = 0f
    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        if (!Global.getCombatEngine().isPaused) {
            tSum += dt
            frameTracker = tSum
        }
    }
}