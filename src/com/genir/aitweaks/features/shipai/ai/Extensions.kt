package com.genir.aitweaks.features.shipai.ai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.extensions.frontFacing
import com.genir.aitweaks.utils.extensions.isPD
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

/** Weapon range from the center of the ship. */
val WeaponAPI.trueRange: Float
    get() = this.range + this.slot.location.x

/** List of all front facing non-PD non-Missile weapons. */
val ShipAPI.primaryWeapons: List<WeaponAPI>
    get() = this.allWeapons.filter { weapon ->
        when {
            weapon.type == WeaponAPI.WeaponType.MISSILE -> false
            !weapon.frontFacing -> false
            weapon.isPD -> false
            weapon.isPermanentlyDisabled -> false
            weapon.usesAmmo() && weapon.ammoTracker?.reloadSize == 0f -> false
            else -> true
        }
    }

/** Range at which all primary weapons can hit. */
val ShipAPI.minRange: Float
    get() = primaryWeapons.minOfOrNull { it.trueRange } ?: 0f

/** Range at which at least one primary weapon can. */
val ShipAPI.maxRange: Float
    get() = primaryWeapons.maxOfOrNull { it.trueRange } ?: 0f

/** Range at which the ship can deliver at least
 * `dpsFraction` of its primary weapons DPS. */
fun ShipAPI.effectiveRange(dpsFraction: Float): Float {
    val weapons = primaryWeapons
    val dps = weapons.sumOf { it.derivedStats.dps.toDouble() }.toFloat()

    if (dps == 0f)
        return 0f

    var dpsInRange = dps
    weapons.sortedWith(compareBy { it.trueRange }).forEach { weapon ->
        dpsInRange -= weapon.derivedStats.dps
        if (dpsInRange / dps <= dpsFraction) {
            return weapon.trueRange
        }
    }

    return 0f
}

/** Fraction of primary weapons DPS that can be delivered at the given range. */
fun ShipAPI.dpsFractionAtRange(range: Float): Float {
    var all = 0f
    var inRange = 0f

    primaryWeapons.forEach {
        val dps = it.derivedStats.dps
        all += dps
        if (it.trueRange >= range)
            inRange += dps
    }

    return if (all != 0f) inRange / all else 0f
}

fun ShipAPI.dpsAtRange(range: Float): Float {
    return primaryWeapons.filter { it.trueRange >= range }.sumOf { it.derivedStats.dps.toDouble() }.toFloat()
}

fun ShipAPI.shortestRotationToTarget(target: Vector2f): Float {
    val facingToTarget = (target - location).getFacing()
    return MathUtils.getShortestRotation(facing, facingToTarget)
}

val ShipAPI.strafeAcceleration: Float
    get() = this.acceleration * when (this.hullSize) {
        ShipAPI.HullSize.FIGHTER -> 0.75f
        ShipAPI.HullSize.FRIGATE -> 1.0f
        ShipAPI.HullSize.DESTROYER -> 0.75f
        ShipAPI.HullSize.CRUISER -> 0.5f
        ShipAPI.HullSize.CAPITAL_SHIP -> 0.25f
        else -> 1.0f
    }