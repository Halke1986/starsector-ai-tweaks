package com.genir.aitweaks.launcher

import com.fs.starfarer.api.combat.ShipSystemAIScript
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.newCoreObject

class HighEnergyFocusAI : ShipSystemAIScript by newCoreObject("com.genir.aitweaks.core.features.shipai.systems.HighEnergyFocusAI")
