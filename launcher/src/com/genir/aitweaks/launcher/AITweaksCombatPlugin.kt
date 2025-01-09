package com.genir.aitweaks.launcher

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin
import com.genir.aitweaks.launcher.loading.CoreLoaderManager
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate

class AITweaksCombatPlugin : EveryFrameCombatPlugin by newPlugin() {
    companion object {
        private fun newPlugin(): BaseEveryFrameCombatPlugin {
            // Rebuild core loader at the beginning of each battle;
            // used for aitweaks-core-dev-*.jar hot reload.
            CoreLoaderManager.updateLoader()

            // Combat state.
            return coreLoader.loadClass("com.genir.aitweaks.core.state.State").instantiate()
        }
    }
}
