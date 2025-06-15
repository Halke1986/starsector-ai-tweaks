package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import java.util.*

/** FluxTracker tracks the rise in flux level during the provided period. */
class FluxTracker(val ship: ShipAPI, private val period: Float) {
    private data class Record(val value: Float, val timestamp: Float)

    private val history = LinkedList<Record>()

    fun advance() {
        if (ship.fluxTracker.isVenting) {
            history.clear()
            return
        }

        val timestamp = Global.getCombatEngine().getTotalElapsedTime(false)

        val newRecord = Record(
            ship.fluxLevel,
            timestamp,
        )

        history.addFirst(newRecord)

        if (timestamp - history.last.timestamp > period) {
            history.removeLast()
        }
    }

    fun delta(): Float {
        if (history.size < 2) {
            return 0f
        }

        return history.first.value - history.last.value
    }
}
