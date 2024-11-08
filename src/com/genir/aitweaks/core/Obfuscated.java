package com.genir.aitweaks.core;

import org.lwjgl.util.vector.Vector2f;

import java.util.List;

/**
 * Collection of placeholders for obfuscated type names.
 */
public class Obfuscated {
    public enum ShipCommand {
    }

    public interface AutofireManager {
        void autofireManager_advance(float dt, ThreatEvalAI threatEvalAI, Vector2f missileDangerDir);
    }

    public interface CombatEntity {
    }

    public interface Maneuver {
        CombatEntity maneuver_getTarget();
    }

    public static class ThreatEvalAI {
    }

    public static class ApproachManeuver {
    }

    public static class ShipCommandWrapper {
        public ShipCommand shipCommandWrapper_getCommand;
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

    public interface Weapon {
        AimTracker getAimTracker();
    }

    public static class AimTracker {
        public void aimTracker_setTargetOverride(Vector2f aim) {
        }
    }
}
