package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.state.State.Companion.state

// TODO disable for player ship

/** SyncFire is used to synchronise weapons that are to fire in staggered mode. */
class SyncFire(private val weapon: WeaponAPI, var state: State?) {
    data class State(var weapons: Int, var lastAttack: Float)

    private var idleFrames: Int = 0

    fun advance() {
        if (weapon.isInFiringCycle) {
            idleFrames = 0
        } else {
            idleFrames++
        }
    }

    /** Make weapons sync fire in staggered firing mode. */
    fun shouldFire(): Boolean {
        val state = this.state ?: return true

        // No need to sync a single weapon.
        if (state.weapons == 1) return true

        // Weapon is the middle of firing cycle, it won't stop now.
        if (weapon.isInFiringCycle) return true

        // Weapons with firing cycle longer than 8 seconds
        // are not eligible for staggered firing mode.
        val cycleDuration = weapon.firingCycle.duration
        if (cycleDuration >= 6f) return true

        val timestamp = Global.getCombatEngine().getTotalElapsedTime(false)
        val sinceLastAttack = timestamp - state.lastAttack
        val stagger = cycleDuration / state.weapons
        val dt = Global.getCombatEngine().elapsedInLastFrame

        val shouldFire = when {
            // Combined rate of fire is too fast to sync.
            stagger < dt * 2f -> true

            // Weapon finished its firing cycle. Assume it's not yet out of
            // sync and continue attack, unless another weapon attacked the
            // same frame. NOTE: there's also an idle frame between shots in
            // a burst, but that case if handled by 'if (weapon.isInFiringCycle)
            // return true' case.
            sinceLastAttack >= stagger && idleFrames == 1 -> true

            // Weapons of same type didn't attack for at least entire firing cycle,
            // meaning all of them are ready to attack. The weapon may fire immediately.
            sinceLastAttack > cycleDuration -> true

            // Wait for opportunity to attack aligned with staggered attack cycle.
            else -> sinceLastAttack >= stagger && sinceLastAttack % stagger <= dt
        }

        if (shouldFire) state.lastAttack = timestamp

        return shouldFire
    }

    companion object {
        /** Ensure all weapons that are to fire in staggered mode share an up-to date state. */
        fun updateWeaponSync(ship: ShipAPI) {
            if (!state.config.enabledStaggeredFire) {
                return
            }

            // Find weapons to sync.
            val weapons: Sequence<WeaponAPI> = ship.allGroupedWeapons.asSequence()
            val syncWeapons: Sequence<AutofireAI> = weapons.filter {
                when {
                    it.isBeam -> false
                    it.isPD -> false
                    it.isMissile -> false

                    else -> true
                }
            }.mapNotNull { it.customAI }

            // Group fire synchronizers by weapon type.
            val fireSyncByType: MutableMap<String, MutableList<SyncFire>> = mutableMapOf()
            syncWeapons.forEach { weaponAI ->
                val list = fireSyncByType[weaponAI.weapon.id] ?: run {
                    val newList = mutableListOf<SyncFire>()
                    fireSyncByType[weaponAI.weapon.id] = newList
                    newList
                }
                list.add(weaponAI.syncFire)
            }

            // Ensure all fire synchronizers of a given type share a common state object.
            fireSyncByType.forEach { (_, fireSynchronizers) ->
                val state = fireSynchronizers.asSequence().mapNotNull { it.state }.firstOrNull() ?: State(0, 0f)
                state.weapons = fireSynchronizers.size
                fireSynchronizers.forEach { it.state = state }
            }
        }
    }
}
