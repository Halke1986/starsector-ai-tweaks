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

class FiringSolution(
    val weapon: WeaponAPI,
    val target: CombatEntityAPI,
) {
    /** where the weapon should aim to hit centerpoint of the target */
    val intercept: Vector2f

    /** was it possible to calculate the firing solution */
    val valid: Boolean

    /** does the weapon have sufficient range and can rotate in its slot
     * to hit the target, taking target movement into account */
    val canTrack: Boolean

    init {
        val p = target.location - weapon.location
        val v = (target.velocity - weapon.ship.velocity) / weapon.projectileSpeed

        val interceptDistance = solve(p, v, 0f, 1f, 0f)
        val closestDistance = solve(p, v, target.radius, 1f, cos180)

        valid = !interceptDistance.isNaN() && interceptDistance.isFinite()
        intercept = if (valid) target.location + v * interceptDistance else Vector2f()

        canTrack = valid && closestDistance <= weapon.range && arcsOverlap(
            Arc(weapon.arc, weapon.absoluteArcFacing), interceptArc(weapon, target)
        )
    }
}

class HitSolver(val weapon: WeaponAPI) {
    private val weaponUnitVector = unitVector(weapon.currAngle)

    private fun pv(target: CombatEntityAPI): Pair<Vector2f, Vector2f> = Pair(
        weapon.location - target.location,
        weaponUnitVector + (weapon.ship.velocity - target.velocity) / weapon.projectileSpeed,
    )

    /** calculates if the weapon projectile will collide
     * with target circumference; ignores weapon range */
    fun willHit(target: CombatEntityAPI): Boolean {
        val (p, v) = pv(target)
        return distanceToOriginSqr(p, v) <= target.radius * target.radius
    }

    fun willHitSpread(target: CombatEntityAPI): Boolean =
        arcsOverlap(Arc(weapon.currSpread, weapon.currAngle), interceptArc(weapon, target))

    /** calculates the range at which projectile will collide
     * with target circumference; null if no collision;
     * more expensive than willHit method */
    fun hitRange(target: CombatEntityAPI): Float? {
        val (p, v) = pv(target)
        val range = solve(p, v, target.radius, 0f, 0f)
        return if (range <= weapon.range) range else null
    }

    fun willHitBounds(target: CombatEntityAPI): Boolean {
        // TODO implement custom bounds check
        val (_, v) = pv(target)
        return CollisionUtils.getCollisionPoint(weapon.location, weapon.location + v * 10e5f, target) != null
    }
}

fun interceptArc(weapon: WeaponAPI, target: CombatEntityAPI): Arc {
    val p = target.location - weapon.location
    val v = (target.velocity - weapon.ship.velocity) / weapon.projectileSpeed

    val tangentDistance = solve(p, v, target.radius, 1f, cos90)
    return if (tangentDistance.isNaN()) Arc(360f, 0f)
    else Arc(
        arc = atan(target.radius / tangentDistance) * 2f,
        facing = VectorUtils.getFacing(p + v * tangentDistance),
    )
}