package com.genir.aitweaks.core.shipai.systems

import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.extensions.command
import com.genir.aitweaks.core.extensions.isFlamedOut
import com.genir.aitweaks.core.shipai.CustomShipAI
import com.genir.aitweaks.core.shipai.movement.ShipKinematics.Companion.kinematics
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection
import org.lazywizard.lazylib.combat.AIUtils

class ManeuveringJets(ai: CustomShipAI) : SystemAI(ai) {
    private val updateInterval: IntervalUtil = defaultAIInterval()

    override fun advance(dt: Float) {
        updateInterval.advance(dt)

        when {
            !updateInterval.intervalElapsed() -> {
                return
            }

            !AIUtils.canUseSystemThisFrame(ship) -> {
                return
            }

            ship.isFlamedOut -> {
                return
            }

            // Ship intends to move with maximum velocity.
            ai.maneuver.expectedVelocity.length() >= ai.ship.kinematics.maxSpeed * 0.95f -> {
                ship.command(ShipCommand.USE_SYSTEM)
            }

            // Ship intends to make a hard turn.
            (ai.maneuver.expectedFacing - ship.facing.toDirection).length >= 120f -> {
                ship.command(ShipCommand.USE_SYSTEM)
            }
        }
    }
}
