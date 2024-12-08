package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.aitweaks.core.utils.shieldUptime

class Backoff(private val ship: ShipAPI) {
    private val damageTracker: DamageTracker = DamageTracker(ship)
    private val updateInterval: IntervalUtil = defaultAIInterval()

    private var idleTime = 0f
    var isBackingOff: Boolean = false

    fun advance(dt: Float) {
        damageTracker.advance()
        updateIdleTime(dt)

        updateInterval.advance(dt)
        if (updateInterval.intervalElapsed()) {
            updateBackoffStatus()
            ventIfNeeded()
        }
    }

    private fun updateIdleTime(dt: Float) {
        val shieldIsUp = ship.shield?.isOn == true && shieldUptime(ship.shield) > Preset.shieldFlickerThreshold
        val pdIsFiring = ship.allWeapons.firstOrNull { it.isPD && it.isFiring } != null

        idleTime = if (shieldIsUp || pdIsFiring) 0f
        else idleTime + dt
    }

    /** Decide if ships needs to back off due to high flux level */
    private fun updateBackoffStatus() {
        val fluxLevel = ship.fluxTracker.fluxLevel
        val underFire = damageTracker.damage / ship.maxFlux > 0.2f

        isBackingOff = when {
            // Enemy is routing, keep the pressure.
            Global.getCombatEngine().isEnemyInFullRetreat -> false

            // Ship with no shield backs off when it can't fire anymore.
            ship.shield == null && ship.allWeapons.any { !it.isInFiringSequence && it.fluxCostToFire >= ship.fluxLeft } -> true

            // High flux.
            ship.shield != null && fluxLevel > Preset.backoffUpperThreshold -> true

            // Shields down and received damage.
            underFire && ship.shield != null && ship.shield.isOff -> true

            // Started venting under fire.
            underFire && ship.fluxTracker.isVenting -> true

            // Stop backing off.
            fluxLevel <= Preset.backoffLowerThreshold -> false

            // Continue current backoff status.
            else -> isBackingOff
        }

        if (isBackingOff) ship.aiFlags.setFlag(ShipwideAIFlags.AIFlags.BACKING_OFF)
        else ship.aiFlags.unsetFlag(ShipwideAIFlags.AIFlags.BACKING_OFF)
    }

    /** Force vent when the ship is backing off,
     * not shooting and with shields down. */
    private fun ventIfNeeded() {
        val shouldVent = when {
            // Already venting.
            ship.fluxTracker.isVenting -> false

            // No need to vent.
            ship.fluxLevel < Preset.backoffLowerThreshold -> false

            // Don't interrupt the ship system.
            ship.system?.isOn == true -> false

            !isBackingOff -> false

            // Vent regardless of situation when already passively
            // dissipated below Preset.backoffLowerThreshold
            ship.fluxLevel < Preset.forceVentThreshold -> true

            idleTime < Preset.shieldDownVentTime -> false

            // Don't vent when defending from missiles.
            ship.allWeapons.any { it.autofirePlugin?.targetMissile != null } -> false

            else -> true
        }

        if (shouldVent) ship.command(ShipCommand.VENT_FLUX)
    }
}
