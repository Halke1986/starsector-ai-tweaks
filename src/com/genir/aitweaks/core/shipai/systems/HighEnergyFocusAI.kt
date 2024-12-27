package com.genir.aitweaks.core.shipai.systems

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.DamageType.*
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize.*
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.autofire.defaultBallisticParams
import com.genir.aitweaks.core.shipai.autofire.willHitShield
import org.lazywizard.lazylib.combat.AIUtils
import org.lwjgl.util.vector.Vector2f

class HighEnergyFocusAI : ShipSystemAIScript {
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
        val weapons: List<WeaponAPI> = energyWeapons(ship)

        // All weapons are in cooldown.
        if (weapons.all { it.cooldownRemaining > 0f }) return false

        val dpsTotal = weapons.sumOf { it.effectiveDPS }
        val dpsHef = weapons.sumOf { (it.derivedStats.dps * dpsMultiplier(it)) }

        // Too small portion of the total DPS is affected by HEF.
        if (dpsHef * 2f < dpsTotal) return false

        // Last charge is reserved for largest weapons.
        val largestWeapon = largestWeaponSize(weapons)
        if (ship.system.ammo == 1) {
            return weapons.filter { it.size == largestWeapon }.any { dpsMultiplier(it) > 0 }
        }

        return true
    }

    private fun energyWeapons(ship: ShipAPI): List<WeaponAPI> {
        return ship.allGroupedWeapons.filter { weapon ->
            when {
                weapon.isDisabled -> false
                weapon.isPermanentlyDisabled -> false
                else -> weapon.type == WeaponType.ENERGY
            }
        }
    }

    private fun largestWeaponSize(weapons: List<WeaponAPI>): WeaponAPI.WeaponSize {
        val sizes = weapons.map { it.size }
        return when {
            sizes.contains(LARGE) -> LARGE
            sizes.contains(MEDIUM) -> MEDIUM
            else -> SMALL
        }
    }

    private fun dpsMultiplier(weapon: WeaponAPI): Float {
        val target: ShipAPI = (weapon.target as? ShipAPI) ?: return 0f
        val params = defaultBallisticParams

        return when {
            // Check custom AI decision.
            weapon.customAI?.shouldHoldFire != null -> 0f

            // Check firing cycle.
            weapon.isIdle -> 0f
            weapon.cooldownRemaining > weapon.ship.system.chargeActiveDur -> 0f

            // Check if target is valid.
            !target.isValidTarget -> 0f
            target.isPhased -> 0f

            // Account for damage type.
            weapon.damageType == FRAGMENTATION -> 0.25f
            weapon.damageType == ENERGY -> 1f
            weapon.damageType == OTHER -> 1f
            target.isFighter -> 1f
            weapon.damageType == HIGH_EXPLOSIVE && willHitShield(weapon, target, params) == null -> 2f
            weapon.damageType == KINETIC && willHitShield(weapon, target, params) != null -> 2f
            else -> 0.5f
        }
    }
}
