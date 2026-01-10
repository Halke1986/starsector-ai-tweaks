package com.genir.aitweaks.launcher

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.SettingsAPI
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate
import lunalib.lunaSettings.LunaSettings

class AITweaksBaseModPlugin : BaseModPlugin() {
    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin>? {
        val picker: AutofireAIPicker = coreLoader.loadClass("com.genir.aitweaks.core.shipai.autofire.AutofireAIPicker").instantiate()
        return picker.pickWeaponAutofireAI(weapon)
    }

    override fun pickShipAI(member: FleetMemberAPI?, ship: ShipAPI): PluginPick<ShipAIPlugin>? {
        val picker: ShipAIPicker = coreLoader.loadClass("com.genir.aitweaks.core.shipai.ShipAIPicker").instantiate()
        return picker.pickShipAI(member, ship)
    }

    override fun onApplicationLoad() {
        if (LunaSettings.getBoolean("aitweaks", "aitweaks_hide_hullmods") == true) {
            val settings: SettingsAPI = Global.getSettings()

            settings.getHullModSpec("aitweaks_finisher_beam_protocol").uiTags.remove("Weapons")
            settings.getHullModSpec("aitweaks_system_shunt").uiTags.remove("Special")
            settings.getHullModSpec("aitweaks_custom_ship_ai").uiTags.remove("Special")
            settings.getHullModSpec("aitweaks_search_and_destroy").uiTags.remove("Special")
            settings.getHullModSpec("aitweaks_skirmisher").uiTags.remove("Special")
        }
    }

    override fun beforeGameSave() {
        MakeAITweaksRemovable.beforeGameSave()
    }

    override fun afterGameSave() {
        MakeAITweaksRemovable.afterGameSave()
    }

    override fun onGameLoad(newGame: Boolean) {
        MakeAITweaksRemovable.onGameLoad()
    }
}
