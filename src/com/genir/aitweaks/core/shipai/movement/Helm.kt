package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand

open class Helm(val ship: ShipAPI) {
    private val commands: MutableSet<ShipCommand> = mutableSetOf()

    fun clearCommands() {
        commands.clear()
    }

    fun executeCommands() {
        commands.forEach { cmd ->
            ship.giveCommand(cmd, null, 0)
        }
    }

    fun giveCommand(cmd: ShipCommand) {
        commands.add(cmd)
    }
}