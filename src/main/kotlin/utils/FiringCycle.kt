package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI

fun firingCycle(weapon: WeaponAPI) = when {
    weapon.isBurstBeam -> BurstBeam(weapon)
    weapon.isBeam -> Beam(weapon)
    else -> Projectile(weapon)
}

interface FiringCycle {
    val damage: Float
    val warmupDuration: Float
    val burstDuration: Float
    val duration: Float
}

class Beam(weapon: WeaponAPI) : FiringCycle {
    override val warmupDuration = 0f
    override val burstDuration = 1f
    override val duration = 1f
    override val damage = weapon.damage.damage
}

class BurstBeam(weapon: WeaponAPI) : FiringCycle {
    private val s: WeaponSpecAPI = weapon.spec

    private val chargeupDuration = s.beamChargeupTime
    private val chargedownDuration = s.beamChargedownTime
    private val cooldownDuration = weapon.cooldown - s.beamChargedownTime
    private val reducedPowerDuration = chargeupDuration + chargedownDuration

    override val warmupDuration = 0f
    override val burstDuration = s.burstDuration
    override val damage = weapon.damage.damage * (burstDuration + reducedPowerDuration / 3f)
    override val duration = burstDuration + reducedPowerDuration + cooldownDuration
}

class Projectile(weapon: WeaponAPI) : FiringCycle {
    private val s = weapon.spec as ProjectileWeaponSpecAPI

    private val cooldownDuration = weapon.cooldown

    override val warmupDuration = s.chargeTime
    override val burstDuration = (s.burstSize - 1) * s.burstDelay
    override val duration = warmupDuration + burstDuration + cooldownDuration
    override val damage = weapon.damage.damage * s.burstSize
}
