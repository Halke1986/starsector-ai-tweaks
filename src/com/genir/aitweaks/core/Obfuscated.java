package com.genir.aitweaks.core;

import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Collection of placeholders for obfuscated types.
 */
public class Obfuscated {
    public enum ShipCommand {}

    public enum PlayerAction {}

    public interface AutofireManager {
        void autofireManager_advance(float dt, ThreatEvaluator threatEvalAI, Vector2f missileDangerDir);
    }

    public interface CombatEntity {
    }

    public interface Maneuver {
        CombatEntity maneuver_getTarget();
    }

    public interface Weapon {
        AimTracker getAimTracker();
    }

    public interface ThreatResponseManeuver {
    }

    public interface SystemAI {
        void systemAI_advance(float dt, Vector2f missileDangerDir, Vector2f collisionDangerDir, Ship target);
    }

    public interface ShieldAI {
        void shieldAI_advance(float dt, ThreatEvaluator threatEvalAI, Vector2f missileDangerDir, Vector2f collisionDangerDir, Ship target);
    }

    public static class ThreatEvaluator {
        public ThreatResponseManeuver threatEvaluator_advance(float dt) {
            return null;
        }
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

        public EnumSet<ShipCommand> getBlockedCommands() {
            return null;
        }
    }

    public static class BasicShipAI implements ShipAIPlugin {
        public BasicShipAI(Ship ship, ShipAIConfig config) {
        }

        @Override
        public void advance(float amount) {
        }

        public Maneuver getCurrentManeuver() {
            return null;
        }

        @Override
        public void cancelCurrentManeuver() {
        }

        public AttackAIModule getAttackAI() {
            return null;
        }

        public ShieldAI getShieldAI() {
            return null;
        }

        public ThreatEvaluator getThreatEvaluator() {
            return null;
        }

        public FlockingAI getFlockingAI() {
            return null;
        }

        @Override
        public void setDoNotFireDelay(float amount) {
        }

        @Override
        public void forceCircumstanceEvaluation() {
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
        public ShipAIConfig getConfig() {
            return null;
        }
    }

    public static class FlockingAI {
        public void flockingAI_setDesiredHeading(float heading) {
        }

        public void flockingAI_setDesiredFacing(float facing) {
        }

        public void flockingAI_setDesiredSpeed(float speed) {
        }

        public void flockingAI_advanceCollisionAnalysisModule(float dt) {
        }

        public Vector2f flockingAI_getMissileDangerDir() {
            return null;
        }
    }

    public static class AttackAIModule {
        public void attackAIModule_advance(float dt, ThreatEvaluator threatEvalAI, Vector2f missileDangerDir) {
        }
    }

    public static class FighterPullbackModule {
        public void fighterPullbackModule_advance(float dt, Ship attackTarget) {
        }
    }

    public static class VentModule {
        public void ventModule_advance(float dt, Ship target) {
        }
    }

    public static class AimTracker {
        public void aimTracker_setTargetOverride(Vector2f aim) {
        }
    }

    public static class Keymap {
        public static boolean keymap_isKeyDown(PlayerAction action) {
            return false;
        }
    }

    public static class CombatEngine {
        public CombatMap getCombatMap() {
            return null;
        }
    }

    public static class CombatMap {
        public List<?> combatMap_getPluginContainers() {
            return null;
        }
    }

    public static class MissionDefinitionPluginContainer {
        public EveryFrameCombatPlugin missionDefinitionPluginContainer_getEveryFrameCombatPlugin() {
            return null;
        }
    }

    public static class BeamWeapon {
        public boolean isForceFireOneFrame() {
            return false;
        }

        public boolean isForceNoFireOneFrame() {
            return false;
        }
    }

    public static class ProjectileWeapon {
        public boolean isForceFireOneFrame() {
            return false;
        }

        public boolean isForceNoFireOneFrame() {
            return false;
        }
    }

    public static class LoadingUtils {
        public static JSONObject loadingUtils_loadSpec(String path, Set<String> def) {
            return null;
        }
    }

    public static class FrontShieldAI {
        public FrontShieldAI(com.fs.starfarer.combat.entities.Ship ship, ShipwideAIFlags flags) {
        }
    }

    public static class BoundsSegment {
        public float x1;
        public float y1;
        public float x2;
        public float y2;
        public Vector2f p1;
        public Vector2f p2;
    }

    public static class Bounds {
        public List<BoundsSegment> origSegments = new ArrayList<>();
    }
}
