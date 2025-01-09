package com.genir.aitweaks.launcher

import com.fs.starfarer.api.combat.HullModEffect
import com.fs.starfarer.api.combat.ShipSystemAIScript
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate

class HighEnergyFocusAI : ShipSystemAIScript by coreLoader.loadClass("com.genir.aitweaks.core.shipai.systems.HighEnergyFocusAI").instantiate()

class CustomShipAI : HullModEffect by coreLoader.loadClass("com.genir.aitweaks.core.hullmods.CustomShipAI").instantiate()

class FinisherBeamProtocol : HullModEffect by coreLoader.loadClass("com.genir.aitweaks.core.hullmods.FinisherBeamProtocol").instantiate()

class SystemShunt : HullModEffect by coreLoader.loadClass("com.genir.aitweaks.core.hullmods.SystemShunt").instantiate()
