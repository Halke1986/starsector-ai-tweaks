package com.genir.aitweaks

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.features.autofire.TurretAutofireAI

class AITweaksBaseModPlugin : BaseModPlugin() {
    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin> {
        super.pickWeaponAutofireAI(weapon)

        val ai = if (weapon.type != WeaponAPI.WeaponType.MISSILE) TurretAutofireAI(weapon)
        else null

        return PluginPick(ai, PickPriority.MOD_GENERAL)
    }
}