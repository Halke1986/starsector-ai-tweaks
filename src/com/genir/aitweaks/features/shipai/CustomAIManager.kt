package com.genir.aitweaks.features.shipai

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.features.shipai.loading.Builder
import lunalib.lunaSettings.LunaSettings
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

class CustomAIManager {
    companion object {
        /** Test the AI build process by attempting to load custom AI Java class. */
        fun test() = getCustomAIClass()

        /** Get CustomShipAI class. Returns null if custom AI is disabled. */
        fun getCustomAIClass(): Class<*>? {
            if (LunaSettings.getBoolean("aitweaks", "aitweaks_enable_custom_ship_ai") != true) {
                return null
            }

            return Builder.getCustomShipAI()
        }

        /** Get CustomShipAI. Returns null if custom AI is disabled or not applicable to given ship. */
        fun getAI(ship: ShipAPI, config: ShipAIConfig = ShipAIConfig()): ShipAIPlugin? {
            if (!shouldHaveCustomAI(ship)) return null

            val klass = getCustomAIClass() ?: return null
            val type = MethodType.methodType(Void.TYPE, Ship::class.java, ShipAIConfig::class.java)
            val ctor = MethodHandles.lookup().findConstructor(klass, type)
            return ctor.invoke(ship as Ship, config) as ShipAIPlugin
        }

        /** Currently, custom AI is enabled only for Guardian in Cryosleeper encounter. */
        private fun shouldHaveCustomAI(ship: ShipAPI): Boolean {
            val ships = Global.getCombatEngine().ships
            val isCryosleeper = ships.count { it.owner == 1 } == 1 && ships.count { it.owner == 1 && it.hullSpec.hullId == "guardian" } == 1

            return when {
                Global.getCurrentState() != GameState.COMBAT -> false
                getCustomAIClass() == null -> false

                ship.owner == 1 && isCryosleeper -> true
                ship.owner == 0 && (ship.isCruiser || ship.isCapital || ship.name == "VSS Neutrino Drag") -> true

                else -> false
            }
        }
    }
}
