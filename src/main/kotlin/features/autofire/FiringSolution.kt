package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.debugValue
import com.genir.aitweaks.utils.arcsOverlap
import com.genir.aitweaks.utils.atan
import com.genir.aitweaks.utils.extensions.absoluteArcFacing
import com.genir.aitweaks.utils.extensions.isAnyBeam
import com.genir.aitweaks.utils.solve
import com.genir.aitweaks.utils.times
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.sqrt

fun makeFiringSolution(weapon: WeaponAPI, target: CombatEntityAPI?): FiringSolution? {
    if (target == null) return null

    val intercept = calculateIntercept(weapon, target) ?: return null

    return FiringSolution(weapon, target, intercept)
}

class FiringSolution(
    val weapon: WeaponAPI,
    val target: CombatEntityAPI,
    val intercept: Vector2f,
) {
    private val interceptFacing: Float
    private val interceptArc: Float
    val closestPossibleHit: Float

    init {
        val relativeIntercept = intercept - weapon.ship.location
        val interceptRangeSqr = relativeIntercept.lengthSquared()
        val targetRadiusSqr = target.collisionRadius * target.collisionRadius

        if (interceptRangeSqr < targetRadiusSqr) {
            // Target is directly over the weapon.
            interceptFacing = 0f
            interceptArc = 360f
            closestPossibleHit = 0f
        } else {
            val interceptTangent = sqrt(interceptRangeSqr - targetRadiusSqr)

            interceptArc = atan(target.collisionRadius / interceptTangent) * 2f
            interceptFacing = VectorUtils.getFacing(relativeIntercept)
            closestPossibleHit = calculateClosestPossibleHit(weapon, target)!!
        }

        debug()
    }

    /**
     * canTrack returns 'true' if it is possible to aim the weapon at selected target,
     * taking into account weapon slot firing arc, weapon range and target predicted
     * intercept location and target angular dimension.
     */
    val canTrack: Boolean
        get() = arcsOverlap(
            weapon.absoluteArcFacing, weapon.arc, interceptFacing, interceptArc
        ) && closestPossibleHit <= weapon.range


    val willHit: Boolean
        get() = arcsOverlap(weapon.currAngle, 0f, interceptFacing, interceptArc)

    private fun debug() {
        if (interceptFacing.isNaN() || interceptArc.isNaN() || closestPossibleHit.isNaN()) {
            data class SolutionDebug(
                val name: String, val intercept: Vector2f, val facing: Float, val arc: Float, val range: Float
            )
            debugValue =
                SolutionDebug(weapon.spec.weaponName, intercept, interceptFacing, interceptArc, closestPossibleHit)
        }
    }
}

/**
 * Calculate the point at which weapon should aim to hit
 * center point of a moving target.
 */
fun calculateIntercept(weapon: WeaponAPI, target: CombatEntityAPI): Vector2f? {
    // Assume infinite projectile speed for beam weapons, resulting in no target lead.
    if (weapon.isAnyBeam) return target.location

    val relativeLocation = target.location - weapon.location
    val relativeVelocity = target.velocity - weapon.ship.velocity

    val travelTime = solve(relativeLocation, relativeVelocity, 0f, weapon.projectileSpeed) ?: return null
    return target.location + relativeVelocity.times(travelTime)
}

/**
 * Calculate the closest possible range at which projectile
 * or beam may intersect collision radius of a moving target.
 */
fun calculateClosestPossibleHit(weapon: WeaponAPI, target: CombatEntityAPI): Float? {
    val relativeLocation = weapon.location - target.location
    val relativeVelocity = target.velocity - weapon.ship.velocity

    // Assume infinite projectile speed for beam weapons, resulting in stationary target.
    if (weapon.isAnyBeam) return relativeLocation.length() - target.collisionRadius

    val travelTimeDebug = solve(
        relativeLocation, relativeVelocity, target.collisionRadius, weapon.projectileSpeed
    )
    if (travelTimeDebug == null) {
        val log = Global.getLogger(weapon.javaClass)
        log.info(relativeLocation)
        log.info(relativeVelocity)
        log.info(target.collisionRadius)
        log.info(weapon.projectileSpeed)
    }

    val travelTime = solve(
        relativeLocation, relativeVelocity, target.collisionRadius, weapon.projectileSpeed
    ) ?: return null
    return weapon.projectileSpeed * travelTime
}

//fun calculateHit(weapon: WeaponAPI, target: CombatEntityAPI): Vector2f? {
//    val relativeLocation = weapon.location - target.location
//    val relativeVelocity = target.velocity - weapon.ship.velocity
//}