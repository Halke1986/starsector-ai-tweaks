package com.genir.aitweaks.core.shipai.systems

import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.ACTIVE
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState.IN
import com.genir.aitweaks.core.shipai.CustomShipAI

class BurnDrive(ai: CustomShipAI) : CustomSystemAI(ai) {
    override fun advance(dt: Float) {
        // Prevent vanilla AI from jumping closer to target with
        // BURN_DRIVE, if the target is already within weapons range.
        val rangeBuffer = 200f
        if (ai.attackTarget != null && ai.currentEffectiveRange(ai.attackTarget!!) < ai.attackingGroup.effectiveRange + rangeBuffer) {
            ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM)
        }

        if (ai.ventModule.isBackingOff) {
            ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM)
        }
    }

    override fun holdTargets(): Boolean {
        return system.state == IN || system.state == ACTIVE
    }

    override fun advanceVanillaSystemAI(): Boolean {
        return !ai.ventModule.isBackingOff
    }
}
