package com.genir.starfarer.combat.ai;

import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.genir.starfarer.combat.ai.attack.AttackAIModule;
import com.genir.starfarer.combat.ai.movement.FlockingAI;
import com.genir.starfarer.combat.ai.movement.maneuvers.Maneuver;
import com.genir.starfarer.combat.entities.Ship;

// UNOBFUSCATED
public class BasicShipAI implements ShipAIPlugin, AI {
    public BasicShipAI(Ship ship, ShipAIConfig config) {
    }

    // UNOBFUSCATED
    public Maneuver getCurrentManeuver() {
        return null;
    }

    // UNOBFUSCATED
    public AttackAIModule getAttackAI() {
        return null;
    }

    // UNOBFUSCATED
    public FlockingAI getFlockingAI() {
        return null;
    }

    // UNOBFUSCATED
    public ThreatEvaluator getThreatEvaluator() {
        return null;
    }

    // UNOBFUSCATED
    public ShieldAI getShieldAI() {
        return null;
    }

    @Override
    public void setDoNotFireDelay(float amount) {

    }

    @Override
    public void forceCircumstanceEvaluation() {

    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public void render() {

    }

    @Override
    public boolean needsRefit() {
        return false;
    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return null;
    }

    @Override
    public void cancelCurrentManeuver() {

    }

    @Override
    public ShipAIConfig getConfig() {
        return null;
    }

    @Override
    public void setTargetOverride(ShipAPI target) {
        ShipAIPlugin.super.setTargetOverride(target);
    }
}
