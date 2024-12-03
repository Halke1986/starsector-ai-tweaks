package com.genir.aitweaks.core.features.shipai.autofire.ballistics

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.Rotation.Companion.rotated
import com.genir.aitweaks.core.utils.extensions.*
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max

class ProjectilePD(weapon: WeaponAPI) : Ballistics(weapon) {
    private val fallback: Projectile = Projectile(weapon)

    private var targetEntity: CombatEntityAPI? = null
    private var adjustedIntercept: Vector2f? = null
    private var adjustedArc: Arc = Arc(0f, 0f)

    private var prevWeaponLocation: Vector2f = Vector2f()
    private var prevWeaponVelocity: Vector2f = Vector2f()
    private var prevIntercept: Vector2f? = null

    fun advance(dt: Float, target: Target?, params: BallisticParams) {
        // No valid target.
        if (target == null || !target.entity.isPDTarget) {
            targetEntity = null
            adjustedIntercept = null
            prevIntercept = null
            return
        }

        // Target has changed, reset history.
        if (targetEntity != target.entity) {
            targetEntity = target.entity
            adjustedIntercept = fallback.intercept(target, params)
            prevIntercept = adjustedIntercept
            return
        }

        val simpleIntercept = fallback.intercept(target, params)
        val simpleArc = fallback.interceptArc(target, params)

        val improvedIntercept = improvedPrevIntercept(dt, target, params)
        val errAngle = shortestRotation(prevIntercept!!.facing, improvedIntercept.facing)
        val errDist = prevIntercept!!.length - (improvedIntercept.length)

        val timeFixed = simpleIntercept.length / (weapon.trueProjectileSpeed * dt + errDist)
        val angleFix = timeFixed * errAngle

        adjustedIntercept = simpleIntercept.rotated(Rotation(angleFix))
        adjustedArc = Arc(simpleArc.angle, simpleArc.facing + angleFix)

        prevIntercept = simpleIntercept
        prevWeaponLocation = weapon.location.copy
        prevWeaponVelocity = weapon.ship.velocity.copy
    }

    override fun closestHitRange(target: Target, params: BallisticParams): Float {
        if (target.entity != targetEntity) return fallback.closestHitRange(target, params)

        return max(0f, adjustedIntercept!!.length - (target.radius + weapon.barrelOffset))
    }

    override fun intercept(target: Target, params: BallisticParams): Vector2f {
        if (target.entity != targetEntity) return fallback.intercept(target, params)

        return adjustedIntercept!!
    }

    override fun interceptArc(target: Target, params: BallisticParams): Arc {
        if (target.entity != targetEntity) return fallback.interceptArc(target, params)

        return adjustedArc
    }

    override fun willHitCircumference(target: Target, params: BallisticParams): Float? {
        if (target.entity != targetEntity) return fallback.willHitCircumference(target, params)

        return if (adjustedArc.contains(weapon.currAngle)) closestHitRange(target, params)
        else null
    }

    override fun willHitShield(target: ShipAPI, params: BallisticParams): Float? {
        return fallback.willHitShield(target, params)
    }

    override fun willHitBounds(target: ShipAPI, params: BallisticParams): Float? {
        return fallback.willHitBounds(target, params)
    }

    private fun improvedPrevIntercept(dt: Float, target: Target, params: BallisticParams): Vector2f {
        val vAbs = (target.velocity - prevWeaponVelocity)
        val pAbs = (target.location - prevWeaponLocation)

        val p = pAbs + vAbs * (params.delay)
        val v = vAbs / (weapon.trueProjectileSpeed * params.accuracy)
        if (targetAboveWeapon(p, weapon, target)) return target.location

        val alreadyTravelled = weapon.trueProjectileSpeed * dt
        val range = solve(Pair(p, v), weapon.barrelOffset + alreadyTravelled, 1f, 0f, 0f) ?: 1e7f
        return p + v * range
    }
}
