package com.genir.aitweaks.core.features.shipai.systems

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.BALLISTIC
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.ENERGY
import com.fs.starfarer.api.impl.combat.LidarArrayStats
import com.genir.aitweaks.core.features.shipai.CustomShipAI
import com.genir.aitweaks.core.features.shipai.autofire.BallisticTarget
import com.genir.aitweaks.core.features.shipai.autofire.canTrack
import com.genir.aitweaks.core.features.shipai.autofire.defaultBallisticParams
import com.genir.aitweaks.core.features.shipai.command
import com.genir.aitweaks.core.utils.Interval
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.aitweaks.core.utils.extensions.attackTarget
import com.genir.aitweaks.core.utils.extensions.autofirePlugin
import com.genir.aitweaks.core.utils.extensions.isHullDamageable
import com.genir.aitweaks.core.utils.extensions.isValidTarget
import com.genir.aitweaks.core.utils.firstShipAlongLineOfFire
import org.lazywizard.lazylib.combat.AIUtils.canUseSystemThisFrame

class LidarArray(ai: CustomShipAI) : SystemAI(ai) {
    private val updateInterval: Interval = defaultAIInterval()
    private var lidarWeapons: List<WeaponAPI> = listOf()
    private var zeroFluxBoostMode: Boolean = false

    @Suppress("ConstPropertyName")
    companion object Preset {
        const val weaponRangeFraction = 0.92f
        const val ventThreshold = 0.2f
    }

    override fun advance(dt: Float) {
        updateInterval.advance(dt)

        // Use weapons only during lidar burst, to preserve zero flux boost.
        if (zeroFluxBoostMode && !system.isOn) lidarWeapons.forEach { it.autofirePlugin?.forceOff() }

        if (updateInterval.elapsed()) {
            updateInterval.reset()
            lidarWeapons = getLidarWeapons()

            if (lidarWeapons.isEmpty() || system.isActive) return

            // Override maneuver distance to always stay at lidar weapons range.
            ai.vanilla.flags.setFlag(ShipwideAIFlags.AIFlags.MANEUVER_RANGE_FROM_TARGET, 1.0f, minLidarWeaponRange())

            // Check if ship has other flux drains except lidar weapons.
            zeroFluxBoostMode = lidarWeapons.size == ai.stats.significantWeapons.size && ship.shield == null

            when {
                shouldForceVent() -> ship.command(ShipCommand.VENT_FLUX)

                shouldUseSystem() -> ship.command(ShipCommand.USE_SYSTEM)
            }
        }
    }

    private fun shouldUseSystem(): Boolean {
        return when {
            // System is not ready.
            !canUseSystemThisFrame(ship) -> false

            // Does ship wave a valid target.
            ship.attackTarget?.isValidTarget != true -> false

            ship.attackTarget?.isFighter == true -> false

            // All weapons are on target.
            else -> applyLidarRangeBonus { weaponsOnTarget(ship.attackTarget!!) && weaponsNotBlocked() }
        }
    }

    private fun shouldForceVent(): Boolean {
        return when {
            zeroFluxBoostMode && !ship.fluxTracker.isVenting && ship.fluxTracker.fluxLevel > ventThreshold -> true

            // Ship has enough flux for next burst.
            burstFluxRequired() < ship.maxFlux - ship.currFlux -> false

            // Do not force mini-vents.
            ship.fluxTracker.fluxLevel < ventThreshold -> false

            else -> ship.fluxTracker.timeToVent >= system.cooldownRemaining
        }
    }

    private fun weaponsOnTarget(target: ShipAPI): Boolean {
        return lidarWeapons.firstOrNull { !canTrack(it, BallisticTarget.entity(target), defaultBallisticParams, it.range * weaponRangeFraction) } == null
    }

    private fun weaponsNotBlocked(): Boolean {
        return lidarWeapons.firstOrNull { isWeaponBlocked(it) } == null
    }

    private fun isWeaponBlocked(weapon: WeaponAPI): Boolean {
        val hit = firstShipAlongLineOfFire(weapon, defaultBallisticParams)?.target
        return when {
            hit == null -> false
            hit !is ShipAPI -> false
            !hit.isAlive -> true
            !hit.isHullDamageable -> true
            hit.owner == weapon.ship.owner -> true
            else -> false
        }
    }

    private fun minLidarWeaponRange(): Float {
        return applyLidarRangeBonus { lidarWeapons.minOf { w -> w.range + w.slot.location.x } } * weaponRangeFraction
    }

    private fun burstFluxRequired(): Float {
        val weaponBaseFlux = lidarWeapons.sumOf { it.derivedStats.fluxPerSecond.toDouble() }
        val weaponFlux = weaponBaseFlux.toFloat() * (1f + LidarArrayStats.ROF_BONUS)
        val dissipation = ship.mutableStats.fluxDissipation.modifiedValue

        return (weaponFlux - dissipation) * system.chargeActiveDur
    }

    private fun <T> applyLidarRangeBonus(f: () -> T): T {
        val rangeBonus = LidarArrayStats.RANGE_BONUS - LidarArrayStats.PASSIVE_RANGE_BONUS

        ship.mutableStats.ballisticWeaponRangeBonus.modifyPercent("aitweaks_lidar", rangeBonus)
        ship.mutableStats.energyWeaponRangeBonus.modifyPercent("aitweaks_lidar", rangeBonus)

        val result = f()

        ship.mutableStats.ballisticWeaponRangeBonus.unmodifyPercent("aitweaks_lidar")
        ship.mutableStats.energyWeaponRangeBonus.unmodifyPercent("aitweaks_lidar")

        return result
    }

    private fun getLidarWeapons(): List<WeaponAPI> {
        return ai.stats.significantWeapons.filter { it.isLidarWeapon }
    }

    private val WeaponAPI.isLidarWeapon: Boolean
        get() = slot.isHardpoint && !isBeam && (type == ENERGY || type == BALLISTIC)
}
