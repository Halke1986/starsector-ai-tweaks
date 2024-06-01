package com.genir.aitweaks

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.MISSILE
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.genir.aitweaks.features.CryosleeperEncounter
import com.genir.aitweaks.features.autofire.AutofireAI
import com.genir.aitweaks.features.shipai.customAIManager
import com.genir.aitweaks.features.shipai.loading.Loader

val autofireBlacklist = setOf(
    "fragbomb", // Stinger-class Proximity Mine is classified as a ballistic weapon, but works more like a missile.
)

class AITweaksBaseModPlugin : MakeAITweaksRemovable() {
    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin> {
        val ai = if (weapon.type != MISSILE && !autofireBlacklist.contains(weapon.id)) AutofireAI(weapon)
        else null

        return PluginPick(ai, PickPriority.MOD_GENERAL)
    }

    override fun pickShipAI(member: FleetMemberAPI?, ship: ShipAPI): PluginPick<ShipAIPlugin> {
        return PluginPick(customAIManager.getCustomAI(ship), PickPriority.MOD_GENERAL)
    }

    override fun onNewGame() {
        super.onNewGame()
        onGameStart()
    }

    // TODO can this be removed?
    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)
        onGameStart()
    }

    private fun onGameStart() {
        // Test custom AI class loader. Better to crash on game start,
        // instead of when player has made progress.
        customAIManager.test()

        // Register Cryosleeper encounter plugin.
        val plugins = Global.getSector().genericPlugins
        if (!plugins.hasPlugin(CryosleeperEncounter::class.java)) {
            plugins.addPlugin(CryosleeperEncounter(), true)
        }
    }

    override fun onApplicationLoad() {
        customAIManager.test()

//        val cl = URLClassLoader((this::class.java.classLoader as URLClassLoader).urLs)
//        cl.loadClass("com.genir.aitweaks.Injector").newInstance()

//        (ClassLoader.getSystemClassLoader() as URLClassLoader).urLs.forEach {
//            log(it)
//        }

//        throw Exception("done")

//        Printer();
//
//        InjectorLoader().loadClass();
    }
}


