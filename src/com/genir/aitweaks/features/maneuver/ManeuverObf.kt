package com.genir.aitweaks.features.maneuver

import com.fs.starfarer.combat.ai.movement.maneuvers.oO0O
import com.fs.starfarer.combat.ai.movement.oOOO
import com.fs.starfarer.combat.entities.Ship
import com.fs.starfarer.combat.o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO.B

class ManeuverV : oO0O {
    private val maneuver: Maneuver

    constructor  (ship: Ship, target: Ship, isBadTarget: Boolean, somethingWithStrafing: Float, var5: Float, flockingAI: oOOO, shipAI: oO0O.o, somethingToDoWithVenting: Boolean) {
        maneuver = Maneuver(ship, target)
    }

    constructor  (ship: Ship, target: Ship, isBadTarget: Boolean, somethingWithStrafing: Float, var5: Float, var6: Float, flockingAI: oOOO, shipAI: oO0O.o, somethingToDoWithVenting: Boolean) {
        maneuver = Maneuver(ship, target)
    }

    override fun o00000(p0: Float) = maneuver.advance(p0)

    override fun o00000(): B? = maneuver.target as? Ship

    override fun Õ00000(): Boolean = maneuver.isDirectControl

    override fun Object() = maneuver.doManeuver()

    override fun Ô00000(): Float = maneuver.desiredHeading

    override fun Ò00000(): Float = maneuver.desiredFacing
}

class ManeuverB(ship: Ship, target: Ship, var3: Float, flockingAI: oOOO, shipAI: oO0O.o) : oO0O {
    private val maneuver: Maneuver = Maneuver(ship, target)

    override fun o00000(p0: Float) = maneuver.advance(p0)

    override fun o00000(): B? = maneuver.target as? Ship

    override fun Õ00000(): Boolean = maneuver.isDirectControl

    override fun Object() = maneuver.doManeuver()

    override fun Ô00000(): Float = maneuver.desiredHeading

    override fun Ò00000(): Float = maneuver.desiredFacing
}