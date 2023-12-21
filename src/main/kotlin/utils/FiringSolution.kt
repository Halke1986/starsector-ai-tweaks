package com.genir.aitweaks.utils

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.debugValue
import com.genir.aitweaks.utils.extensions.absoluteArcFacing
import com.genir.aitweaks.utils.extensions.isAnyBeam
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.sqrt

class FiringSolution(
    val weapon: WeaponAPI,
    val target: CombatEntityAPI,
    val intercept: Vector2f,
    val estimateHitRange: Float, // rough estimate
    val facing: Float,
    val arc: Float,
) {
    val canTrack: Boolean
        get() = arcsOverlap(weapon.absoluteArcFacing, weapon.arc, facing, arc) && estimateHitRange <= weapon.range

    val willHit: Boolean
        get() = arcsOverlap(weapon.currAngle, 0f, facing, arc)

}

fun calculateFiringSolution(weapon: WeaponAPI, target: CombatEntityAPI?): FiringSolution? {
    if (target == null) return null

    val relativeLocation = target.location - weapon.location
    val relativeVelocity = target.velocity - weapon.ship.velocity

    // Assume infinite projectile speed for beam weapons.
    // This results in no target lead.
    val travelTime = if (weapon.isAnyBeam) 0f
    else intersectionTime(relativeLocation, relativeVelocity, 0f, weapon.projectileSpeed) ?: return null
    val relativeIntercept = relativeLocation + relativeVelocity.times(travelTime)
    val interceptRange = relativeIntercept.length()

    // Target is directly over the weapon.
    if (interceptRange < target.collisionRadius) {
        return FiringSolution(weapon, target, target.location, 0f, 0f, 360f)
    }

    val tangent = sqrt(interceptRange * interceptRange - target.collisionRadius * target.collisionRadius)
    val intercept = relativeIntercept + weapon.location
    val estimateHitRange = interceptRange - target.collisionRadius
    val facing = VectorUtils.getFacing(relativeIntercept)
    val arc = atan(target.collisionRadius / tangent) * 2f

    if (facing.isNaN() || arc.isNaN()) {
        debugValue = Triple(weapon.spec.weaponName, facing, arc)
    }

    return FiringSolution(weapon, target, intercept, estimateHitRange, facing, arc)
}

