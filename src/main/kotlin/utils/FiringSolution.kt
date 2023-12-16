package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.sqrt

class FiringSolution(
    val weapon: WeaponAPI,
    val target: CombatEntityAPI,
    val intercept: Vector2f,
    val facing: Float,
    val arc: Float,
)

fun calculateFiringSolution(weapon: WeaponAPI, target: CombatEntityAPI): FiringSolution? {
    val relativeLocation = target.location - weapon.location
    val relativeVelocity = target.velocity - weapon.ship.velocity

    val travelTime = intersectionTime(relativeLocation, relativeVelocity, 0f, weapon.projectileSpeed) ?: return null
    val relativeIntercept = relativeLocation + relativeVelocity.times(travelTime)

    val radiusSquared = target.collisionRadius * target.collisionRadius
    val tangent = sqrt(relativeIntercept.lengthSquared() - radiusSquared)

    val intercept = relativeIntercept + weapon.location
    val facing = VectorUtils.getFacing(relativeIntercept)
    val arc = atan(target.collisionRadius / tangent) * 2f

    return FiringSolution(weapon, target, intercept, facing, arc)
}
