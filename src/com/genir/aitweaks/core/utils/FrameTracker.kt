package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.Global

class FrameTracker {
    var count: Int = 0
    var unpausedCount: Int = 0

    fun advance() {
        count++
        if (!Global.getCombatEngine().isPaused) {
            unpausedCount++
        }
    }
}
