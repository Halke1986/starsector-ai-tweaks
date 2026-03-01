package com.genir.aitweaks.core.handles

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.handles.wrappers.ShipWrapper
import com.genir.starfarer.combat.entities.Ship

class ShipHandle(shipAPI: ShipAPI) : ShipWrapper(shipAPI as Ship) {
    val api: ShipAPI
        get() = ship

    override fun equals(other: Any?): Boolean {
        val otherShip: Any? = (other as? ShipHandle)?.ship ?: other

        return ship.equals(otherShip)
    }

    override fun hashCode(): Int {
        return ship.hashCode()
    }

    companion object {
        val ShipAPI.handle: ShipHandle
            get() = ShipHandle(this)
    }
}
