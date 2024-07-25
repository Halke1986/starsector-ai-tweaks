package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.extensions.isFrontFacing
import com.genir.aitweaks.core.utils.extensions.isModule
import com.genir.aitweaks.core.utils.extensions.isPD
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max

/** Weapon range from the center of the ship. */
val WeaponAPI.slotRange: Float
    get() = this.range + this.slot.location.x

val WeaponAPI.reloadTimeRemaining: Float
    get() {
        // Weapon does not use ammo, or has ammo to spare.
        if (!usesAmmo() || ammo > 0) return cooldownRemaining

        val tracker = ammoTracker!!

        // Weapon is permanently out if ammo.
        if (tracker.ammoPerSecond <= 0f) return Float.MAX_VALUE

        val reloadTime = tracker.reloadProgress * tracker.reloadSize / tracker.ammoPerSecond
        return max(reloadTime, cooldownRemaining)
    }

val ShipAPI.significantWeapons: List<WeaponAPI>
    get() = this.allWeapons.filter { weapon ->
        when {
            weapon.slot.isHidden -> false
            weapon.slot.isDecorative -> false
            weapon.slot.isSystemSlot -> false
            weapon.slot.isStationModule -> false
            weapon.type == WeaponAPI.WeaponType.MISSILE -> false
            weapon.isPD -> false
            weapon.isPermanentlyDisabled -> false
            weapon.derivedStats.dps == 0f -> false
            weapon.reloadTimeRemaining >= Preset.weaponMaxReloadTime -> false
            else -> true
        }
    }

/** Front facing significant weapons. */
val ShipAPI.primaryWeapons: List<WeaponAPI>
    get() = significantWeapons.filter { it.isFrontFacing }

/** Range at which all primary weapons can hit. */
val ShipAPI.minRange: Float
    get() = primaryWeapons.minOfOrNull { it.slotRange } ?: 0f

/** Range at which at least one primary weapon can hit. */
val ShipAPI.maxRange: Float
    get() = primaryWeapons.maxOfOrNull { it.slotRange } ?: 0f

/** Range at which the ship can deliver at least
 * `dpsFraction` of its primary weapons DPS. */
fun ShipAPI.effectiveRange(dpsFraction: Float): Float {
    val weapons = primaryWeapons
    val dps = weapons.sumOf { it.derivedStats.dps.toDouble() }.toFloat()

    if (dps == 0f) return 0f

    var dpsInRange = dps
    weapons.sortedWith(compareBy { it.slotRange }).forEach { weapon ->
        dpsInRange -= weapon.derivedStats.dps
        if (dpsInRange / dps <= dpsFraction) {
            return weapon.slotRange
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
        if (it.slotRange >= range) inRange += dps
    }

    return if (all != 0f) inRange / all else 0f
}

fun ShipAPI.dpsAtRange(range: Float): Float {
    return primaryWeapons.filter { it.slotRange >= range }.sumOf { it.derivedStats.dps.toDouble() }.toFloat()
}

fun ShipAPI.shortestRotationToTarget(target: Vector2f, broadsideFacing: Float): Float {
    val facingToTarget = (target - location).getFacing()
    return MathUtils.getShortestRotation(facing + broadsideFacing, facingToTarget)
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

/** Collision radius encompassing an entire modular ship, including drones. */
val ShipAPI.totalCollisionRadius: Float
    get() {
        val modules = childModulesCopy.filter { it.isModule } // Make sure the module is still attached.
        val drones = deployedDrones?.filter { it.collisionClass == CollisionClass.SHIP }

        val withModules = modules.maxOfOrNull { (location - it.location).length() + it.collisionRadius } ?: 0f
        val withDrones = drones?.maxOfOrNull { (location - it.location).length() + it.collisionRadius } ?: 0f

        return max(collisionRadius, max(withDrones, withModules))
    }

fun ShipAPI.command(cmd: ShipCommand) = this.giveCommand(cmd, null, 0)
