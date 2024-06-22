package com.genir.aitweaks.core.utils.extensions

import com.fs.starfarer.api.combat.ShipSystemSpecAPI
import com.genir.aitweaks.core.utils.ShipSystemAiType
import com.genir.aitweaks.core.utils.loadEnum

val ShipSystemSpecAPI.AIType: ShipSystemAiType?
    get() = loadEnum<ShipSystemAiType>(specJson, "aiType")
