package com.genir.aitweaks.core.utils.extensions

import com.fs.starfarer.api.combat.ShipSystemSpecAPI
import com.genir.aitweaks.core.utils.ShipSystemAIType
import com.genir.aitweaks.core.utils.loadEnum

val ShipSystemSpecAPI.AIType: ShipSystemAIType?
    get() = loadEnum<ShipSystemAIType>(specJson, "aiType")
