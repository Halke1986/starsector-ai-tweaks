package com.genir.aitweaks

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.MISSILE
import com.genir.aitweaks.features.autofire.AutofireAI

val autofireBlacklist = setOf(
    "fragbomb", // Stinger-class Proximity Mine is classified as a ballistic weapon, but works more like missile.
)

class AITweaksBaseModPlugin : MakeAITweaksRemovable() {
    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin> {
        val ai = if (weapon.type != MISSILE && !autofireBlacklist.contains(weapon.id)) AutofireAI(weapon)
        else null

        return PluginPick(ai, PickPriority.MOD_GENERAL)
    }
}

