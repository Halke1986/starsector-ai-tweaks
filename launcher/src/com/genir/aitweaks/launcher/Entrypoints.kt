package com.genir.aitweaks.launcher

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate

class HighEnergyFocus : ShipSystemAIScript by coreLoader.loadClass("com.genir.aitweaks.core.shipai.systems.HighEnergyFocus").instantiate()

interface AutofireAIPicker {
    fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin>?
}

interface ShipAIPicker {
    fun pickShipAI(member: FleetMemberAPI?, ship: ShipAPI): PluginPick<ShipAIPlugin>?

    fun canHaveCustomAI(ship: ShipAPI): Boolean
}
