package com.genir.aitweaks.core.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.genir.aitweaks.core.features.shipai.vanilla.AutofireManagerAdapter
import com.genir.aitweaks.core.utils.Interval
import com.genir.aitweaks.core.utils.extensions.hasBasicShipAI

/** Plugin for replacing vanilla AutofireManager with AI Tweaks implementation. */
class OverrideAutofire : BaseEveryFrameCombatPlugin() {
    private val updateInterval = Interval(0.75f, 1f)

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        updateInterval.advance(dt)
        if (!updateInterval.elapsed()) return

        updateInterval.reset()

        val ships = Global.getCombatEngine().ships.filter {
            when {
                !it.isAlive -> false

                it.isFighter -> false

                !it.hasBasicShipAI -> false

                else -> true
            }
        }.asSequence()

        ships.forEach {
            AutofireManagerAdapter.inject(it, (it.ai as BasicShipAI).attackAI)
        }
    }
}
