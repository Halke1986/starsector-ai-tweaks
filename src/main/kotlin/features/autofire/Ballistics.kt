package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.extensions.absoluteArcFacing
import com.genir.aitweaks.utils.extensions.radius
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

class FiringSolution(
    val weapon: WeaponAPI,
    val target: CombatEntityAPI,
) {
    /** where the weapon should aim to hit centerpoint of the target */
    val intercept: Vector2f

    /** was it possible to calculate the firing solution */
    val valid: Boolean

    /** does the weapon have sufficient range and can rotate in its slot to hit the target */
    val canTrack: Boolean

    init {
        val locationRelative = target.location - weapon.location
        val velocityRelative = (target.velocity - weapon.ship.velocity) / weapon.projectileSpeed

        val interceptDistance = solve(locationRelative, velocityRelative, 0f, 1f)
        valid = !interceptDistance.isNaN() && interceptDistance.isFinite() && interceptDistance >= 0f
        intercept = target.location + velocityRelative * interceptDistance

        val interceptRelative = intercept - weapon.location
        val interceptArc = angularSize(interceptRelative.lengthSquared(), target.radius)
        val interceptFacing = VectorUtils.getFacing(interceptRelative)
        val closestPossibleHit =
            if (interceptArc == 360f) 0f else solve(locationRelative, velocityRelative, target.radius, 1f)

        canTrack = valid && closestPossibleHit <= weapon.range && arcsOverlap(
            weapon.absoluteArcFacing, weapon.arc, interceptFacing, interceptArc
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

    /** calculates the range at which projectile will collide
     * with target circumference; null if no collision;
     * more expensive than willHit method */
    fun hitRange(target: CombatEntityAPI): Float? {
        val (p, v) = pv(target)
        val range = solve(p, v, target.radius, 0f)
        return if (range <= weapon.range) range else null
    }
}