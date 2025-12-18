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
    companion object { var advancedGunneryControlNotFound = true }

    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin>? {
        val picker: AutofireAIPicker = coreLoader.loadClass("com.genir.aitweaks.core.shipai.autofire.AutofireAIPicker").instantiate()
        return picker.pickWeaponAutofireAI(weapon)
    }

    override fun pickShipAI(member: FleetMemberAPI?, ship: ShipAPI): PluginPick<ShipAIPlugin>? {
        val picker: ShipAIPicker = coreLoader.loadClass("com.genir.aitweaks.core.shipai.ShipAIPicker").instantiate()
        return picker.pickShipAI(member, ship)
    }

    override fun beforeGameSave() {
        MakeAITweaksRemovable.beforeGameSave()
    }

    override fun afterGameSave() {
        MakeAITweaksRemovable.afterGameSave()
    }

    override fun onApplicationLoad() {
        advancedGunneryControlNotFound = !Global.getSettings().modManager.isModEnabled("advanced_gunnery_control_dbeaa06e")
    }

    override fun onGameLoad(newGame: Boolean) {
        MakeAITweaksRemovable.onGameLoad()
    }
}
