package com.genir.aitweaks.core.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.features.shipai.AutofireManager
import com.genir.aitweaks.core.utils.Interval
import com.genir.aitweaks.core.utils.extensions.basicShipAI

/** Plugin for replacing vanilla AutofireManager with AI Tweaks implementation. */
class OverrideAutofireManager : BaseEveryFrameCombatPlugin() {
    private val updateInterval = Interval(0.75f, 1f)

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        updateInterval.advance(dt)
        if (!updateInterval.elapsed()) return

        updateInterval.reset()

        val ships = Global.getCombatEngine().ships.filter {
            when {
                !it.isAlive -> false

                it.isFighter -> false

                // Override autofire manager only for vanilla AI.
                it.basicShipAI == null -> false

                else -> true
            }
        }.asSequence()

        ships.forEach {
            AutofireManager.inject(it, it.basicShipAI!!.attackAI)
        }
    }
}
