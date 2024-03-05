package com.genir.aitweaks.features

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.BALLISTIC
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.ENERGY
import com.fs.starfarer.api.impl.combat.LidarArrayStats
import com.genir.aitweaks.debugPlugin
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.Target
import com.genir.aitweaks.utils.extensions.frontFacing
import com.genir.aitweaks.utils.extensions.isShip
import com.genir.aitweaks.utils.extensions.isValidTarget
import com.genir.aitweaks.utils.extensions.isVastBulk
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
        ai?.advance()
    }
}

class LidarArrayAIImpl(private val ship: ShipAPI, private val system: ShipSystemAPI, private val flags: ShipwideAIFlags) {
    private val targetTracker = ShipTargetTracker(ship)

    private var aiLock: LockAIOnTarget? = null

    fun advance() {
        debugPlugin[1] = burstFluxRequired()
        debugPlugin[2] = ship.maxFlux - ship.currFlux
        debugPlugin[3] = ship.fluxTracker.timeToVent
        debugPlugin[4] = system.cooldownRemaining

        flags.setFlag(AIFlags.DO_NOT_VENT)

        debugPlugin[0] = ""

        if (aiLock != null) {
            debugPlugin[0] = "LOCKED"
        }

        if (!system.isOn) {
            if (shouldForceVent()) {
                ship.fluxTracker.ventFlux()
            } else if (shouldUseSystem()) {
                ship.useSystem()
            }
        }

        // Attack has started, lock the ship AI on target.
        if (system.isOn && aiLock == null && targetTracker.target?.isValidTarget == true) {
            aiLock = LockAIOnTarget(ship, targetTracker.target)
        }

        // Attack has ended, unlock the AI.
        if (aiLock != null) {
            aiLock!!.advance()
            if (!aiLock!!.isLocked() || !system.isOn) {
                aiLock!!.unlock()
                aiLock = null
            }
        }
    }

    private fun shouldUseSystem(): Boolean {
        // System can be used.
        if (!canUseSystemThisFrame(ship)) return false

        // Has valid target.
        targetTracker.advance()
        val target = targetTracker.target ?: return false
        if (!target.isShip || (target.isFrigate && !target.isStationModule)) return false

        // All weapons are on target.
        return applyLidarRangeBonus { weaponsOnTarget(target) && weaponsNotBlocked() }
    }

    private fun shouldForceVent() = when {
        burstFluxRequired() < ship.maxFlux - ship.currFlux -> false
        ship.fluxTracker.fluxLevel < 0.2f -> false
        else -> ship.fluxTracker.timeToVent >= system.cooldownRemaining
    }

    private fun weaponsOnTarget(target: ShipAPI): Boolean {
        return getLidarWeapons().firstOrNull { !canTrack(it, Target(target), defaultBallisticParams(), it.range * 0.85f) } == null
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

    private fun <T> applyLidarRangeBonus(f: () -> T): T {
        val rangeBonus = LidarArrayStats.RANGE_BONUS - LidarArrayStats.PASSIVE_RANGE_BONUS

        ship.mutableStats.ballisticWeaponRangeBonus.modifyPercent("aitweaks_lidar", rangeBonus)
        ship.mutableStats.energyWeaponRangeBonus.modifyPercent("aitweaks_lidar", rangeBonus)

        val result = f()

        ship.mutableStats.ballisticWeaponRangeBonus.unmodifyPercent("aitweaks_lidar")
        ship.mutableStats.energyWeaponRangeBonus.unmodifyPercent("aitweaks_lidar")

        return result
    }

    private fun burstFluxRequired() = getLidarWeapons().fold(0f) { s, it -> s + weaponFluxRequired(it) }

    private fun weaponFluxRequired(weapon: WeaponAPI): Float {
        return weapon.derivedStats.fluxPerSecond * (1f + LidarArrayStats.ROF_BONUS) * system.chargeActiveDur
    }

    private fun getLidarWeapons(): List<WeaponAPI> = ship.allWeapons.filter {
        it.slot.isHardpoint && it.frontFacing && (it.type == ENERGY || it.type == BALLISTIC) && !it.isPermanentlyDisabled
    }
}
