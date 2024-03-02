package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAssignmentType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.entities.Ship

/** Return personality preset used by vanilla ship AI. */
val ShipAPI.AIPersonality: String
    get() = (this.ai as? BasicShipAI)?.config?.personalityOverride ?: (this as Ship).personality

val ShipAPI.rootModule: ShipAPI
    get() = this.parentStation ?: this

val ShipAPI.isVastBulk: Boolean
    get() = this.hullSpec.isBuiltInMod("vastbulk")

val ShipAPI.hasEscortAssignment: Boolean
    get() = Global.getCombatEngine().getFleetManager(this.owner).getTaskManager(this.isAlly).getAssignmentFor(this)?.type.let { it == CombatAssignmentType.LIGHT_ESCORT || it == CombatAssignmentType.MEDIUM_ESCORT || it == CombatAssignmentType.HEAVY_ESCORT }
