package com.genir.aitweaks.core.playerassist

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.timeMult

fun makeAIDrone(ai: ShipAIPlugin, variantName: String): ShipAPI {
    val spec = Global.getSettings().getHullSpec("dem_drone")
    val v = Global.getSettings().createEmptyVariant(variantName, spec)
    val aiDrone = Global.getCombatEngine().createFXDrone(v)

    aiDrone.owner = 0
    aiDrone.mutableStats.hullDamageTakenMult.modifyMult("aitweaks_ai_drone", 0f) // so it's non-targetable
    aiDrone.isDrone = true
    aiDrone.collisionClass = CollisionClass.NONE
    aiDrone.location.y = -1e7f

    aiDrone.shipAI = ai

    return aiDrone
}

/** AI drone time multiplier needs to be the same as the player ship time multiplier.
 * This is required for the game engine to advance the AI the same number of times
 * as the player ship is advanced when the player ship is in fast-time mode. */
fun syncTimeWithPlayerShip(aiDrone: ShipAPI) {
    val droneTime = aiDrone.mutableStats.timeMult
    val playerShip = Global.getCombatEngine().playerShip
    if (playerShip != null) {
        droneTime.modifyMult("aitweaks_ai_drone_time_sync", playerShip.timeMult)
    } else {
        droneTime.unmodifyMult("aitweaks_ai_drone_time_sync")
    }
}
