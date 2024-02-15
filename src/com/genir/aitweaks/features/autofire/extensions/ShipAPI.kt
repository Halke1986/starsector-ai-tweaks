package com.genir.aitweaks.features.autofire.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAssignmentType.*
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags

val ShipAPI.trueShipTarget: ShipAPI?
    get() {
        val ship = this.root
        val engine = Global.getCombatEngine()
        val aiControl = ship != engine.playerShip || !engine.isUIAutopilotOn
        return if (aiControl) ship.aiFlags.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI
        else ship.shipTarget
    }

val ShipAPI.root: ShipAPI
    get() = this.parentStation ?: this

val ShipAPI.isInert: Boolean
    get() = this.isHulk || this.hullSpec.isBuiltInMod("vastbulk")

val ShipAPI.hasEscortAssignment: Boolean
    get() = Global.getCombatEngine().getFleetManager(this.owner).getTaskManager(this.isAlly)
        .getAssignmentFor(this)?.type.let { it == LIGHT_ESCORT || it == MEDIUM_ESCORT || it == HEAVY_ESCORT }
