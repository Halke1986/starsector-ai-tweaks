package com.genir.aitweaks.features

import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.getTargetEntity
import com.genir.aitweaks.hasBestTargetLeading
import com.genir.aitweaks.times
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

fun applyTargetLeadAI(ship: ShipAPI) {
    ship.weaponGroupsCopy.forEach { group ->
        val plugins = group.aiPlugins
        for (i in plugins.indices) {
            plugins[i] = TargetLeadAI(plugins[i])
        }
    }
}

class TargetLeadAI(private val basePlugin: AutofireAIPlugin) : AutofireAIPlugin {
    private var prevTarget: CombatEntityAPI? = null
    private var attackTime: Float = 0f
    private var idleTime: Float = 0f

    override fun advance(timeDelta: Float) {
        basePlugin.advance(timeDelta)

        val target = basePlugin.getTargetEntity()
        if (target != null && prevTarget != target) {
            prevTarget = target
            attackTime = 0f
        }

        if (basePlugin.weapon.isFiring) {
            attackTime += timeDelta
            idleTime = 0f
        } else
            idleTime += timeDelta

        if (idleTime >= 3f)
            attackTime = 0f
    }

    override fun getTarget(): Vector2f? {
        val target = basePlugin.getTargetEntity() ?: return null

        // no need to compute stuff for beam or non-aimable weapons
        if (weapon.isBeam || weapon.isBurstBeam) {
            return target.location
        }

        val tgtLocation = target.location - weapon.location
        val tgtVelocity = target.velocity - (weapon.ship?.velocity ?: Vector2f(0.0f, 0.0f))
        val travelT = intersectionTime(tgtLocation, tgtVelocity, 0f, weapon.projectileSpeed)

        return if (travelT == null)
            basePlugin.target
        else
            target.location + tgtVelocity.times(travelT / getAccuracy())
    }

    override fun shouldFire(): Boolean = basePlugin.shouldFire()
    override fun forceOff() = basePlugin.forceOff()
    override fun getTargetShip(): ShipAPI? = basePlugin.targetShip
    override fun getWeapon(): WeaponAPI = basePlugin.weapon
    override fun getTargetMissile(): MissileAPI? = basePlugin.targetMissile

    private fun getAccuracy(): Float {
        if (basePlugin.weapon.hasBestTargetLeading())
            return 1f

        val accBase = basePlugin.weapon.ship.aimAccuracy
        val accBonus = basePlugin.weapon.spec.autofireAccBonus
        return (accBase - (accBonus + attackTime / 15f))
            .coerceAtLeast(1f)
    }
}

/**
 * Compute time after which point p moving with speed dp
 * will intersect circle centered at point 0,0, with initial
 * radius r expanding with speed dr.
 * This is done by solving the following equation for t:
 *
 * r+dr*t = |p+dp*t|
 *
 * The smaller of the two positive solutions is returned.
 * If no positive solution exists, null is returned.
 */
fun intersectionTime(p: Vector2f, dp: Vector2f, r: Float, dr: Float): Float? {
    // the solved equation can be expanded the following way:
    // r + dr * t = |p + dp * t|
    // r + dr * t = sqrt[ (p.x + dp.x * t)^2 + (p.y + dp.y * t)^2 ]
    // (r + dr * t)^2 = (p.x + dp.x * t)^2 + (p.y + dp.y * t)^2
    // 0 = (dp.x^2 + dp.y^2 - dr^2)*t^2 + 2(p.x*dp.x + p.y*dp.y - r*dr)*t + (p.x^2 + p.y^2 - r^2)
    val a = dp.lengthSquared() - dr * dr
    val b = 2f * (p.x * dp.x + p.y * dp.y - r * dr)
    val c = p.lengthSquared() - r * r

    val (t1, t2) = solve(a, b, c) ?: return null

    if (t1 < 0 && t2 < 0) return null
    if (t1 < 0 || t2 < 0) return max(t1, t2)
    return min(t1, t2)
}

/**
 * solve quadratic equation [ax^2 + bx + c = 0] for x.
 */
fun solve(a: Float, b: Float, c: Float): Pair<Float, Float>? {
    val d = b * b - 4f * a * c
    if (d < 0 || (MathUtils.equals(a, 0f) && MathUtils.equals(b, 0f))) {
        return null
    }
    val r = sqrt(d)
    return if (MathUtils.equals(a, 0f)) {
        Pair((2 * c) / (-b), (2 * c) / (-b))
    } else {
        Pair((-b + r) / (2 * a), (-b - r) / (2 * a))
    }
}




