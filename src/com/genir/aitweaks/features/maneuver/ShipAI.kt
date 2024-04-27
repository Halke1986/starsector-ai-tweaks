package com.genir.aitweaks.features.maneuver

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.utils.extensions.isShip
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

fun newVanillaAI(ship: ShipAPI, config: ShipAIConfig = ShipAIConfig()): ShipAIPlugin {
    if (!ship.isShip) {
        return BasicShipAI(ship as Ship, config)
    }

    val loader = AIClassLoader()

    val klas = loader.loadClass("com.genir.aitweaks.asm.combat.ai.AssemblyShipAI")
    val type = MethodType.methodType(Void.TYPE, Ship::class.java, ShipAIConfig::class.java)

    val ctor = MethodHandles.lookup().findConstructor(klas, type)

    return ctor.invoke(ship as Ship, config) as ShipAIPlugin
}