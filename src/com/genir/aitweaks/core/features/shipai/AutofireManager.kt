package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.extensions.customAI
import com.genir.aitweaks.core.extensions.isMissile
import com.genir.aitweaks.core.extensions.isPD
import com.genir.aitweaks.core.features.shipai.autofire.SyncState
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.aitweaks.core.utils.defaultAIInterval
import org.lwjgl.util.vector.Vector2f

class AutofireManager(val ship: ShipAPI) : Obfuscated.AutofireManager {
    private val updateInterval: IntervalUtil = defaultAIInterval()
    private val weaponSyncMap: MutableMap<String, SyncState> = mutableMapOf()
    private var autofireCount = 0

    override fun autofireManager_advance(dt: Float, threatEvalAI: Obfuscated.ThreatEvaluator?, missileDangerDir: Vector2f?) {
        updateInterval.advance(dt)
        if (updateInterval.intervalElapsed()) {
            autofireCount = ensureAutofire()
            updateWeaponSync()
        }

        // If all 7 weapon groups are autofiring, then shipAI forcefully selects one of them
        // to control manually. Block SELECT_GROUP command to prevent that.
        if (autofireCount == 7) ship.blockCommandForOneFrame(ShipCommand.SELECT_GROUP)
    }

    private fun ensureAutofire(): Int {
        return ship.weaponGroupsCopy.count { group ->
            val weapons = group.weaponsCopy

            val shouldAutofire = when {
                weapons.any { it.isPD } -> true
                !ship.hullSpec.isBuiltInMod("missile_reload") && weapons.any { it.isMissile } -> false
                else -> true
            }

            if (shouldAutofire) {
                // Deselect group that should be autofiring.
                if (ship.selectedGroupAPI == group) (ship as Ship).setNoWeaponSelected()
                group.toggleOn()
            } else {
                group.toggleOff()
            }

            shouldAutofire
        }
    }

    /** Ensure all weapons that are to fire in staggered mode share an up-to date state. */
    private fun updateWeaponSync() {
        if (!state.config.enabledStaggeredFire) return

        val weapons: Sequence<WeaponAPI> = ship.weaponGroupsCopy.flatMap { it.weaponsCopy }.asSequence()
        val syncWeapons = weapons.filter {
            when {
                it.isBeam -> false
                it.isPD -> false
                it.isMissile -> false

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
