package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.FogOfWarAPI

fun FogOfWarAPI.filter(entities: Sequence<CombatEntityAPI>): Sequence<CombatEntityAPI> {
    return entities.filter { isVisible(it) }
}
