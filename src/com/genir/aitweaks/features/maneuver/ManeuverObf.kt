@file:Suppress("UNUSED_PARAMETER")

package com.genir.aitweaks.features.maneuver

import com.fs.starfarer.combat.ai.movement.maneuvers.oO0O
import com.fs.starfarer.combat.ai.movement.oOOO
import com.fs.starfarer.combat.entities.Ship
import com.fs.starfarer.combat.o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO.B
import org.lwjgl.util.vector.Vector2f

class Strafe : ManeuverObfBase {
    constructor  (ship: Ship, target: Ship, p2: Boolean, p3: Float, p4: Float, p5: oOOO, p6: oO0O.o, p7: Boolean) : super(ship, target, null)

    constructor  (ship: Ship, target: Ship, p2: Boolean, p3: Float, p4: Float, p5: Float, p6: oOOO, p7: oO0O.o, p8: Boolean) : super(ship, target, null)

    fun Object(p0: Boolean) = maneuver.desiredHeading
}

class Approach(ship: Ship, target: Ship, var3: Float, flockingAI: oOOO, shipAI: oO0O.o) : ManeuverObfBase(ship, target, null)

class Intercept(ship: Ship, target: B, flockingAI: oOOO, shipAI: oO0O.o) : ManeuverObfBase(ship, target as? Ship, null)

class Move(ship: Ship, location: Vector2f, shipAI: oO0O.o) : ManeuverObfBase(ship, null, location)

open class ManeuverObfBase(ship: Ship, target: Ship?, location: Vector2f?) : oO0O {
    protected val maneuver: Maneuver = Maneuver(ship, target, location)

    override fun o00000(p0: Float) = maneuver.advance(p0)

    override fun o00000(): B = maneuver.maneuverTarget as Ship

    override fun Õ00000(): Boolean = true // isDirectControl

    override fun Object() = maneuver.doManeuver()

    override fun Ô00000(): Float = maneuver.desiredHeading

    override fun Ò00000(): Float = maneuver.desiredFacing
}