package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.extensions.absoluteArcFacing
import com.genir.aitweaks.utils.extensions.radius
import com.genir.aitweaks.utils.extensions.velocity
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus

class FiringSolution(
    val weapon: WeaponAPI,
    val target: CombatEntityAPI,
) {
    private val locationRelative = target.location - weapon.location
    private val velocityRelative = (target.velocity - weapon.velocity) / weapon.projectileSpeed

    private val interceptTimeScaled = solve(locationRelative, velocityRelative, 0f, 1f)
    val valid = !interceptTimeScaled.isNaN() && interceptTimeScaled.isFinite() && interceptTimeScaled >= 0f
    val intercept = target.location + velocityRelative * interceptTimeScaled
    val interceptTime = interceptTimeScaled / weapon.projectileSpeed

    private val interceptRelative = intercept - weapon.location
    val interceptArc = angularSize(interceptRelative.lengthSquared(), target.radius)
    val interceptFacing = VectorUtils.getFacing(interceptRelative)

    val closestPossibleHit =
        if (interceptArc == 360f) 0f else solve(locationRelative, velocityRelative, target.radius, 1f)

    // TODO debug nans

    /**
     * canTrack returns 'true' if it is possible to aim the weapon at selected target,
     * taking into account weapon slot firing arc, weapon range and target predicted
     * intercept location and target angular dimension.
     */
    val canTrack: Boolean
        get() = valid && closestPossibleHit <= weapon.range && arcsOverlap(
            weapon.absoluteArcFacing, weapon.arc, interceptFacing, interceptArc
        )

    val willHit: Boolean
        get() {
            if (!valid || !arcsOverlap(weapon.currAngle, 0f, interceptFacing, interceptArc)) return false

            val locationRelative = weapon.location - target.location
            val velocityRelative = unitVector(weapon.currAngle) - target.velocity / weapon.projectileSpeed
            return solve(locationRelative, velocityRelative, target.radius, 0f) <= weapon.range
        }
}

