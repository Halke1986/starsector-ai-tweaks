package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority.MOD_GENERAL
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle
import com.genir.aitweaks.core.state.Config.Companion.config

class AutofireAIPicker : com.genir.aitweaks.launcher.AutofireAIPicker {
    override fun pickWeaponAutofireAI(weaponAPI: WeaponAPI): PluginPick<AutofireAIPlugin>? {
        val weapon = weaponAPI.handle

        return when {
            config.useVanillaAI -> {
                null
            }

            // Vanilla Eval AI creates fake weapons internally.
            // They can be recognized by null specs. Perhaps
            // there are other sources of fake weapons as well.
            weapon.spec == null -> {
                null
            }

            weapon.type == WeaponAPI.WeaponType.MISSILE -> {
                null
            }

            // Missile weapons pretending to be ballistics.
            weapon.spec.projectileSpec is MissileSpecAPI -> {
                null
            }

            // Unusual no-aim weapons, like the Voltaic Discharge.
            weapon.hasAIHint(WeaponAPI.AIHints.DO_NOT_AIM) -> {
                null
            }

            weapon.hasAITag(Tag.NO_MODDED_AI) -> {
                null
            }

            weapon.hasAITag(Tag.TRIGGER_HAPPY) -> {
                PluginPick(RecklessAutofireAI(weapon), MOD_GENERAL)
            }

            else -> {
                PluginPick(AutofireAI(weapon), MOD_GENERAL)
            }
        }
    }
}
