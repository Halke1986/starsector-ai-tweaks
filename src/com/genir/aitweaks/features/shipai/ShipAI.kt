package com.genir.aitweaks.features.shipai

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

private val loader = AIClassLoader()

fun shouldHaveAssemblyAI(ship: ShipAPI): Boolean {
    return when {
        ship.owner != 0 -> false
        ship.hullSpec.hullId != "guardian" -> false
        else -> true
    }
}

fun newAssemblyAI(ship: ShipAPI, config: ShipAIConfig = ShipAIConfig()): ShipAIPlugin {
    val klas = loader.loadClass("com.genir.aitweaks.asm.shipai.AssemblyShipAI")
    val type = MethodType.methodType(Void.TYPE, Ship::class.java, ShipAIConfig::class.java)

    val ctor = MethodHandles.lookup().findConstructor(klas, type)

    return ctor.invoke(ship as Ship, config) as ShipAIPlugin
}

fun assemblyShipAIClass() = loader.loadClass("com.genir.aitweaks.asm.shipai.AssemblyShipAI")

fun aiPluginAdapterClass() = loader.loadClass("com.genir.aitweaks.asm.shipai.AIPluginAdapter")

val ShipAPI.hasBasicShipAI: Boolean
    get() = when {
        ai is BasicShipAI -> true
        assemblyShipAIClass().isInstance(ai) -> true
        else -> false
    }

fun ShipAPI.hasAIType(c: Class<*>): Boolean {
    return when {
        c.isInstance(ai) -> true
        c.isInstance((ai as? Ship.ShipAIWrapper)?.ai) -> true
        else -> false
    }
}
