package com.genir.aitweaks.core.playerassist

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI

fun makeAIDrone(ai: ShipAIPlugin): ShipAPI {
    val spec = Global.getSettings().getHullSpec("dem_drone")
    val v = Global.getSettings().createEmptyVariant("dem_drone", spec)
    val aiDrone = Global.getCombatEngine().createFXDrone(v)

    aiDrone.owner = 0
    aiDrone.mutableStats.hullDamageTakenMult.modifyMult("aitweaks_ai_drone", 0f) // so it's non-targetable
    aiDrone.isDrone = true
    aiDrone.collisionClass = CollisionClass.NONE
    aiDrone.location.y = -1e7f

    aiDrone.shipAI = ai

    return aiDrone
}
