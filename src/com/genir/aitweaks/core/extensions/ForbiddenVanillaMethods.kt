package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

/** Vanilla ShipAPI.fluxLevel May return NaN for ships
 * without flux bar, like armor modules. */
val ShipAPI.FluxLevel: Float
    get() {
        val level = fluxLevel
        return if (level.isNaN()) 0f else level
    }

val WeaponAPI.Range: Float
    get() = range + barrelOffset
