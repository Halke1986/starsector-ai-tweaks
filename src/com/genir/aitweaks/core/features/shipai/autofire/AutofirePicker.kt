package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.WeaponAPI

class AutofirePicker {
    private val autofireBlacklist = setOf(
        "fragbomb", // Stinger-class Proximity Mine is classified as a ballistic weapon, but works like a missile.
        "cryoflux", // Cryoflamer has a custom script that makes the projectiles incompatible with ballistic calculations.
    )

    fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin> {
        val shouldHaveCustomAI = when {
            weapon.type == WeaponAPI.WeaponType.MISSILE -> false

            autofireBlacklist.contains(weapon.id) -> false

            else -> true
        }

        val ai: AutofireAIPlugin? = if (shouldHaveCustomAI) AutofireAI(weapon) else null
        return PluginPick(ai, CampaignPlugin.PickPriority.MOD_GENERAL)
    }
}
