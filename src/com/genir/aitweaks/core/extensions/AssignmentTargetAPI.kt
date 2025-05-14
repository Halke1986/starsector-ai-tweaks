package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AssignmentTargetAPI
import kotlin.math.abs

val AssignmentTargetAPI.isNearMapCenterline: Boolean
    get() = abs(location.x) < Global.getCombatEngine().mapWidth / 6f
