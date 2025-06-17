package com.genir.aitweaks.core.shipai.autofire.ballistics

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.utils.Bounds
import com.genir.aitweaks.core.utils.solve
import com.genir.aitweaks.core.utils.types.LinearMotion

/** Analyzes the potential collision between projectile and target. Null if no collision. */
fun analyzeHit(weapon: WeaponHandle, target: CombatEntityAPI, params: BallisticParams): Hit? {
    val projectileMotion = predictProjectileMotion(weapon, target.linearMotion, params)

    val hit = analyzeHit(projectileMotion, target)
        ?: return null

    return Hit(
        target = hit.target,
        range = hit.range + weapon.projectileSpawnOffset,
        type = hit.type
    )
}

fun analyzeHit(projectile: DamagingProjectileAPI, target: CombatEntityAPI): Hit? {
    val projectileMotion = projectile.linearMotion - target.linearMotion

    return analyzeHit(projectileMotion, target)
}

private fun analyzeHit(projectileMotion: LinearMotion, target: CombatEntityAPI): Hit? {
    // Simple circumference collision is enough for missiles and fighters.
    if (!target.isShip) {
        return willHitCircumference(projectileMotion, BallisticTarget.collisionRadius(target))?.let { hitRange ->
            Hit(target, hitRange, Hit.Type.HULL)
        }
    }

    // Check shield hit.
    willHitShield(projectileMotion, target as ShipAPI)?.let { hitRange ->
        return Hit(target, hitRange, Hit.Type.SHIELD)
    }

    // Check collision radius hit before testing bounds, to increases performance.
    willHitCircumference(projectileMotion, BallisticTarget.collisionRadius(target))
        ?: return null

    // Check bounds hit.
    return willHitBounds(projectileMotion, target)?.let { hitRange ->
        Hit(target, hitRange, Hit.Type.HULL)
    }
}

/** Calculates if a projectile will collide with the target circumference.
 * Collision range is returned, null if no collision. */
private fun willHitCircumference(projectileMotion: LinearMotion, target: BallisticTarget): Float? {
    return solve(projectileMotion, target.radius)?.smallerNonNegative
}

/** Calculates if a perfectly accurate projectile will collide with target shield,
 * given current weapon facing. Will not detect hits to inside of shield.
 * Collision range is returned, null if no collision. */
fun willHitShield(weapon: WeaponHandle, target: ShipAPI, params: BallisticParams): Float? {
    val projectileMotion = predictProjectileMotion(weapon, target.linearMotion, params)

    val range = willHitShield(projectileMotion, target)
        ?: return null

    return range + weapon.projectileSpawnOffset
}

/** Calculates if a projectile will collide with target shield.
 * Will not detect hits to inside of shield.
 * Collision range is returned, null if no collision. */
private fun willHitShield(projectileMotion: LinearMotion, target: ShipAPI): Float? {
    if (!target.hasShield) {
        return null
    }

    val shield = target.shield
        ?: return null

    if (shield.isOff) {
        return null
    }

    val projectileFlightDistance = solve(projectileMotion, shield.radius)?.smallerNonNegative
        ?: return null

    val hitPoint = projectileMotion.positionAfter(projectileFlightDistance)

    return if (shield.isHit(hitPoint)) {
        projectileFlightDistance
    } else {
        null
    }
}

/** Calculates if a perfectly accurate projectile will collide with target bounds,
 * given current weapon facing.
 * Collision range is returned, null if no collision. */
private fun willHitBounds(projectileMotion: LinearMotion, target: ShipAPI): Float? {
    return Bounds.collision(projectileMotion.position, projectileMotion.velocity, target)
}

/** Predicted projectile location and velocity in target frame of reference.
 * Assumes a perfectly accurate weapon.
 * weapon.projectileSpeed is used as velocity unit.*/
private fun predictProjectileMotion(weapon: WeaponHandle, target: LinearMotion, params: BallisticParams): LinearMotion {
    val vAbs = weapon.ship.velocity - target.velocity
    val pAbs = weapon.location - target.position
    val vProj = weapon.facingWhenFiringThisFrame.unitVector

    return LinearMotion(
        position = pAbs + vAbs * params.delay + vProj * weapon.projectileSpawnOffset,
        velocity = vProj + vAbs / (weapon.projectileSpeed * params.accuracy)
    )
}

fun estimateIdealHit(weapon: WeaponHandle, target: CombatEntityAPI, params: BallisticParams): Hit {
    val ballisticTarget = BallisticTarget.shieldRadius(target as ShipAPI)
    val (hitPoint, range) = closestHitInTargetFrameOfReference(weapon, ballisticTarget, params)

    val shieldHit = target.hasShield && target.shield.isHit(hitPoint)
    val hitType = if (shieldHit) Hit.Type.SHIELD else Hit.Type.HULL

    return Hit(target, range, hitType)
}
