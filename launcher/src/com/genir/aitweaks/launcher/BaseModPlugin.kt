package com.genir.aitweaks.launcher

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
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
            val autofireClass = coreLoader.loadClass("com.genir.aitweaks.core.features.shipai.autofire.AutofireAI")
            val ctorType = MethodType.methodType(Void.TYPE, WeaponAPI::class.java)
            val ctor = MethodHandles.lookup().findConstructor(autofireClass, ctorType)

            return PluginPick(ctor.invoke(weapon) as AutofireAIPlugin, PickPriority.MOD_GENERAL)
        }

        return PluginPick(null, PickPriority.MOD_GENERAL)
    }

    override fun pickShipAI(member: FleetMemberAPI?, ship: ShipAPI): PluginPick<ShipAIPlugin> {
        val aiManagerClass = coreLoader.loadClass("com.genir.aitweaks.core.features.shipai.CustomAIManager")
        val aiManager = aiManagerClass.newInstance()

        val getCustomAIForShipType = MethodType.methodType(ShipAIPlugin::class.java, ShipAPI::class.java)
        val getCustomAIForShip = MethodHandles.lookup().findVirtual(aiManagerClass, "getCustomAIForShip", getCustomAIForShipType)

        val customAI: ShipAIPlugin? = getCustomAIForShip.invoke(aiManager, ship) as? ShipAIPlugin
        return PluginPick(customAI, PickPriority.MOD_GENERAL)
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
        // Register Cryosleeper encounter plugin.
        val plugins = Global.getSector().genericPlugins
        if (!plugins.hasPlugin(CryosleeperEncounter::class.java)) {
            plugins.addPlugin(CryosleeperEncounter(), true)
        }
    }
}
