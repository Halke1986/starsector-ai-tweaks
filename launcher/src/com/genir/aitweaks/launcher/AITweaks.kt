package com.genir.aitweaks.launcher

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.genir.aitweaks.launcher.features.CryosleeperEncounter
import com.genir.aitweaks.launcher.loading.CoreLoaderManagerHandler
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

class AITweaks : BaseModPlugin() {
    companion object {
        val coreLoaderManager = CoreLoaderManagerHandler()
        var coreLoader: ClassLoader = coreLoaderManager.getCoreLoader()
    }

    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin> {
        val autofirePickerClass: Class<*> = coreLoader.loadClass("com.genir.aitweaks.core.features.shipai.autofire.AutofirePicker")
        val autofirePicker: Any = autofirePickerClass.newInstance()

        val pickWeaponType: MethodType = MethodType.methodType(PluginPick::class.java, WeaponAPI::class.java)
        val pickWeapon: MethodHandle = MethodHandles.lookup().findVirtual(autofirePickerClass, "pickWeaponAutofireAI", pickWeaponType)

        return pickWeapon(autofirePicker, weapon) as PluginPick<AutofireAIPlugin>
    }

    override fun pickShipAI(member: FleetMemberAPI?, ship: ShipAPI): PluginPick<ShipAIPlugin> {
        val aiManagerClass = coreLoader.loadClass("com.genir.aitweaks.core.features.CustomAIManager")
        val aiManager = aiManagerClass.newInstance()

        val getAIForShipType = MethodType.methodType(ShipAIPlugin::class.java, ShipAPI::class.java)
        val getAIForShip = MethodHandles.lookup().findVirtual(aiManagerClass, "getAIForShip", getAIForShipType)

        val customAI: ShipAIPlugin? = getAIForShip.invoke(aiManager, ship) as? ShipAIPlugin
        return PluginPick(customAI, PickPriority.MOD_GENERAL)
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
