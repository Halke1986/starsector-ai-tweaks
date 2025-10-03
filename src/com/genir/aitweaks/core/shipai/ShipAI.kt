package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.shipai.global.GlobalAI

interface ShipAI {
    val ship: ShipAPI
    val flags: Flags
    val globalAI: GlobalAI
}
