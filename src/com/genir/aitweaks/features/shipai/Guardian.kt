package com.genir.aitweaks.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.utils.extensions.hasAIType
import com.genir.aitweaks.utils.extensions.isPD

/** Special config for Guardians with custom AI. */
class Guardian : BaseEveryFrameCombatPlugin() {
    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val ships = Global.getCombatEngine().ships
        val guardiansWithCustomAI = ships.filter { it.hullSpec.hullId == "guardian" && it.hasAIType(CustomAIManager.getCustomAIClass()) }

        // Prevent non-PD turrets from attacking fighters.
        guardiansWithCustomAI.flatMap { it.allWeapons }.filter { it.slot.isTurret && !it.isPD }.forEach { it.spec.aiHints.add(WeaponAPI.AIHints.STRIKE) }

        // Stay on first weapon group.
        guardiansWithCustomAI.forEach { it.blockCommandForOneFrame(ShipCommand.SELECT_GROUP) }

        // Custom ship AI can't follow orders very well. TODO remove when order following is implemented.
        val isCryosleeper = ships.count { it.owner == 1 } == 1 && guardiansWithCustomAI.count { it.owner == 1 } == 1
        if (isCryosleeper)
            Global.getCombatEngine().getFleetManager(1).admiralAI = null
    }
}