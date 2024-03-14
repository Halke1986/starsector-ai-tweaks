package com.genir.aitweaks

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.campaign.CampaignEngine

const val fbeam = "fnisherbeamprotocol" // TODO rename "aitweaks_finisherbeamprotocol"
const val keyPrefix = "\$aitweaks_finisherbeamprotocol"

open class MakeAITweaksRemovable : BaseModPlugin() {
    override fun beforeGameSave() {
        clearMemoryKeys()
        getEntitiesWithHullmods().forEach { processBeforeSave(it) }
    }

    override fun afterGameSave() {
        getEntitiesWithHullmods().forEach { processAfterSave(it) }
        clearMemoryKeys()
    }

    override fun onGameLoad(newGame: Boolean) {
        getEntitiesWithHullmods().forEach { processAfterSave(it) }
        clearMemoryKeys()
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

    private fun getEntitiesWithHullmods(): List<HasHullMod> {
        val locations = Global.getSector().allLocations

        val submarkets = locations.flatMap { it.allEntities }.mapNotNull { it.market }.flatMap { it.submarketsCopy }

        val fleetMembers = listOf(
            locations.flatMap { it.fleets }.map { it.fleetData }, // Ships in active fleets.
            submarkets.mapNotNull { it.cargo?.mothballedShips },  // Ships in storage.
        ).flatten().flatMap { it.membersListCopy }

        return listOf(
            Global.getSector().allFactions.map { Faction(it) }, // Factions.
            submarkets.map { Submarket(it) }, // Submarkets.
            fleetMembers.map { Ship(it) }, // Ships.
            CampaignEngine.getInstance().savedVariantData.variantMap.map { Variant(it.key, it.value) }, // Global variants.
            fleetMembers.flatMap { ship -> ship.moduleVariants().map { Variant("${it.key} ${ship.id}", it.value) } }, // Ship modules.
        ).flatten()
    }

    private fun FleetMemberAPI.moduleVariants(): Map<String, ShipVariantAPI> {
        return this.variant.stationModules.mapValues { this.variant.getModuleVariant(it.key) }
    }

    private fun decorateKey(k: String): String = "$keyPrefix $k".replace(Regex("\\s+"), "_")
}

private interface HasHullMod {
    fun key(): String
    fun hullMods(): MutableCollection<String>
}

private class Variant(val key: String, val variant: ShipVariantAPI) : HasHullMod {
    override fun key() = "variant $key"
    override fun hullMods(): MutableCollection<String> = variant.hullMods
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
