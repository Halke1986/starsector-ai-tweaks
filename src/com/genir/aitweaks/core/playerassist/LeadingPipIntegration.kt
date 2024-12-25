package com.genir.aitweaks.core.playerassist

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.CombatEngine
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.extensions.copy
import com.genir.aitweaks.core.extensions.minus
import com.genir.aitweaks.core.utils.PI
import com.genir.aitweaks.core.utils.times
import kotlin.math.cos

/** Leading PIP mod guides players to manually lead the target even when
 * AI Tweaks Aim Assist is enabled, which might lead to confusion.
 * LeadingPipIntegration overrides the lead indicator position when
 * Aim Assist is enabled. */
object LeadingPipIntegration {
    fun overrideTargetingLeadIndicator(manager: AimAssistManager) {
        val pip = findEveryFramePlugin("data.scripts.everyframe.TargetingLeadIndicator") ?: return

        val combatEngine = Global.getCombatEngine() as CombatEngine
        combatEngine.removePlugin(pip)
        combatEngine.addPlugin(TargetingLeadIndicatorWrapper(pip, manager))
    }

    private fun findEveryFramePlugin(name: String): EveryFrameCombatPlugin? {
        val combatEngine = Global.getCombatEngine() as Obfuscated.CombatEngine
        val pluginContainers = combatEngine.combatMap.combatMap_getPluginContainers()

        val missionPlugins = pluginContainers.filterIsInstance<Obfuscated.MissionDefinitionPluginContainer>()
        val plugins = missionPlugins.map { it.missionDefinitionPluginContainer_getEveryFrameCombatPlugin() }
        return plugins.firstOrNull { it::class.java.name == name }
    }

    class TargetingLeadIndicatorWrapper(private val pip: EveryFrameCombatPlugin, private val manager: AimAssistManager) : EveryFrameCombatPlugin by pip {
        private var timeEnabled = 0f

        /** Run Leading PIP with spoofed target velocity, so that it
         * centers on the target location when Aim Assist is enabled. */
        override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
            timeEnabled += if (manager.enableAimAssist) dt else -dt
            timeEnabled = timeEnabled.coerceIn(0f, 0.5f)

            val ship: ShipAPI? = Global.getCombatEngine().playerShip
            val target: ShipAPI? = ship?.shipTarget
            if (ship == null || target == null) {
                pip.advance(dt, events)
                return
            }

            // Transition between overriding and not
            // overriding PIP location in a smooth way.
            val scale = (1 - cos(timeEnabled * 2 * PI)) / 2
            val vRelative = (target.velocity - ship.velocity) * scale
            val vTarget = target.velocity.copy

            target.velocity.set(target.velocity - vRelative)
            pip.advance(dt, events)
            target.velocity.set(vTarget)
        }
    }
}
