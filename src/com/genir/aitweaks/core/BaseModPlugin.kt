package com.genir.aitweaks.core

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority.MOD_GENERAL
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.genir.aitweaks.core.shipai.autofire.AutofirePicker

class BaseModPlugin : BaseModPlugin() {
    override fun pickShipAI(member: FleetMemberAPI?, ship: ShipAPI): PluginPick<ShipAIPlugin>? {
        return CustomAIManager.pickShipAI(member, ship)
    }

    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin>? {
        val ai: AutofireAIPlugin = AutofirePicker().pickWeaponAutofireAI(weapon) ?: return null
        return PluginPick(ai, MOD_GENERAL)
    }
}
