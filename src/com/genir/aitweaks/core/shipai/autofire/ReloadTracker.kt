package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.extensions.totalReloadTime
import com.genir.aitweaks.core.extensions.totalReloadTimeRemaining
import com.genir.aitweaks.core.shipai.Preset

class ReloadTracker(private val weapon: WeaponAPI) {
    var isInLongReload = false

    fun advance() {
        // Weapon is not reloading ammo.
        if (!weapon.usesAmmo() || weapon.ammoPerSecond == 0f) {
            return
        }

        isInLongReload = when {
            weapon.totalReloadTimeRemaining < 2f -> false

            weapon.totalReloadTime < Preset.weaponMaxReloadTime -> false

            weapon.ammo == 0 -> true

            isInLongReload && weapon.ammo <= weapon.maxAmmo * 0.75f -> true

            else -> isInLongReload
        }
    }
}