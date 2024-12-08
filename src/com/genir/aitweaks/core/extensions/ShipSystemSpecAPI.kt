package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.combat.ShipSystemSpecAPI
import com.genir.aitweaks.core.utils.ShipSystemAIType
import com.genir.aitweaks.core.utils.loadEnum

val ShipSystemSpecAPI.AIType: ShipSystemAIType?
    get() = loadEnum<ShipSystemAIType>(specJson, "aiType")
