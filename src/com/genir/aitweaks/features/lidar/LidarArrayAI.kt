package com.genir.aitweaks.features.lidar

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.BALLISTIC
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.ENERGY
import com.fs.starfarer.api.impl.combat.LidarArrayStats
import com.genir.aitweaks.utils.attack.AttackTarget
import com.genir.aitweaks.utils.attack.ShipTargetTracker
import com.genir.aitweaks.utils.attack.canTrack
import com.genir.aitweaks.utils.attack.defaultBallisticParams
import com.genir.aitweaks.utils.defaultAIInterval
import com.genir.aitweaks.utils.extensions.frontFacing
import com.genir.aitweaks.utils.extensions.isShip
import com.genir.aitweaks.utils.extensions.isVastBulk
import com.genir.aitweaks.utils.firstShipAlongLineOfFire
import org.lazywizard.lazylib.combat.AIUtils.canUseSystemThisFrame
import org.lwjgl.util.vector.Vector2f

class LidarArrayAI : ShipSystemAIScript {
    private var ai: LidarArrayAIImpl? = null

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        ship ?: return
        system ?: return
        flags ?: return

        this.ai = LidarArrayAIImpl(ship, system, flags)
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        ai?.advance(amount)
    }
}

class LidarArrayAIImpl(private val ship: ShipAPI, private val system: ShipSystemAPI, private val flags: ShipwideAIFlags) {
    private val targetTracker = ShipTargetTracker(ship)
    private var advanceInterval = defaultAIInterval()

    fun advance(timeDelta: Float) {
        advanceInterval.advance(timeDelta)
        if (!advanceInterval.intervalElapsed()) return

        flags.setFlag(AIFlags.DO_NOT_VENT)

        // Assume ship is not under vanilla AI
        // control when lidar array is active.
        if (system.isOn) return

        val minLidarRange = minLidarWeaponRange()
        flags.setFlag(AIFlags.BACK_OFF_MIN_RANGE, 1.0f, minLidarRange * 0.6f)

        if (shouldForceVent()) {
            ship.giveCommand(ShipCommand.VENT_FLUX, null, 0)
        } else if (shouldUseSystem()) {
            ship.useSystem()

            // Set data to be used by ShipAI.
            ship.setCustomData(lidarConfigID, LidarConfig(targetTracker.target, minLidarRange))
        }
    }

    private fun shouldUseSystem(): Boolean {
        // System can be used.
        if (!canUseSystemThisFrame(ship)) return false

        // Has valid target.
        targetTracker.advance()
        val target = targetTracker.target ?: return false
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

    private fun weaponsOnTarget(target: ShipAPI): Boolean {
        return getLidarWeapons().firstOrNull { !canTrack(it, AttackTarget(target), defaultBallisticParams(), it.range * 0.85f) } == null
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
            hit.isVastBulk -> true
            hit.owner == weapon.ship.owner -> true
            else -> false
        }
    }

    private fun minLidarWeaponRange(): Float {
        return applyLidarRangeBonus { getLidarWeapons().minOf { w -> w.range + w.slot.location.x } }
    }

    private fun burstFluxRequired(): Float {
        return getLidarWeapons().fold(0f) { s, it -> s + weaponFluxRequired(it) }
    }

    private fun weaponFluxRequired(weapon: WeaponAPI): Float {
        return weapon.derivedStats.fluxPerSecond * (1f + LidarArrayStats.ROF_BONUS) * system.chargeActiveDur
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
}

internal val WeaponAPI.isLidarWeapon: Boolean
    get() = this.slot.isHardpoint && this.frontFacing && (this.type == ENERGY || this.type == BALLISTIC)