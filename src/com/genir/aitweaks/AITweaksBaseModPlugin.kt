package com.genir.aitweaks

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.Misc.getFactionMarkets
import com.fs.starfarer.campaign.CampaignEngine
import com.fs.starfarer.campaign.econ.Market
import com.fs.starfarer.campaign.fleet.FleetData
import com.genir.aitweaks.features.autofire.AutofireAI

class AITweaksBaseModPlugin : BaseModPlugin() {
    override fun pickWeaponAutofireAI(weapon: WeaponAPI): PluginPick<AutofireAIPlugin> {
        val ai = if (weapon.type != WeaponAPI.WeaponType.MISSILE) AutofireAI(weapon)
        else null

        return PluginPick(ai, PickPriority.MOD_GENERAL)
    }

    override fun beforeGameSave() {
        val campaign = CampaignEngine.getInstance()
        campaign.savedVariantData.variantMap.forEach { it.value.hullMods.remove("fnisherbeamprotocol") }

        Global.getSector().allLocations.map { loc ->

            Global.getLogger(this.javaClass).error(loc.name)

            loc.fleets.forEach { fleet ->

                Global.getLogger(this.javaClass).error("   " + fleet.name)

                (fleet.fleetData as FleetData).members.forEach { member ->

                    Global.getLogger(this.javaClass).error("      " + member.shipName)
//                    member.variant.hullMods.forEach { mod ->
//                        Global.getLogger(this.javaClass).error("         $mod")
//                    }

                    member.variant.hullMods.remove("fnisherbeamprotocol")
                }
            }
        }

//        Global.getSector().allFactions.forEach { it.fl

        Global.getSector().allFactions.forEach { it.knownHullMods.remove("fnisherbeamprotocol") }
//        Global.getSettings().allHullModSpecs

        getFactionMarkets(Global.getSector().playerFaction).forEach { m ->
            (m as Market).submarkets.forEach { sub ->
                sub.cargo?.mothballedShips?.members?.forEach { member ->
                    member.variant.hullMods.remove("fnisherbeamprotocol")
                }
                sub.faction.knownHullMods.remove("fnisherbeamprotocol")
            }

//               m.submarketsCopy.forEach { s ->
//                    s.cargo.h removeItems(something)
//                }
        }

        Global.getSector().allFactions.forEach {

//                    Global.getLogger(this.javaClass).error(it.displayName)

            getFactionMarkets(it).forEach { m ->
//                        Global.getLogger(this.javaClass).error("   ${m.name}");

                (m as Market).submarkets.forEach { sub ->

                    sub.cargo?.mothballedShips?.members?.forEach { member ->
                        member.variant.hullMods.remove("fnisherbeamprotocol")
                    }
//                            Global.getLogger(this.javaClass).error("      ${sub.name} ${sub.plugin?.name}");


//                    sub.faction.knownHullMods.forEach { m -> Global.getLogger(this.javaClass).error("              $m"); }

                    sub.faction.knownHullMods.remove("fnisherbeamprotocol")
                }

//               m.submarketsCopy.forEach { s ->
//                    s.cargo.h removeItems(something)
//                }
            }
        }


//        campaign.economy.markets.forEach { (it as Market).submarkets.forEach { sub -> sub.faction.knownHullMods.remove("fnisherbeamprotocol") } }

//        Global. saved .forEach { it.knownHullMods.remove("fnisherbeamprotocol") }
    }
}

