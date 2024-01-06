package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags

val ShipAPI.maneuverTarget: ShipAPI?
    get() = this.aiFlags.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI

val ShipAPI.isInert: Boolean
    get() = this.isHulk || this.hullSpec.isBuiltInMod("vastbulk")
