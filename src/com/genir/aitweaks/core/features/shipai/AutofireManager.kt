package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.features.shipai.autofire.SyncState
import com.genir.aitweaks.core.utils.Interval
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.aitweaks.core.utils.extensions.customAI
import com.genir.aitweaks.core.utils.extensions.isPD

class AutofireManager(val ship: ShipAPI) {
    private val updateInterval: Interval = defaultAIInterval()
    private val weaponSyncMap: MutableMap<String, SyncState> = mutableMapOf()

    fun advance(dt: Float) {
        updateInterval.advance(dt)
        if (updateInterval.elapsed()) {
            updateInterval.reset()

            ensureAutofire()
            updateWeaponSync()
        }
    }

    private fun ensureAutofire() {
        // Ensure autofire on all groups.
        (ship as Ship).setNoWeaponSelected()
        ship.weaponGroupsCopy.forEach { it.toggleOn() }
    }

    /** Ensure all weapons that are to fire in staggered mode share an up-to date state. */
    private fun updateWeaponSync() {
        val weapons: Sequence<WeaponAPI> = ship.weaponGroupsCopy.flatMap { it.weaponsCopy }.asSequence()
        val syncWeapons = weapons.filter {
            when {
                it.isBeam -> false

                it.isPD -> false

                it.type == WeaponAPI.WeaponType.MISSILE -> false

                else -> true
            }
        }.mapNotNull { it.customAI }

        weaponSyncMap.forEach { it.value.weapons = 0 }

        syncWeapons.forEach { autofireAI ->
            val state: SyncState = weaponSyncMap[autofireAI.weapon.id] ?: run {
                val newState = SyncState(0, 0f)
                weaponSyncMap[autofireAI.weapon.id] = newState
                newState
            }

            state.weapons++
            autofireAI.syncState = state
        }
    }
}
