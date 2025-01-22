package com.genir.aitweaks.launcher

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate

class AITweaksBaseModPlugin : BaseModPlugin() {
    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin>? {
        val core: BaseModPlugin = coreLoader.loadClass("com.genir.aitweaks.core.BaseModPlugin").instantiate()
        return core.pickWeaponAutofireAI(weapon)
    }

    override fun pickShipAI(member: FleetMemberAPI?, ship: ShipAPI): PluginPick<ShipAIPlugin>? {
        val core: BaseModPlugin = coreLoader.loadClass("com.genir.aitweaks.core.BaseModPlugin").instantiate()
        return core.pickShipAI(member, ship)
    }

    override fun beforeGameSave() {
        MakeAITweaksRemovable.beforeGameSave()
    }

    override fun afterGameSave() {
        MakeAITweaksRemovable.afterGameSave()
    }

    override fun onNewGame() {
        onGameStart()
    }

    override fun onGameLoad(newGame: Boolean) {
        MakeAITweaksRemovable.onGameLoad()
        onGameStart()
    }

    private fun onGameStart() {
        val sector = Global.getSector()
        if (!sector.hasTransientScript(AITweaksEveryFrameScript::class.java)) {
            sector.addTransientScript(AITweaksEveryFrameScript())
        }

        // Register Cryosleeper encounter plugin.
        val plugins = sector.genericPlugins
        if (!plugins.hasPlugin(CryosleeperEncounter::class.java)) {
            plugins.addPlugin(CryosleeperEncounter(), true)
        }
    }
}
