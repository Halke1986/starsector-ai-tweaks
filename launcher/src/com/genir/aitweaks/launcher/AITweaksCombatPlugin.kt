package com.genir.aitweaks.launcher

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin
import com.genir.aitweaks.launcher.AITweaks.Companion.coreLoader
import com.genir.aitweaks.launcher.AITweaks.Companion.coreLoaderManager
import com.genir.aitweaks.launcher.AITweaks.Companion.coreObject

class AITweaksCombatPlugin : EveryFrameCombatPlugin by newPlugin() {
    companion object {
        private fun newPlugin(): BaseEveryFrameCombatPlugin {
            // Rebuild core loader at the beginning of each battle;
            // used for aitweaks-core-dev-*.jar hot reload.
            coreLoader = coreLoaderManager.getCoreLoader()

            // Combat state.
            return coreObject("com.genir.aitweaks.core.state.State")
        }
    }
}
