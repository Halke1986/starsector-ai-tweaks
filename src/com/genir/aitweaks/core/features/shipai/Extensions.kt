package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.extensions.isModule
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max

/** Weapon range from the center of the ship. */
val WeaponAPI.slotRange: Float
    get() = this.range + this.slot.location.x

val WeaponAPI.maxReloadTime: Float
    get() {
        // Weapon does not use ammo, or has ammo to spare.
        if (!usesAmmo()) return cooldown

        // Weapon is permanently out if ammo.
        val tracker = ammoTracker!!
        if (tracker.ammoPerSecond <= 0f) return Float.MAX_VALUE

        val reloadTime = tracker.reloadSize / tracker.ammoPerSecond
        return max(reloadTime, cooldown)
    }

val WeaponAPI.reloadTimeRemaining: Float
    get() {
        // Weapon does not use ammo, or has ammo to spare.
        if (!usesAmmo() || ammo > 0) return cooldownRemaining

        // Weapon is permanently out if ammo.
        val tracker = ammoTracker!!
        if (tracker.ammoPerSecond <= 0f) return Float.MAX_VALUE

        val reloadTime = (1f - tracker.reloadProgress) * tracker.reloadSize / tracker.ammoPerSecond
        return max(reloadTime, cooldownRemaining)
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
