package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.*
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI

data class FiringCycle(val damage: Float, val warmupDuration: Float, val burstDuration: Float, val duration: Float)

val WeaponAPI.firingCycle: FiringCycle
    get() = when {
        // Burst beam.
        isBurstBeam -> {
            val spec: BeamWeaponSpecAPI = spec as BeamWeaponSpecAPI
            val chargeDownDuration = spec.beamChargedownTime
            val cooldownDuration = cooldown - spec.beamChargedownTime
            val reducedPowerDuration = spec.beamChargeupTime + chargeDownDuration

            val warmupDuration = 0f
            val burstDuration = spec.burstDuration + reducedPowerDuration
            val damage = damage.damage * (spec.burstDuration + reducedPowerDuration / 3f)
            val duration = spec.burstDuration + cooldownDuration

            FiringCycle(damage, warmupDuration, burstDuration, duration)
        }

        // Continuous beam.
        isBeam -> {
            val damage = damage.damage
            val warmupDuration = 0f
            val burstDuration = 0f
            val duration = 1f

            FiringCycle(damage, warmupDuration, burstDuration, duration)
        }

        // Projectile weapon.
        spec is ProjectileWeaponSpecAPI -> {
            val spec = spec as ProjectileWeaponSpecAPI
            val cooldownDuration = cooldown

            val warmupDuration = spec.chargeTime
            val burstDuration = (spec.burstSize - 1) * spec.burstDelay
            val duration = warmupDuration + burstDuration + cooldownDuration
            val damage = damage.damage * spec.burstSize

            val rof = 1f / when (type) {
                BALLISTIC -> ship.mutableStats.ballisticRoFMult.modifiedValue
                ENERGY -> ship.mutableStats.energyRoFMult.modifiedValue
                MISSILE -> ship.mutableStats.missileRoFMult.modifiedValue
                else -> 1f
            }

            FiringCycle(damage, warmupDuration * rof, burstDuration * rof, duration * rof)
        }

        // Unknown.
        else -> FiringCycle(0f, 0f, 0f, 0f)
    }


