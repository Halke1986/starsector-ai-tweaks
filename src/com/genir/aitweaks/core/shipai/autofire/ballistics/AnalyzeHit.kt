package com.genir.aitweaks.core.shipai.autofire.ballistics

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.utils.Bounds
import com.genir.aitweaks.core.utils.solve
import com.genir.aitweaks.core.utils.types.LinearMotion

/** Analyze the potential collision between a perfectly accurate projectile fired from the weapon and the target.
 * Null if no collision. */
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

/** Analyze the potential collision between the projectile and the target.
 * Null if no collision. */
fun analyzeHit(projectile: DamagingProjectileAPI, target: CombatEntityAPI): Hit? {
    val projectileMotion = projectile.linearMotion - target.linearMotion

    return analyzeHit(projectileMotion, target)
}

/** Calculates if a perfectly accurate projectile fired from the weapon will collide with the target circumference.
 * Collision range is returned; null if no collision. */
fun willHitCircumference(weapon: WeaponHandle, target: CombatEntityAPI, params: BallisticParams): Float? {
    val projectileMotion = predictProjectileMotion(weapon, target.linearMotion, params)
    val ballisticTarget = BallisticTarget.collisionRadius(target)

    return willHitCircumference(projectileMotion, ballisticTarget)?.let { range ->
        range + weapon.projectileSpawnOffset
    }
}

/** Calculates if the projectile will collide with the target circumference.
 * Collision range is returned; null if no collision. */
fun willHitCircumference(projectile: DamagingProjectileAPI, target: CombatEntityAPI): Float? {
    val projectileMotion = projectile.linearMotion - target.linearMotion
    val ballisticTarget = BallisticTarget.collisionRadius(target)

    return willHitCircumference(projectileMotion, ballisticTarget)
}

/** Calculates if a perfectly accurate projectile fired from the weapon will collide with the target shield.
 * Will not detect hits to inside of shield.
 * Collision range is returned; null if no collision. */
fun willHitShield(weapon: WeaponHandle, target: ShipAPI, params: BallisticParams): Float? {
    val projectileMotion = predictProjectileMotion(weapon, target.linearMotion, params)

    return willHitShield(projectileMotion, target)?.let { range ->
        range + weapon.projectileSpawnOffset
    }
}

/** Calculates if the projectile will collide with the target shield.
 * Collision range is returned; null if no collision. */
fun willHitShield(projectile: DamagingProjectileAPI, target: CombatEntityAPI): Float? {
    val projectileMotion = projectile.linearMotion - target.linearMotion

    return willHitShield(projectileMotion, target)
}

/** Calculates if a perfectly accurate projectile fired from the weapon will collide with the target bounds.
 * Collision range is returned; null if no collision. */
fun willHitBounds(weapon: WeaponHandle, target: CombatEntityAPI, params: BallisticParams): Float? {
    val projectileMotion = predictProjectileMotion(weapon, target.linearMotion, params)

    return willHitBounds(projectileMotion, target)?.let { range ->
        range + weapon.projectileSpawnOffset
    }
}

/** Calculates if the projectile will collide with the target bounds.
 * Collision range is returned; null if no collision. */
fun willHitBounds(projectile: DamagingProjectileAPI, target: CombatEntityAPI): Float? {
    val projectileMotion = projectile.linearMotion - target.linearMotion

    return willHitBounds(projectileMotion, target)
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

private fun willHitCircumference(projectileMotion: LinearMotion, target: BallisticTarget): Float? {
    return solve(projectileMotion, target.radius)?.smallerNonNegative
}

private fun willHitShield(projectileMotion: LinearMotion, target: CombatEntityAPI): Float? {
    when {
        !target.isShip -> return null

        !target.hasShield -> return null

        target.shield.isOff -> return null
    }

    val shield = target.shield
    val projectileFlightDistance = solve(projectileMotion, shield.radius)?.smallerNonNegative
        ?: return null

    val hitPoint = projectileMotion.positionAfter(projectileFlightDistance)

    return if (shield.isHit(hitPoint)) {
        projectileFlightDistance
    } else {
        null
    }
}

private fun willHitBounds(projectileMotion: LinearMotion, target: CombatEntityAPI): Float? {
    return Bounds.collision(projectileMotion.position, projectileMotion.velocity, target)
}

/** Predicted projectile location and velocity in target frame of reference.
 * Assumes a perfectly accurate weapon.
 * weapon.projectileSpeed is used as the unit of velocity.*/
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
