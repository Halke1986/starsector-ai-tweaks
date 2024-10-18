package com.genir.aitweaks.core;

import org.lwjgl.util.vector.Vector2f;

import java.util.List;

/**
 * Collection of placeholders for obfuscated type names.
 */
public class Obfuscated {
    public enum ShipCommand {
        TURN_LEFT, TURN_RIGHT,
    }

    public interface AutofireManager {
        void advance_AutofireManager(float dt, ThreatEvalAI threatEvalAI, Vector2f missileDangerDir);
    }

    public interface CombatEntity {
    }

    public interface Maneuver {
        CombatEntity getTarget_Maneuver();
    }

    public static class ThreatEvalAI {
    }

    public static class ApproachManeuver {
    }

    public static class ShipCommandWrapper {
        public ShipCommand command_ShipCommandWrapper;
    }

    public static class Ship {
        public List<ShipCommandWrapper> getCommands() {
            return null;
        }
    }

    public static class BasicShipAI {
        public void advance(float amount) {
        }

        public Maneuver getCurrentManeuver() {
            return null;
        }

        public void cancelCurrentManeuver() {
        }
    }
}
