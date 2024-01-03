package com.genir.aitweaks

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.features.autofire.AutofireAI

class AITweaksBaseModPlugin : BaseModPlugin() {
    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin> {
        super.pickWeaponAutofireAI(weapon)

        val ai = when {
            Global.getCurrentState() == GameState.TITLE -> null
            else -> AutofireAI(weapon)
        }

        return PluginPick(ai, PickPriority.MOD_GENERAL)
    }
}