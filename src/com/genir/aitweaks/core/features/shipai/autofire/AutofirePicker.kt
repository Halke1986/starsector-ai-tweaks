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
        val ai = when {
            weapon.type == WeaponAPI.WeaponType.MISSILE -> null

            autofireBlacklist.contains(weapon.id) -> null

            weapon.id == "TADA_plasma" || weapon.id == "TS_plasma" -> TadaPlasmaAI(weapon)

            else -> AutofireAI(weapon)
        }

        return PluginPick(ai, CampaignPlugin.PickPriority.MOD_GENERAL)
    }
}
