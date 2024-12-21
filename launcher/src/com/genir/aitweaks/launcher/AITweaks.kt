package com.genir.aitweaks.launcher

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.genir.aitweaks.launcher.features.CryosleeperEncounter
import com.genir.aitweaks.launcher.loading.CoreLoaderManagerHandler

class AITweaks : BaseModPlugin() {
    companion object {
        val coreLoaderManager = CoreLoaderManagerHandler()
        var coreLoader: ClassLoader = coreLoaderManager.getCoreLoader()

        fun <T> coreObject(path: String): T {
            return coreLoader.loadClass(path).newInstance() as T
        }
    }

    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin> {
        val core: BaseModPlugin = coreObject("com.genir.aitweaks.core.AITweaksCore")
        return core.pickWeaponAutofireAI(weapon)
    }

    override fun pickShipAI(member: FleetMemberAPI?, ship: ShipAPI): PluginPick<ShipAIPlugin> {
        val core: BaseModPlugin = coreObject("com.genir.aitweaks.core.AITweaksCore")
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
        // Register Cryosleeper encounter plugin.
        val plugins = Global.getSector().genericPlugins
        if (!plugins.hasPlugin(CryosleeperEncounter::class.java)) {
            plugins.addPlugin(CryosleeperEncounter(), true)
        }
    }
}
