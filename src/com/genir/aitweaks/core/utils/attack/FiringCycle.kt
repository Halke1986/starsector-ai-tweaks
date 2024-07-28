package com.genir.aitweaks.core.utils.attack

import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI

val WeaponAPI.firingCycle: FiringCycle
    get() = when {
        isBurstBeam -> BurstBeam(this)
        isBeam -> Beam(this)
        else -> Projectile(this)
    }

interface FiringCycle {
    val damage: Float
    val warmupDuration: Float
    val burstDuration: Float
    val duration: Float
}

class Beam(weapon: WeaponAPI) : FiringCycle {
    override val damage = weapon.damage.damage
    override val warmupDuration = 0f
    override val burstDuration = 1f
    override val duration = 1f
}

class BurstBeam(weapon: WeaponAPI) : FiringCycle {
    private val spec: BeamWeaponSpecAPI = weapon.spec as BeamWeaponSpecAPI
    private val chargeDownDuration = spec.beamChargedownTime
    private val cooldownDuration = weapon.cooldown - spec.beamChargedownTime
    private val reducedPowerDuration = spec.beamChargeupTime + chargeDownDuration

    override val warmupDuration = 0f
    override val burstDuration = spec.burstDuration
    override val damage = weapon.damage.damage * (spec.burstDuration + reducedPowerDuration / 3f)
    override val duration = spec.burstDuration + reducedPowerDuration + cooldownDuration
}

class Projectile(weapon: WeaponAPI) : FiringCycle {
    private val spec = weapon.spec as ProjectileWeaponSpecAPI
    private val cooldownDuration = weapon.cooldown

    override val warmupDuration = spec.chargeTime
    override val burstDuration = (spec.burstSize - 1) * spec.burstDelay
    override val duration = warmupDuration + burstDuration + cooldownDuration
    override val damage = weapon.damage.damage * spec.burstSize
}
