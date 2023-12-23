package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.extensions.absoluteArcFacing
import com.genir.aitweaks.utils.extensions.radius
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus

class FiringSolution(
    val weapon: WeaponAPI,
    val target: CombatEntityAPI,
) {
    private val locationRelative = target.location - weapon.location
    private val velocityRelative = (target.velocity - weapon.ship.velocity) / weapon.projectileSpeed

    private val interceptDistance = solve(locationRelative, velocityRelative, 0f, 1f)
    val valid = !interceptDistance.isNaN() && interceptDistance.isFinite() && interceptDistance >= 0f
    val intercept = target.location + velocityRelative * interceptDistance
    val interceptTime = interceptDistance / weapon.projectileSpeed

    private val interceptRelative = intercept - weapon.location
    val interceptArc = angularSize(interceptRelative.lengthSquared(), target.radius)
    val interceptFacing = VectorUtils.getFacing(interceptRelative)

    val closestPossibleHit =
        if (interceptArc == 360f) 0f else solve(locationRelative, velocityRelative, target.radius, 1f)

    /**
     * canTrack returns 'true' if it is possible to aim the weapon at selected target,
     * taking into account weapon slot firing arc, weapon range and target predicted
     * intercept location and target angular dimension.
     */
    val canTrack: Boolean
        get() = valid && closestPossibleHit <= weapon.range && arcsOverlap(
            weapon.absoluteArcFacing, weapon.arc, interceptFacing, interceptArc
        )
}

