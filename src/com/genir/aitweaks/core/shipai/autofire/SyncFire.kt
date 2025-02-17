package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.state.Config.Companion.config
import kotlin.math.min
import kotlin.math.round

/** SyncFire is used to synchronise weapons that are to fire in staggered mode. */
class SyncFire(private val weapon: WeaponAPI, var state: State?) {
    data class State(var weapons: Int, var lastAttack: Float)

    private var idleFrames: Int = 0
    private var isInSync: Boolean = false
    private var prevROF: Float = 0f

    fun advance() {
        if (weapon.isInFiringCycle) {
            idleFrames = 0
        } else {
            idleFrames++
        }

        // Weapon went out of sync.
        if (idleFrames > 1) {
            isInSync = false
        }

        // Ensure any weapon that may disrupt its
        // firing cycle will be re-synchronized.
        if (weapon.isOutOfOrder) {
            isInSync = false
        }

        // Re-synchronized after a period of modified rate of fire.
        val rof = weapon.RoFMultiplier
        if (rof < prevROF) {
            isInSync = false
        }

        prevROF = rof
    }

    /** Make weapons sync fire in staggered firing mode. */
    fun shouldFire(): Boolean {
        // Staggered fire is not available for manually controlled ships.
        if (weapon.ship.isUnderManualControl) {
            isInSync = false
            return true
        }

        val state = this.state
        if (state == null) {
            isInSync = false
            return true
        }

        // No need to sync a single weapon.
        if (state.weapons == 1) {
            isInSync = false
            return true
        }

        // Do not attempt to sync a weapon with disrupted firing cycle,
        // as this may lead corrupted synchronization state.
        if (weapon.isOutOfOrder) {
            isInSync = false
            return true
        }

        // Weapons with firing cycle longer than 6 seconds
        // are not eligible for staggered firing mode.
        val combinedCycleDuration = weapon.firingCycle.duration
        if (combinedCycleDuration > 6f) {
            isInSync = false
            return true
        }

        val timestamp = Global.getCombatEngine().getTotalElapsedTime(false)
        val sinceLastAttack = timestamp - state.lastAttack
        val stagger = combinedCycleDuration / state.weapons
        val dt = Global.getCombatEngine().elapsedInLastFrame

        // Combined rate of fire is too fast to sync.
        if (stagger < dt * 2) {
            isInSync = false
            return true
        }

        // Weapon is the middle of firing cycle, it won't stop now.
        if (weapon.isInFiringCycle) {
            return true
        }

        val cycles = round(sinceLastAttack / stagger)
        val opportunity = state.lastAttack + cycles * stagger
        val tolerance = min(stagger / 2, dt * 3)

        isInSync = when {
            // Weapons of same type didn't attack for at least entire firing cycle,
            // meaning all of them are ready to attack. The weapon may fire immediately.
            sinceLastAttack > combinedCycleDuration -> {
                state.lastAttack = timestamp
                true
            }

            // Another weapon seized the attack opportunity.
            cycles == 0f -> false

            // Weapon finished its firing cycle. Assume it's not yet out of
            // sync and continue attack. NOTE: there's also an idle frame between shots in
            // a burst, but that case if handled by 'if (weapon.isInFiringCycle)
            // return true' case.
            idleFrames == 1 && isInSync -> {
                state.lastAttack = opportunity
                true
            }

            // Wait for opportunity to attack aligned with the staggered attack cycle.
            timestamp >= opportunity && timestamp <= opportunity + tolerance -> {
                state.lastAttack = opportunity
                true
            }

            else -> false
        }

        return isInSync
    }

    /** Weapon is considered 'out of order' if it's unable
     * to follow fire command while in idle state. */
    private val WeaponAPI.isOutOfOrder: Boolean
        get() {
            return when {
                isDisabled -> true

                isPermanentlyDisabled -> true

                isOutOfAmmo -> true

                isForceNoFireOneFrame -> true

                ship.fluxTracker.isOverloadedOrVenting -> true

                ship.fluxTracker.currFlux + weapon.fluxCostToFire >= ship.fluxTracker.maxFlux -> true

                else -> false
            }
        }

    companion object {
        /** Ensure all weapons that are to fire in staggered mode share an up-to date state. */
        fun updateWeaponSync(ship: ShipAPI) {
            if (!config.enabledStaggeredFire) {
                return
            }

            // Find weapons to sync.
            val weapons: Sequence<WeaponAPI> = ship.allGroupedWeapons.asSequence()
            val syncWeapons: Sequence<AutofireAI> = weapons.filter {
                when {
                    it.isBeam -> false
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
