package com.genir.aitweaks

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine
import com.fs.starfarer.loading.specs.HullVariantSpec

const val fbeam = "fnisherbeamprotocol"
const val keyPrefix = "\$aitweaks_finisherbeamprotocol"

open class MakeAITweaksRemovable : BaseModPlugin() {
    override fun beforeGameSave() {
        clearMemoryKeys()
        traverseState(::processBeforeSave)
    }

    override fun afterGameSave() {
        traverseState(::processAfterSave)
        clearMemoryKeys()
    }

    override fun onGameLoad(newGame: Boolean) {
        traverseState(::processAfterSave)
        clearMemoryKeys()
    }

    private fun traverseState(process: (HasHullMod) -> Unit) {
        CampaignEngine.getInstance().savedVariantData.variantMap.forEach { process(Variant(it)) }

        // All active fleets.
        Global.getSector().allLocations.forEach { loc ->
            loc.fleets.forEach { fleet ->
                fleet.fleetData.membersListCopy.forEach { process(Ship(it)) }
            }
        }

        Global.getSector().allFactions.forEach { faction ->
            process(Faction(faction))

            Misc.getFactionMarkets(faction).forEach { market ->
                market.submarketsCopy.forEach { submarket ->
                    process(SubMarketFaction(faction, market, submarket))
                    submarket.cargo?.mothballedShips?.membersListCopy?.forEach { member ->
                        process(SubMarketShip(member, faction, market, submarket))
                    }
                }
            }
        }
    }

    private fun processBeforeSave(e: HasHullMod) {
        if (e.hullMods().contains(fbeam)) {
            e.hullMods().remove(fbeam)
            CampaignEngine.getInstance().memoryWithoutUpdate.set(decorateKey(e.key()), null)
        }
    }

    private fun processAfterSave(e: HasHullMod) {
        val memory = CampaignEngine.getInstance().memoryWithoutUpdate
        if (memory.contains(decorateKey(e.key()))) {
            e.hullMods().add(fbeam)
        }
    }

    private fun clearMemoryKeys() {
        val memory = CampaignEngine.getInstance().memoryWithoutUpdate
        memory.keys.filter { it.startsWith(keyPrefix) }.forEach {
            memory.unset(it)
        }
    }

    private fun decorateKey(k: String): String = "$keyPrefix $k".replace(Regex("\\s+"), "_")
}

private interface HasHullMod {
    fun key(): String
    fun hullMods(): MutableCollection<String>
}

private class Variant(val v: Map.Entry<String, HullVariantSpec>) : HasHullMod {
    override fun key() = "variant ${v.key}"
    override fun hullMods(): MutableCollection<String> = v.value.hullMods
}

private class Ship(val member: FleetMemberAPI) : HasHullMod {
    override fun key() = "ship ${member.id}"
    override fun hullMods(): MutableCollection<String> = member.variant.hullMods
}

private class Faction(val faction: FactionAPI) : HasHullMod {
    override fun key() = "faction ${faction.id}"
    override fun hullMods(): MutableCollection<String> = faction.knownHullMods
}

private class SubMarketFaction(val faction: FactionAPI, val market: MarketAPI, val submarket: SubmarketAPI) : HasHullMod {
    override fun key() = "submarket faction ${faction.id} ${market.id} ${submarket.name}"
    override fun hullMods(): MutableCollection<String> = submarket.faction.knownHullMods
}

private class SubMarketShip(val member: FleetMemberAPI, val faction: FactionAPI, val market: MarketAPI, val submarket: SubmarketAPI) : HasHullMod {
    override fun key() = "submarket ship ${faction.id} ${market.id} ${submarket.name} ${member.id}"
    override fun hullMods(): MutableCollection<String> = member.variant.hullMods
}
