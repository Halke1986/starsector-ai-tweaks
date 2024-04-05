package com.genir.aitweaks.features.maneuver

import com.fs.starfarer.api.combat.ShipAPI

class Maneuver(val ship: ShipAPI, val target: ShipAPI) {
    fun advance(dt: Float) {}

    val isDirectControl: Boolean = true

    fun doManeuver() {}

    val desiredHeading: Float = Float.MAX_VALUE

    val desiredFacing: Float = Float.MAX_VALUE
}
