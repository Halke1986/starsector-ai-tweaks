package com.genir.aitweaks.core.shipai.systems

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.DamageType.*
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize.*
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.shipai.autofire.defaultBallisticParams
import com.genir.aitweaks.core.shipai.autofire.willHitShield
import org.lazywizard.lazylib.combat.AIUtils
import org.lwjgl.util.vector.Vector2f

class HighEnergyFocus : ShipSystemAIScript {
    private var ship: ShipAPI? = null

    override fun init(ship: ShipAPI?, p1: ShipSystemAPI?, p2: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(p0: Float, p1: Vector2f?, p2: Vector2f?, p3: ShipAPI?) {
        val ship: ShipAPI = ship ?: return

        when {
            Global.getCurrentState() == GameState.TITLE -> return
            Global.getCombatEngine().isPaused -> return

            !AIUtils.canUseSystemThisFrame(ship) -> return
            !shouldTriggerHEF(ship) -> return
            else -> ship.useSystem()
        }
    }

    /** High Energy Focus is triggered when weapons with at least half
     * of the total energy weapon DPS (adjusted for damage type) are firing. */
    private fun shouldTriggerHEF(ship: ShipAPI): Boolean {
        val weapons: List<WeaponHandle> = energyWeapons(ship)

        // All weapons are in cooldown.
        if (weapons.all { it.cooldownRemaining > 0f }) {
            return false
        }

        // Too small portion of the total DPS is affected by HEF.
        val dpsThreshold = 0.5f * weapons.filter { !it.isPD }.sumOf { it.effectiveDPS }
        val dpsHef = weapons.sumOf { (it.derivedStats.dps * dpsMultiplier(it)) }
        if (dpsHef == 0f || dpsHef < dpsThreshold) {
            return false
        }

        // Last charge is reserved for the largest weapons.
        val largestWeapon = largestWeaponSize(weapons)
        if (ship.system.ammo == 1) {
            return weapons.filter { it.size == largestWeapon }.any { dpsMultiplier(it) > 0 }
        }

        return true
    }

    private fun energyWeapons(ship: ShipAPI): List<WeaponHandle> {
        return ship.allGroupedWeapons.filter { weapon ->
            when {
                weapon.isDisabled -> false
                weapon.isPermanentlyDisabled -> false
                else -> weapon.type == WeaponType.ENERGY
            }
        }
    }

    private fun largestWeaponSize(weapons: List<WeaponHandle>): WeaponAPI.WeaponSize {
        val sizes = weapons.map { it.size }
        return when {
            sizes.contains(LARGE) -> LARGE
            sizes.contains(MEDIUM) -> MEDIUM
            else -> SMALL
        }
    }

    private fun dpsMultiplier(weapon: WeaponHandle): Float {
        val target: ShipAPI = (weapon.target as? ShipAPI) ?: return 0f
        val params = defaultBallisticParams

        return when {
            // Check custom AI decision.
            weapon.customAI?.shouldHoldFire != null -> 0f

            // Check firing cycle.
            weapon.isIdle -> 0f
            weapon.cooldown > weapon.ship.system.chargeActiveDur && weapon.cooldownRemaining > 0.5f -> 0f

            // Check if target is valid.
            !target.isValidTarget -> 0f
            target.isPhased -> 0f

            // Handle PD weapons. PD weapons don't count towards DPS threshold,
            // so on ships with only PD energy weapons, even one of them firing
            // will trigger the HEF.
            weapon.isPD -> 0.25f

            // Account for damage type.
            weapon.damageType == FRAGMENTATION -> 0.25f
            target.isFighter -> 0.25f
            weapon.damageType == HIGH_EXPLOSIVE -> if (willHitShield(weapon, target, params) == null) 2f else 0f
            weapon.damageType == KINETIC -> if (willHitShield(weapon, target, params) != null) 2f else 0f
            else -> 1f
        }
    }
}
