package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags

val ShipAPI.trueShipTarget: ShipAPI?
    get() {
        val ship = if (this.isStationModule) this.parentStation else this
        val engine = Global.getCombatEngine()
        val aiControl = ship != engine.playerShip || !engine.isUIAutopilotOn
        return if (aiControl) ship.aiFlags.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI
        else ship.shipTarget
    }

val ShipAPI.isInert: Boolean
    get() = this.isHulk || this.hullSpec.isBuiltInMod("vastbulk")
