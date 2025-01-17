package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.MissileSpecAPI

class AutofirePicker {
    fun pickWeaponAutofireAI(weapon: WeaponAPI): AutofireAIPlugin? {
        return when {
            weapon.type == WeaponAPI.WeaponType.MISSILE -> null

            // Missile weapons pretending to be ballistics.
            weapon.spec?.projectileSpec is MissileSpecAPI -> null

            weapon.hasAITag(Tag.NO_MODDED_AI) -> null

            weapon.hasAITag(Tag.TRIGGER_HAPPY) -> RecklessAutofireAI(weapon)

            else -> AutofireAI(weapon)
        }
    }
}
