package com.genir.aitweaks.extensions

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags

val ShipAPI.maneuverTarget: ShipAPI?
    get() = this.aiFlags.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI
