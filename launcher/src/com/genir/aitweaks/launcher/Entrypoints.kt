package com.genir.aitweaks.launcher

import com.fs.starfarer.api.combat.ShipSystemAIScript
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate

class HighEnergyFocus : ShipSystemAIScript by coreLoader.loadClass("com.genir.aitweaks.core.shipai.systems.HighEnergyFocus").instantiate()
