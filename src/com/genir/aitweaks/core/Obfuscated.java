package com.genir.aitweaks.core;

import org.lwjgl.util.vector.Vector2f;

import java.util.List;

/**
 * Collection of placeholders for obfuscated type names.
 */
public class Obfuscated {
    public static class ThreatEvalAI {
    }

    public interface AutofireManager {
        void advance_AutofireManager(float dt, ThreatEvalAI threatEvalAI, Vector2f missileDangerDir);
    }

    public interface Maneuver {
    }

    public static class ApproachManeuver {
    }

    public enum ShipCommand {
        TURN_LEFT, TURN_RIGHT,
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
    }
}
