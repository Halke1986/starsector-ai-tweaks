package com.genir.aitweaks.core.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import org.lwjgl.util.vector.Vector2f

/** Stash holds variables that should carry
 * over between instances of Maneuver class. */
class Stash {
    var attackTarget: ShipAPI? = null
    var burnDriveHeading: Vector2f? = null

    private var lastUsed = totalElapsedTime()

    fun advance() {
        lastUsed = totalElapsedTime()
    }

    fun age(): Float {
        return totalElapsedTime() - lastUsed
    }

    private fun totalElapsedTime(): Float {
        return Global.getCombatEngine().getTotalElapsedTime(false)
    }
}

private const val stashKey = "aitweaks_custom_ai_stash"
private const val maxAge = 0.5f

fun getStash(ship: ShipAPI): Stash {
    val s = ship.customData[stashKey] as? Stash

    if (s != null && s.age() <= maxAge) {
        return s
    } else {
        val newStash = Stash()
        ship.setCustomData(stashKey, newStash)
        return newStash
    }
}
