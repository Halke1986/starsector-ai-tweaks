package com.genir.starfarer.title.mission;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;

import java.awt.*;

// UNOBFUSCATED
public class MissionDefinition implements MissionDefinitionAPI {
    @Override
    public void initFleet(FleetSide side, String shipNamePrefix, FleetGoal goal, boolean useDefaultAI) {

    }

    @Override
    public void initFleet(FleetSide side, String shipNamePrefix, FleetGoal goal, boolean useDefaultAI, int commandRating) {

    }

    @Override
    public FleetMemberAPI addToFleet(FleetSide side, String variantId, FleetMemberType type, boolean isFlagship) {
        return null;
    }

    @Override
    public FleetMemberAPI addToFleet(FleetSide side, String variantId, FleetMemberType type, String shipName, boolean isFlagship) {
        return null;
    }

    @Override
    public void defeatOnShipLoss(String shipName) {

    }

    @Override
    public void addBriefingItem(String item) {

    }

    @Override
    public void setFleetTagline(FleetSide side, String tagline) {

    }

    @Override
    public void initMap(float minX, float maxX, float minY, float maxY) {

    }

    @Override
    public void addNebula(float x, float y, float radius) {

    }

    @Override
    @Deprecated
    public void addObjective(float x, float y, String type, BattleObjectiveAPI.Importance importance) {

    }

    @Override
    public void addObjective(float x, float y, String type) {

    }

    @Override
    public void addPlanet(float x, float y, float radius, String type, float gravity) {

    }

    @Override
    public void addPlanet(float x, float y, float radius, String type, float gravity, boolean backgroundPlanet) {

    }

    @Override
    public void addPlanet(float x, float y, float radius, PlanetAPI planet, float gravity, boolean backgroundPlanet) {

    }

    @Override
    public void setPlanetBgSize(float bgWidth, float bgHeight) {

    }

    @Override
    public void addAsteroidField(float x, float y, float angle, float width, float minSpeed, float maxSpeed, int quantity) {

    }

    @Override
    public void addRingAsteroids(float x, float y, float angle, float width, float minSpeed, float maxSpeed, int quantity) {

    }

    @Override
    public int getFleetPointCost(String id) {
        return 0;
    }

    @Override
    public void addPlugin(EveryFrameCombatPlugin plugin) {

    }

    @Override
    public void setBackgroundSpriteName(String background) {

    }

    @Override
    public void addFleetMember(FleetSide side, FleetMemberAPI member) {

    }

    @Override
    public void setHyperspaceMode(boolean hyperspaceMode) {

    }

    @Override
    public void setNebulaTex(String nebulaTex) {

    }

    @Override
    public void setNebulaMapTex(String nebulaMapTex) {

    }

    @Override
    public void setBackgroundGlowColor(Color backgroundGlowColor) {

    }

    @Override
    public void initFleet(FleetSide side, String shipNamePrefix, FleetGoal goal, boolean useDefaultAI, int commandRating, int allyCommandRating) {

    }

    @Override
    public BattleCreationContext getContext() {
        return null;
    }

    @Override
    public PersonAPI getDefaultCommander(FleetSide side) {
        return null;
    }

    @Override
    public boolean hasNebula() {
        return false;
    }

    // OBFUSCATED
    public static class PluginContainer {
        // OBFUSCATED
        public EveryFrameCombatPlugin missionDefinitionPluginContainer_getEveryFrameCombatPlugin() {
            return null;
        }
    }
}
