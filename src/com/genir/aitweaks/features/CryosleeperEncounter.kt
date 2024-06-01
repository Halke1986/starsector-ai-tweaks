package com.genir.aitweaks.features

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed
import com.fs.starfarer.api.util.Misc
import com.genir.aitweaks.utils.log
import java.util.*

class CryosleeperEncounter : SalvageGenFromSeed.SalvageDefenderModificationPlugin {
    override fun getHandlingPriority(params: Any?): Int {
        return when {
            params !is SalvageGenFromSeed.SDMParams -> 0
            params.entity.customEntityType == Entities.DERELICT_CRYOSLEEPER -> 2
            else -> -1
        }
    }

    override fun modifyFleet(p: SalvageGenFromSeed.SDMParams, fleet: CampaignFleetAPI, random: Random?, withOverride: Boolean) {
        if (p.entity.customEntityType != Entities.DERELICT_CRYOSLEEPER)
            return

        fleet.fleetData.clear()
        fleet.fleetData.addFleetMember("guardian_Nonstandard")

        val plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE)
        val captain = plugin.createPerson(Commodities.ALPHA_CORE, fleet.faction.id, random)

        captain.stats.skillsCopy.filter { it.level != 0f }.forEach {
            captain.stats.decreaseSkill(it.skill.id)
            captain.stats.decreaseSkill(it.skill.id)
        }

        captain.stats.setSkillLevel(Skills.HELMSMANSHIP, 2f)
        captain.stats.setSkillLevel(Skills.TARGET_ANALYSIS, 2f)
        captain.stats.setSkillLevel(Skills.BALLISTIC_MASTERY, 2f)
        captain.stats.setSkillLevel(Skills.FIELD_MODULATION, 2f)
        captain.stats.setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2f)
        captain.stats.setSkillLevel(Skills.GUNNERY_IMPLANTS, 2f)
        captain.stats.setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2f)
        captain.stats.setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2f)

        // so it's not the standard alpha core portrait but an older-looking one
        captain.portraitSprite = fleet.faction.createRandomPerson().portraitSprite
        fleet.commander = captain
        fleet.flagship.captain = captain
        RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(fleet.flagship)

        for (member in fleet.fleetData.membersListCopy) {
            member.repairTracker.cr = member.repairTracker.maxCR
        }
    }

    override fun getStrength(p: SalvageGenFromSeed.SDMParams?, strength: Float, random: Random?, withOverride: Boolean) = strength

    override fun getProbability(p: SalvageGenFromSeed.SDMParams?, probability: Float, random: Random?, withOverride: Boolean) = probability

    override fun getQuality(p: SalvageGenFromSeed.SDMParams?, quality: Float, random: Random?, withOverride: Boolean) = quality

    override fun getMaxSize(p: SalvageGenFromSeed.SDMParams?, maxSize: Float, random: Random?, withOverride: Boolean) = maxSize

    override fun getMinSize(p: SalvageGenFromSeed.SDMParams?, minSize: Float, random: Random?, withOverride: Boolean) = minSize

    override fun reportDefeated(p: SalvageGenFromSeed.SDMParams?, entity: SectorEntityToken?, fleet: CampaignFleetAPI?) = Unit
}