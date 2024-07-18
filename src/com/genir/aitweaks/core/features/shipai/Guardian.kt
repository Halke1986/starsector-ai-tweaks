package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.utils.extensions.hasCustomAI
import com.genir.aitweaks.core.utils.extensions.isPD

/** Special config for Guardians with custom AI. */
class Guardian : BaseEveryFrameCombatPlugin() {
    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val ships = Global.getCombatEngine().ships
        val guardiansWithCustomAI = ships.filter { it.hullSpec.hullId.startsWith("guardian") && it.hasCustomAI }

        // Prevent non-PD turrets from attacking fighters.
        guardiansWithCustomAI.flatMap { it.allWeapons }.filter { it.slot.isTurret && !it.isPD }.forEach { it.spec.aiHints.add(WeaponAPI.AIHints.STRIKE) }


        // Stay on first weapon group.
        guardiansWithCustomAI.forEach {
            val ship = it as Ship
            val groups = ship.weaponGroupsCopy
            val firstGroup = groups.removeFirst()

            val weapons = groups.flatMap { group -> group.weaponsCopy }
            weapons.forEach { weapon ->
                ship.removeWeaponFromGroupsReal(weapon)
                firstGroup.addWeaponAPI(weapon)
            }

            ship.setNoWeaponSelected()
            ship.blockCommandForOneFrame(ShipCommand.SELECT_GROUP)
        }
    }
}