package com.genir.aitweaks.core.shipai.systems

import com.fs.starfarer.api.combat.ShipCommand
import com.genir.aitweaks.core.extensions.command
import com.genir.aitweaks.core.shipai.CustomShipAI
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame

class TemporalShell(ai: CustomShipAI) : SystemAI(ai) {
    override fun advance(dt: Float) {
        // Use temporal shell for backing off.
        if (ai.ventModule.isBackingOff && ship.canUseSystemThisFrame()) {
            ship.command(ShipCommand.USE_SYSTEM)
        }
    }

    override fun overrideVanillaSystemAI(): Boolean {
        return false
    }
}