package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.combat.listeners.DamageListener

class DamageTracker(val ship: ShipAPI) : DamageListener {
    data class Entry(val timestamp: Float, val damage: Float)

    var damage: Float = 0f

    private var history: MutableMap<ShipAPI, MutableList<Entry>> = mutableMapOf()

    init {
        // Register damage tracker.
        ship.addListener(this)
    }

    fun advance() {
        val timestamp = Global.getCombatEngine().getTotalElapsedTime(false)
        val historyIter = history.iterator()

        while (historyIter.hasNext()) {
            val sourceHistory = historyIter.next()
            val source = sourceHistory.key
            val entries = sourceHistory.value

            if (!source.isAlive || source.isExpired) {
                // Drop the entire history for given source,
                // if the source is no longer valid.
                entries.forEach { entry -> damage -= entry.damage }
                historyIter.remove()
            } else {
                // Remove stale damage.
                val entriesIter = entries.iterator()
                while (entriesIter.hasNext()) {
                    val entry = entriesIter.next()

                    if (timestamp - entry.timestamp >= Preset.damageHistoryDuration) {
                        damage -= entry.damage
                        entriesIter.remove()
                    } else break
                }

                // All damage inflicted by source is stale.
                if (entries.isEmpty()) {
                    historyIter.remove()
                }
            }
        }
    }

    override fun reportDamageApplied(source: Any?, target: CombatEntityAPI, result: ApplyDamageResultAPI) {
        when {
            source !is ShipAPI -> return

            !source.isAlive || source.isExpired -> return

            target != ship -> return
        }

        val newDamage = result.damageToShields + result.damageToHull + result.totalDamageToArmor
        val timestamp = Global.getCombatEngine().getTotalElapsedTime(false)

        damage += newDamage

        val entry = Entry(timestamp, newDamage)
        if (history[source]?.add(entry) != true) {
            history[source as ShipAPI] = mutableListOf(entry)
        }
    }
}
