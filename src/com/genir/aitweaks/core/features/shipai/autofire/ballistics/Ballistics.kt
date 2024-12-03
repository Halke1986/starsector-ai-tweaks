package com.genir.aitweaks.core.features.shipai.autofire.ballistics

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.Arc
import com.genir.aitweaks.core.utils.extensions.absoluteArcFacing
import com.genir.aitweaks.core.utils.extensions.barrelOffset
import com.genir.aitweaks.core.utils.extensions.lengthSquared
import com.genir.aitweaks.core.utils.extensions.totalRange
import org.lwjgl.util.vector.Vector2f

abstract class Ballistics(val weapon: WeaponAPI) {
    abstract fun closestHitRange(target: Target, params: BallisticParams): Float

    abstract fun intercept(target: Target, params: BallisticParams): Vector2f

    /** Does the weapon have sufficient range and can rotate in its slot to aim at the target. */
    fun canTrack(target: Target, params: BallisticParams, rangeOverride: Float? = null): Boolean {
        val closestHit = closestHitRange(target, params)
        if (closestHit > (rangeOverride ?: weapon.totalRange)) return false

        val interceptArc = interceptArc(target, params)
        return Arc(weapon.arc, weapon.absoluteArcFacing).overlaps(interceptArc)
    }

    abstract fun interceptArc(target: Target, params: BallisticParams): Arc

    abstract fun willHitCircumference(target: Target, params: BallisticParams): Float?

    abstract fun willHitShield(target: ShipAPI, params: BallisticParams): Float?

    abstract fun willHitBounds(target: ShipAPI, params: BallisticParams): Float?

    /** True if target collision radius is above weapon barrel radius.  */
    protected fun targetAboveWeapon(locationRelative: Vector2f, weapon: WeaponAPI, target: Target): Boolean {
        val d2 = locationRelative.lengthSquared
        val r = weapon.barrelOffset + target.radius
        return d2 < r * r
    }

    companion object {
        val WeaponAPI.ballistics: Ballistics
            get() = Projectile(this)
    }
}
