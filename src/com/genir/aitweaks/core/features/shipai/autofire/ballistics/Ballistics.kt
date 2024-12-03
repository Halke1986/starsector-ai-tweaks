package com.genir.aitweaks.core.features.shipai.autofire.ballistics

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.Arc
import org.lwjgl.util.vector.Vector2f

abstract class Ballistics(val weapon: WeaponAPI) {
    abstract fun closestHitRange(target: Target, params: BallisticParams): Float

    abstract fun intercept(target: Target, params: BallisticParams): Vector2f

    abstract fun canTrack(target: Target, params: BallisticParams, rangeOverride: Float? = null): Boolean

    abstract fun interceptArc(target: Target, params: BallisticParams): Arc

    abstract fun willHitCircumference(target: Target, params: BallisticParams): Float?

    abstract fun willHitShield(target: ShipAPI, params: BallisticParams): Float?

    abstract fun willHitBounds(target: ShipAPI, params: BallisticParams): Float?

    companion object {
        val WeaponAPI.ballistics: Ballistics
            get() = Projectile(this)
    }
}
