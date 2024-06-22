package com.genir.aitweaks.launcher

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.MISSILE
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.genir.aitweaks.launcher.features.CryosleeperEncounter
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

class BaseModPlugin : MakeAITweaksRemovable() {
    // Stinger-class Proximity Mine is classified as a ballistic weapon, but works like a missile.
    private val autofireBlacklist = setOf("fragbomb")

    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin> {
        if (weapon.type != MISSILE && !autofireBlacklist.contains(weapon.id)) {
            val autofireClass = coreLoader.loadClass("com.genir.aitweaks.features.autofire.AutofireAI")
            val ctorType = MethodType.methodType(Void.TYPE, WeaponAPI::class.java)
            val ctor = MethodHandles.lookup().findConstructor(autofireClass, ctorType)

            return PluginPick(ctor.invoke(weapon) as AutofireAIPlugin, PickPriority.MOD_GENERAL)
        }

        return PluginPick(null, PickPriority.MOD_GENERAL)
    }

    override fun pickShipAI(member: FleetMemberAPI?, ship: ShipAPI): PluginPick<ShipAIPlugin> {
        val aiManagerClass = coreLoader.loadClass("com.genir.aitweaks.features.shipai.CustomAIManager")
        val getAIType = MethodType.methodType(ShipAIPlugin::class.java, ShipAPI::class.java, ShipAIConfig::class.java)
        val getAI = MethodHandles.lookup().findVirtual(aiManagerClass, "getAI", getAIType)

        val ai = getAI.invoke(aiManagerClass.newInstance(), ship, ShipAIConfig()) as? ShipAIPlugin
        return PluginPick(ai, PickPriority.MOD_GENERAL)
    }

    override fun onNewGame() {
        super.onNewGame()
        onGameStart()
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)
        onGameStart()
    }

    private fun onGameStart() {
        // Test custom AI class loader. Better to crash on game start,
        // instead of when the player has made progress.
        val aiManagerClass = coreLoader.loadClass("com.genir.aitweaks.features.shipai.CustomAIManager")
        val testType = MethodType.methodType(Void.TYPE)
        val test = MethodHandles.lookup().findVirtual(aiManagerClass, "test", testType)

        test.invoke(aiManagerClass.newInstance())

        // Register Cryosleeper encounter plugin.
        val plugins = Global.getSector().genericPlugins
        if (!plugins.hasPlugin(CryosleeperEncounter::class.java)) {
            plugins.addPlugin(CryosleeperEncounter(), true)
        }
    }
}


