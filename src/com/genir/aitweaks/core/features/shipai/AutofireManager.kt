package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.combat.ai.attack.AttackAIModule
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.features.shipai.autofire.SyncState
import com.genir.aitweaks.core.utils.Interval
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.aitweaks.core.utils.extensions.customAI
import com.genir.aitweaks.core.utils.extensions.isMissile
import com.genir.aitweaks.core.utils.extensions.isPD
import lunalib.lunaSettings.LunaSettings
import org.lwjgl.util.vector.Vector2f
import java.lang.reflect.Field

class AutofireManager(val ship: ShipAPI) : Obfuscated.AutofireManager {
    private val enabledStaggeredFire: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_staggered_fire") == true
    private val updateInterval: Interval = defaultAIInterval()
    private val weaponSyncMap: MutableMap<String, SyncState> = mutableMapOf()

    override fun advance_AutofireManager(dt: Float, threatEvalAI: Obfuscated.ThreatEvalAI?, missileDangerDir: Vector2f?) {
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
        if (!enabledStaggeredFire) return

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
            // TODO replace with compiled code.
            val f = findAutofireManagerField().also { it.setAccessible(true) }
            if (AutofireManager::class.java.isInstance(f.get(attackModule))) return

            f.set(attackModule, AutofireManager(ship))
        }

        /** Find obfuscated AttackAIModule.autofireManager field. */
        private fun findAutofireManagerField(): Field {
            val fields: Array<Field> = AttackAIModule::class.java.declaredFields
            return fields.first { it.type.isInterface && it.type.methods.size == 1 }
        }
    }
}
