package com.genir.aitweaks

import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.FastTrig
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import kotlin.math.cos
import kotlin.math.sin

fun willHitShield(weapon: WeaponAPI, target: ShipAPI?) = when {
    target == null -> false
    target.shield == null -> false
    target.shield.isOff -> false
    else -> willHitActiveShieldArc(weapon, target.shield)
}

fun willHitActiveShieldArc(weapon: WeaponAPI, shield: ShieldAPI): Boolean {
    val r = (weapon.location - shield.location).rotate(-shield.facing)
    val attackAngle = Math.toDegrees(FastTrig.atan2(r.y.toDouble(), r.x.toDouble()))
    return kotlin.math.abs(attackAngle) < (shield.activeArc / 2)
}

class Rotation(radians: Float) {
    private val sinf = sin(radians)
    private val cosf = cos(radians)

    fun rotate(v: Vector2f): Vector2f = Vector2f(
        v.x * cosf - v.y * sinf,
        v.x * sinf + v.y * cosf,
    )

    fun rotateAround(v: Vector2f, pivot: Vector2f): Vector2f {
        val a = v.x - pivot.x
        val b = v.y - pivot.y
        return Vector2f(
            a * cosf - b * sinf + pivot.x,
            a * sinf + b * cosf + pivot.y,
        )
    }

}
