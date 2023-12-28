package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags

val ShipAPI.maneuverTarget: ShipAPI?
    get() = this.aiFlags.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI

// TODO remove if nothing breaks
val ShipAPI.isValidTarget: Boolean
    get() = this.isAlive
//    get() = Global.getCombatEngine().isEntityInPlay(this) && this.isAlive