package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.extensions.absoluteArcFacing
import com.genir.aitweaks.utils.extensions.radius
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

const val cos90 = 0f
const val cos180 = -1f

/** Does the weapon have sufficient range and can rotate in its slot to hit the target. */
fun canTrack(weapon: WeaponAPI, target: CombatEntityAPI): Boolean {
    val (p, v) = targetCoords(weapon, target)
    val closestDistance = solve(p, v, target.radius, 1f, cos180)

    return closestDistance <= weapon.range && arcsOverlap(
        Arc(weapon.arc, weapon.absoluteArcFacing), interceptArc(p, v, target)
    )
}

/** Weapon aim offset from the target position, required to hit centerpoint of the moving target.
 * Vector2f(NaN,Nan) if the target is faster than the projectile. */
fun interceptOffset(weapon: WeaponAPI, target: CombatEntityAPI): Vector2f {
    val (p, v) = targetCoords(weapon, target)
    return v * solve(p, v, 0f, 1f, 0f)
}

/** The range at which a perfectly accurate projectile will collide with target circumference.
 * NaN if no collision. */
fun hitRange(weapon: WeaponAPI, target: CombatEntityAPI): Float {
    val (p, v) = projectileCoords(weapon, target)
    return solve(p, v, target.radius, 0f, 0f)
}

/** Calculates if a perfectly accurate projectile will collide with target circumference,
 * ignoring weapon range. */
fun willHit(weapon: WeaponAPI, target: CombatEntityAPI): Boolean {
    val (p, v) = projectileCoords(weapon, target)
    return distanceToOriginSqr(p, v) <= target.radius * target.radius
}

/** Calculates if an inaccurate projectile may collide with target circumference,
 * ignoring weapon range. */
fun willSpreadHit(weapon: WeaponAPI, target: CombatEntityAPI): Boolean {
    val (p, v) = targetCoords(weapon, target)
    return arcsOverlap(Arc(weapon.currSpread, weapon.currAngle), interceptArc(p, v, target))
}

/** Calculates if a perfectly accurate projectile will collide with target bounds,
 * ignoring weapon range. */
fun willHitBounds(weapon: WeaponAPI, target: CombatEntityAPI): Boolean {
    // TODO implement custom bounds check
    val (_, v) = projectileCoords(weapon, target)
    return CollisionUtils.getCollisionPoint(weapon.location, weapon.location + v * 10e5f, target) != null
}

/** Target location and velocity in projectile frame of reference. */
private fun targetCoords(weapon: WeaponAPI, target: CombatEntityAPI) = Pair(
    target.location - weapon.location,
    (target.velocity - weapon.ship.velocity) / weapon.projectileSpeed,
)

/** Projectile location and velocity in target frame of reference. */
private fun projectileCoords(weapon: WeaponAPI, target: CombatEntityAPI) = Pair(
    weapon.location - target.location,
    unitVector(weapon.currAngle) + (weapon.ship.velocity - target.velocity) / weapon.projectileSpeed,
)

private fun interceptArc(p: Vector2f, v: Vector2f, target: CombatEntityAPI): Arc {
    val tangentDistance = solve(p, v, target.radius, 1f, cos90)

    // Target is directly over the weapon.
    if (tangentDistance.isNaN()) Arc(360f, 0f)

    return Arc(
        arc = atan(target.radius / tangentDistance) * 2f,
        facing = VectorUtils.getFacing(p + v * tangentDistance),
    )
}