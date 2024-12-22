package com.genir.aitweaks.launcher

import com.fs.starfarer.api.combat.HullModEffect
import com.fs.starfarer.api.combat.ShipSystemAIScript
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.newCoreObject

class HighEnergyFocusAI : ShipSystemAIScript by newCoreObject("com.genir.aitweaks.core.shipai.systems.HighEnergyFocusAI")

class CustomShipAI : HullModEffect by newCoreObject("com.genir.aitweaks.core.hullmods.CustomShipAI")

class FinisherBeamProtocol : HullModEffect by newCoreObject("com.genir.aitweaks.core.hullmods.FinisherBeamProtocol")

class SystemShunt : HullModEffect by newCoreObject("com.genir.aitweaks.core.hullmods.SystemShunt")
