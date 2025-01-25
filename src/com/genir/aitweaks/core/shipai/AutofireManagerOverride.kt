package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.combat.ai.attack.AttackAIModule
import com.genir.aitweaks.core.extensions.basicShipAI
import java.lang.reflect.Field

/** Plugin for replacing vanilla AutofireManager with AI Tweaks implementation. */
class AutofireManagerOverride : BaseEveryFrameCombatPlugin() {
    private val updateInterval = IntervalUtil(0.75f, 1f)

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        updateInterval.advance(dt)
        if (!updateInterval.intervalElapsed()) {
            return
        }

        val ships = Global.getCombatEngine().ships.asSequence().filter {
            when {
                !it.isAlive -> false

                it.isFighter -> false

                // Override autofire manager only for vanilla AI.
                it.basicShipAI == null -> false

                else -> true
            }
        }

        ships.forEach {
            inject(it, it.basicShipAI!!.attackAI)
        }
    }

    companion object {
        /** Replace vanilla autofire manager with AI Tweaks adapter. */
        fun inject(ship: ShipAPI, attackModule: AttackAIModule) {
            // Find the obfuscated AttackAIModule.autofireManager field.
            val fields: Array<Field> = AttackAIModule::class.java.declaredFields
            val field = fields.first { it.type.isInterface && it.type.methods.size == 1 }
            field.setAccessible(true)

            // AutofireManager is already overridden.
            if (AutofireManager::class.java.isInstance(field.get(attackModule))) {
                return
            }

            field.set(attackModule, AutofireManager(ship))
        }
    }
}
