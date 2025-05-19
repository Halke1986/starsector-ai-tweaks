package com.genir.aitweaks.core.shipai.autofire

import com.genir.aitweaks.core.handles.WeaponHandle


class ReloadTracker(private val weapon: WeaponHandle) {
    private var isInAmmoRegen = false
    var isInLongReload = false

    fun advance() {
        val fullAmmoThreshold = 0.75f
        val longReloadThreshold = 6f

        isInLongReload = when {
            // Is in long cooldown
            weapon.cooldown >= longReloadThreshold && weapon.cooldownRemaining >= 2f -> true

            !weapon.usesAmmo() -> false

            // Is permanently out of ammo.
            weapon.ammo == 0 && weapon.ammoPerSecond == 0f -> true

            // Weapon regenerates ammo quickly.
            (weapon.maxAmmo * fullAmmoThreshold) / weapon.ammoPerSecond < longReloadThreshold -> false

            // Weapon has full ammo.
            weapon.ammo > weapon.maxAmmo * fullAmmoThreshold -> {
                isInAmmoRegen = false
                false
            }

            // Weapon ran out of ammo.
            weapon.ammo == 0 -> {
                isInAmmoRegen = true
                true
            }

            else -> isInAmmoRegen
        }
    }
}
