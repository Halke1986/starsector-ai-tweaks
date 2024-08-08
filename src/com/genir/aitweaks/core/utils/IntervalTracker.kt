package com.genir.aitweaks.core.utils

import kotlin.random.Random

/** Replacement for vanilla IntervalUtil. Interval does not restart
 * automatically by advance() method. Instead, the interval must be
 * restarted explicitly by calling reset(). This allows for easy
 * tracking of overdue actions.  */
class IntervalTracker(private val min: Float, private val max: Float) {
    private var timeLeft: Float = 0f

    init {
        reset()
    }

    fun advance(dt: Float) {
        timeLeft -= dt
    }

    fun intervalElapsed(): Boolean {
        return timeLeft <= 0f
    }

    fun reset() {
        timeLeft = min + Random.nextFloat() * (max - min)
    }
}
