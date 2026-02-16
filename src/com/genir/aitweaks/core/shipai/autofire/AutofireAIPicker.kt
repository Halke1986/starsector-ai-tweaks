package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority.MOD_GENERAL
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle
import com.genir.aitweaks.core.state.Config.Companion.config

class AutofireAIPicker : com.genir.aitweaks.launcher.AutofireAIPicker {
    override fun pickWeaponAutofireAI(weaponAPI: WeaponAPI): PluginPick<AutofireAIPlugin>? {
        val weapon = weaponAPI.handle

        return when {
            !shouldHaveCustomAI(weapon) -> {
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

    private fun shouldHaveCustomAI(weapon: WeaponHandle): Boolean {
        return when {
            config.useVanillaAI -> {
                false
            }

            weapon.ship.owner != 0 -> {
                false
            }

            // Vanilla Eval AI creates fake weapons internally.
            // They can be recognized by null specs. Perhaps
            // there are other sources of fake weapons as well.
            weapon.spec == null -> {
                false
            }

            // Unusual no-aim weapons, like the Voltaic Discharge.
            weapon.hasAIHint(WeaponAPI.AIHints.DO_NOT_AIM) -> {
                false
            }

            // Decoratives, fake weapons, etc.
            weapon.derivedStats.dps == 0f -> {
                false
            }

            weapon.hasAITag(Tag.NO_MODDED_AI) -> {
                false
            }

            // Missile weapons
            weapon.isMissile || weapon.spec.projectileSpec is MissileSpecAPI -> {
//                weapon.isUnguidedMissile && weapon.hasAIHint(WeaponAPI.AIHints.DO_NOT_CONSERVE)
                false
            }

            else -> {
                true
            }
        }
    }
}
