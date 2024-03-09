package com.genir.aitweaks

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.features.autofire.AutofireAI

class AITweaksBaseModPlugin : MakeAITweaksRemovable() {
    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin> {
        val ai = if (weapon.type != WeaponAPI.WeaponType.MISSILE) AutofireAI(weapon)
        else null

        return PluginPick(ai, PickPriority.MOD_GENERAL)
    }
}

