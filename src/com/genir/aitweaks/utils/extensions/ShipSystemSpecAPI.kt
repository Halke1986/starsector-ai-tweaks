package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.ShipSystemSpecAPI
import com.genir.aitweaks.utils.ShipSystemAiType
import com.genir.aitweaks.utils.loadEnum

val ShipSystemSpecAPI.AIType: ShipSystemAiType?
    get() = loadEnum<ShipSystemAiType>(specJson, "aiType")
