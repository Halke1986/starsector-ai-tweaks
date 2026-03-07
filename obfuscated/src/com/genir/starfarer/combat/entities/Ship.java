package com.genir.starfarer.combat.entities;

import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.genir.starfarer.combat.ai.AI;

import java.util.EnumSet;
import java.util.List;

// UNOBFUSCATED
public class Ship {
    // UNOBFUSCATED
    public String getPersonality() {
        return null;
    }

    // UNOBFUSCATED
    public EnumSet<Command> getBlockedCommands() {
        return null;
    }

    // UNOBFUSCATED
    public void setNoWeaponSelected() {
    }

    // UNOBFUSCATED
    public void setFallbackPersonalityId(String var1) {
    }

    // UNOBFUSCATED
    public List<CommandWrapper> getCommands() {
        return null;
    }

    // UNOBFUSCATED
    public AI getAI() {
        return null;
    }

    // UNOBFUSCATED
    public void setAI(AI var1) {
    }

    // OBFUSCATED
    public enum Command {}

    public static class ShipAIWrapper implements ShipAIPlugin {
        // UNOBFUSCATED
        public ShipAIPlugin getAI() {
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

    // OBFUSCATED
    public static class CommandWrapper {
        // OBFUSCATED
        public Command shipCommandWrapper_command;
    }
}
