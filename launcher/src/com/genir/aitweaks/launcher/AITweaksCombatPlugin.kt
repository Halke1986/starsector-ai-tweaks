package com.genir.aitweaks.launcher

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin
import com.genir.aitweaks.launcher.loading.CoreLoaderManager
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.newCoreObject

class AITweaksCombatPlugin : EveryFrameCombatPlugin by newPlugin() {
    companion object {
        private fun newPlugin(): BaseEveryFrameCombatPlugin {
            // Rebuild core loader at the beginning of each battle;
            // used for aitweaks-core-dev-*.jar hot reload.
            CoreLoaderManager.updateLoader()

            // Combat state.
            return newCoreObject("com.genir.aitweaks.core.state.State")
        }
    }
}
