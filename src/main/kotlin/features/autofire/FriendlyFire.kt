package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.distanceToOriginSqr
import com.genir.aitweaks.utils.extensions.absoluteProjectileVelocity
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

fun firstAlongLineOfFire(weapon: WeaponAPI, radius: Float): ShipAPI? {
    val searchRange = radius * 2.0f + 50.0f // Magic numbers based on vanilla autofire AI.
    val grid = Global.getCombatEngine().shipGrid
    val shipIterator = grid.getCheckIterator(weapon.location, searchRange, searchRange)
    val projectileVelocity = weapon.absoluteProjectileVelocity

    var closestShip: ShipAPI? = null
    var closestRange = Float.MAX_VALUE

    val evaluateShip = fun(ship: ShipAPI) {
        if (ship.isFighter || ship.isDrone || ship == weapon.ship) return
        if (!willHit(projectileVelocity, weapon, ship)) return

        val range = (weapon.location - ship.location).lengthSquared()
        if (range < closestRange) {
            closestShip = ship
            closestRange = range
        }
    }

    shipIterator.forEach { evaluateShip(it as ShipAPI) }

    return closestShip
}

fun willHit(projectileVelocity: Vector2f, weapon: WeaponAPI, ship: ShipAPI): Boolean {
    val p = weapon.location - ship.location
    val v = projectileVelocity - ship.velocity
    val distanceSqr = distanceToOriginSqr(p, v) ?: return false
    return distanceSqr <= ship.collisionRadius * ship.collisionRadius
}