package com.genir.aitweaks

import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.FastTrig
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.rotate

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