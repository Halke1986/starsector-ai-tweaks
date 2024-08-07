package com.genir.aitweaks.core.features.shipai.systems

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.BALLISTIC
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.ENERGY
import com.fs.starfarer.api.impl.combat.LidarArrayStats
import com.genir.aitweaks.core.features.shipai.AI
import com.genir.aitweaks.core.features.shipai.command
import com.genir.aitweaks.core.utils.attack.AttackTarget
import com.genir.aitweaks.core.utils.attack.canTrack
import com.genir.aitweaks.core.utils.attack.defaultBallisticParams
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.aitweaks.core.utils.extensions.attackTarget
import com.genir.aitweaks.core.utils.extensions.isHullDamageable
import com.genir.aitweaks.core.utils.extensions.isShip
import com.genir.aitweaks.core.utils.firstShipAlongLineOfFire
import org.lazywizard.lazylib.combat.AIUtils.canUseSystemThisFrame

class LidarArray(ai: AI) : SystemAI(ai) {
    private val updateInterval = defaultAIInterval()
    private var rangeOverride: Float = minLidarWeaponRange()

    override fun advance(dt: Float) {
        updateInterval.advance(dt)
        if (updateInterval.intervalElapsed()) {

            // Override maneuver distance to always stay at lidar weapons range.
            if (system.state == ShipSystemAPI.SystemState.IDLE)
                rangeOverride = minLidarWeaponRange()
            ai.vanilla.flags.setFlag(ShipwideAIFlags.AIFlags.MANEUVER_RANGE_FROM_TARGET, 1.0f, rangeOverride)

            when {
                system.isOn || getLidarWeapons().isEmpty() -> return

                shouldForceVent() -> ship.command(ShipCommand.VENT_FLUX)

                shouldUseSystem() -> ship.command(ShipCommand.USE_SYSTEM)
            }
        }
    }

    private fun shouldUseSystem(): Boolean {
        // System can be used.
        if (!canUseSystemThisFrame(ship)) return false

        // Has valid target.
        val target = ship.attackTarget ?: return false
        when {
            !target.isShip -> return false
            target.isFrigate && !target.isStationModule -> return false
            target.armorGrid.armorRating < 250 -> return false
        }

        // All weapons are on target.
        return applyLidarRangeBonus { weaponsOnTarget(target) && weaponsNotBlocked() }
    }

    private fun shouldForceVent() = when {
        burstFluxRequired() < ship.maxFlux - ship.currFlux -> false
        ship.fluxTracker.fluxLevel < 0.2f -> false
        else -> ship.fluxTracker.timeToVent >= system.cooldownRemaining
    }

    // TODO delegate to autofire plugin
    private fun weaponsOnTarget(target: ShipAPI): Boolean {
        return getLidarWeapons().firstOrNull { !canTrack(it, AttackTarget(target), defaultBallisticParams(), it.range * 0.92f) } == null
    }

    private fun weaponsNotBlocked(): Boolean {
        return getLidarWeapons().firstOrNull { isWeaponBlocked(it) } == null
    }

    private fun isWeaponBlocked(weapon: WeaponAPI): Boolean {
        val hit = firstShipAlongLineOfFire(weapon, defaultBallisticParams())?.target
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
        return applyLidarRangeBonus { getLidarWeapons().minOf { w -> w.range + w.slot.location.x } }
    }

    private fun burstFluxRequired(): Float {
        val weaponBaseFlux = getLidarWeapons().sumOf { it.derivedStats.fluxPerSecond.toDouble() }
        val weaponFlux = weaponBaseFlux.toFloat() * (1f + LidarArrayStats.ROF_BONUS)
        val dissipation = ship.mutableStats.fluxDissipation.modifiedValue

        return (weaponFlux - dissipation) * system.chargeActiveDur
    }

    private fun getLidarWeapons(): List<WeaponAPI> = ship.allWeapons.filter {
        it.isLidarWeapon && !it.isPermanentlyDisabled
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

    private val WeaponAPI.isLidarWeapon: Boolean
        get() = this.slot.isHardpoint && !this.isBeam && !this.isDecorative && (this.type == ENERGY || this.type == BALLISTIC)
}

