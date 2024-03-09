package com.genir.aitweaks

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.campaign.CampaignEngine
import com.fs.starfarer.loading.specs.HullVariantSpec

const val fbeam = "fnisherbeamprotocol" // TODO rename "aitweaks_finisherbeamprotocol"
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
        val locations = Global.getSector().allLocations
        val submarkets = locations.flatMap { it.allEntities }.mapNotNull { it.market }.flatMap { it.submarketsCopy }

        // Global variants.
        CampaignEngine.getInstance().savedVariantData.variantMap.forEach { process(Variant(it)) }

        // Factions.
        Global.getSector().allFactions.forEach { process(Faction(it)) }

        // Ships in active fleets.
        locations.flatMap { it.fleets }.flatMap { it.fleetData.membersListCopy }.forEach { process(Ship(it)) }

        // Ships in storage.
        submarkets.mapNotNull { it.cargo?.mothballedShips }.flatMap { it.membersListCopy }.forEach { process(Ship(it)) }

        // Submarkets.
        submarkets.forEach { process(Submarket(it)) }
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

private class Submarket(val submarket: SubmarketAPI) : HasHullMod {
    override fun key() = "submarket ${submarket.market.primaryEntity.id} ${submarket.nameOneLine}"
    override fun hullMods(): MutableCollection<String> = submarket.faction.knownHullMods
}
