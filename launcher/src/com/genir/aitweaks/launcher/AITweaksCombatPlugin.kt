package com.genir.aitweaks.launcher

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin
import com.genir.aitweaks.launcher.loading.CoreLoaderManager
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate

class AITweaksCombatPlugin : EveryFrameCombatPlugin by newPlugin() {
    companion object {
        private fun newPlugin(): BaseEveryFrameCombatPlugin {
            // If the plugin is initialized outside a combat engine, use a dummy implementation.
            val engine = Global.getCombatEngine() ?: return BaseEveryFrameCombatPlugin()

            // Try to reuse an existing combat state.
            val state = engine.customData["aitweaks_combat_state"]
            if (state is BaseEveryFrameCombatPlugin) {
                return state
            }

            // Rebuild core loader at the beginning of each combat.
            // used for aitweaks-core-dev-*.jar hot reload.
            CoreLoaderManager.updateLoader()
            val newState: BaseEveryFrameCombatPlugin = coreLoader.loadClass("com.genir.aitweaks.core.state.State").instantiate()

            // Store the state for easy access.
            Global.getCombatEngine().customData["aitweaks_combat_state"] = newState
            return newState
        }
    }
}
