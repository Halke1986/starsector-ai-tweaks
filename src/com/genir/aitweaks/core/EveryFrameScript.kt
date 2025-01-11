package com.genir.aitweaks.core

import com.fs.starfarer.api.EveryFrameScript

class EveryFrameScript : EveryFrameScript {
    override fun advance(amount: Float) = Unit

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true
}
