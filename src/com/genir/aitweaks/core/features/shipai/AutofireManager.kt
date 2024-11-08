package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.combat.ai.attack.AttackAIModule
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.features.shipai.autofire.SyncState
import com.genir.aitweaks.core.state.state
import com.genir.aitweaks.core.utils.Interval
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.aitweaks.core.utils.extensions.customAI
import com.genir.aitweaks.core.utils.extensions.isMissile
import com.genir.aitweaks.core.utils.extensions.isPD
import org.lwjgl.util.vector.Vector2f
import java.lang.reflect.Field

class AutofireManager(val ship: ShipAPI) : Obfuscated.AutofireManager {

    private val updateInterval: Interval = defaultAIInterval()
    private val weaponSyncMap: MutableMap<String, SyncState> = mutableMapOf()

    override fun autofireManager_advance(dt: Float, threatEvalAI: Obfuscated.ThreatEvalAI?, missileDangerDir: Vector2f?) {
        updateInterval.advance(dt)
        if (updateInterval.elapsed()) {
            updateInterval.reset()

            ensureAutofire()
            updateWeaponSync()
        }
    }

    private fun ensureAutofire() {
        ship.weaponGroupsCopy.forEach { group ->
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

    companion object {
        /** Replace vanilla autofire manager with AI Tweaks adapter. */
        fun inject(ship: ShipAPI, attackModule: AttackAIModule) {
            // Find the obfuscated AttackAIModule.autofireManager field.
            val fields: Array<Field> = AttackAIModule::class.java.declaredFields
            val field = fields.first { it.type.isInterface && it.type.methods.size == 1 }
            field.setAccessible(true)

            if (AutofireManager::class.java.isInstance(field.get(attackModule))) return
            field.set(attackModule, AutofireManager(ship))
        }
    }
}
